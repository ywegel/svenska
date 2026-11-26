package de.ywegel.svenska.data.impl

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import de.ywegel.svenska.RobolectricTest
import de.ywegel.svenska.data.db.QuizPoolDao
import de.ywegel.svenska.data.db.ReviewDao
import de.ywegel.svenska.data.db.VocabularyDatabase
import de.ywegel.svenska.data.db.quiz.FormsFacetCard
import de.ywegel.svenska.data.db.quiz.FsrsColumns
import de.ywegel.svenska.data.db.quiz.QuizPoolEntry
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.quiz.PoolRemoval
import de.ywegel.svenska.data.model.quiz.QuizFacet
import de.ywegel.svenska.data.model.quiz.QuizTranslationDirection
import de.ywegel.svenska.data.model.quiz.formsKey
import de.ywegel.svenska.data.model.vocabulary
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import kotlin.time.Instant

class QuizPoolRepositoryImplTest : RobolectricTest() {
    private lateinit var db: VocabularyDatabase
    private lateinit var dao: QuizPoolDao
    private lateinit var reviewDao: ReviewDao
    private lateinit var repository: QuizPoolRepositoryImpl

    @Before
    fun setUp() = runTest {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), VocabularyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.quizPool()
        reviewDao = db.reviewDao()
        repository = QuizPoolRepositoryImpl(db, dao, db.vocabulary(), reviewDao)
        db.vocabulary().upsertVocabulary(vocabulary(id = VOCABULARY_ID))
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `Removing a queued word deletes its entry`() = runTest {
        // Given
        val entry = queued()
        dao.insert(entry)

        // When
        val removal = repository.removeFromPool(VOCABULARY_ID)

        // Then
        expectThat(removal).isEqualTo(PoolRemoval.DELETED)
        expectThat(dao.get(VOCABULARY_ID)).isNull()
    }

    @Test
    fun `Removing an active word pauses it and keeps its progress`() = runTest {
        // Given
        val entry = active()
        dao.insert(entry)

        // When
        val removal = repository.removeFromPool(VOCABULARY_ID)

        // Then
        expectThat(removal).isEqualTo(PoolRemoval.PAUSED)
        expectThat(dao.get(VOCABULARY_ID)).isEqualTo(entry.copy(isPaused = true))
    }

    @Test
    fun `Removing a paused word keeps it paused`() = runTest {
        // Given
        val entry = active().copy(isPaused = true)
        dao.insert(entry)

        // When
        val removal = repository.removeFromPool(VOCABULARY_ID)

        // Then
        expectThat(removal).isEqualTo(PoolRemoval.UNCHANGED)
        expectThat(dao.get(VOCABULARY_ID)).isEqualTo(entry)
    }

    @Test
    fun `Removing a word that is not in the pool changes nothing`() = runTest {
        // When
        val removal = repository.removeFromPool(VOCABULARY_ID)

        // Then
        expectThat(removal).isEqualTo(PoolRemoval.UNCHANGED)
        expectThat(dao.get(VOCABULARY_ID)).isNull()
    }

    @Test
    fun `Removing a word introduced after it was shown as queued pauses it`() = runTest {
        // Given: the list still shows the word as queued, but a session has introduced it since
        val shownAsQueued = queued()
        dao.insert(shownAsQueued)
        dao.update(shownAsQueued.introduce(now = 2_000L))

        // When
        val removal = repository.removeFromPool(VOCABULARY_ID)

        // Then
        expectThat(removal).isEqualTo(PoolRemoval.PAUSED)
        expectThat(dao.get(VOCABULARY_ID)?.isPaused).isEqualTo(true)
    }

    @Test
    fun `Words and forms introduced since the start of the day count together`() = runTest {
        // Given
        insert(word(id = 2), active().copy(vocabularyId = 2, introducedAt = YESTERDAY))
        insert(word(id = 3), active().copy(vocabularyId = 3, introducedAt = TODAY))
        insert(word(id = 4), active().copy(vocabularyId = 4, introducedAt = YESTERDAY, formsIntroducedAt = TODAY))

        // When
        val count = repository.countIntroducedSince(Instant.fromEpochMilliseconds(TODAY))

        // Then
        expectThat(count).isEqualTo(2)
    }

    @Test
    fun `Queued words come first in, first out, and words queued together in the order of their id`() = runTest {
        // Given
        insert(word(id = 2), queued().copy(vocabularyId = 2, queuedAt = 2_000L))
        insert(word(id = 3), queued().copy(vocabularyId = 3, queuedAt = 1_000L))
        insert(word(id = 4), queued().copy(vocabularyId = 4, queuedAt = 2_000L))
        insert(word(id = 5), active().copy(vocabularyId = 5, queuedAt = 500L))

        // When
        val queued = repository.queuedWords(limit = 2)

        // Then
        expectThat(queued.map { it.id }).containsExactly(3, 2)
    }

    @Test
    fun `Only active rows with the streak, forms and no forms that need review are forms candidates`() = runTest {
        // Given
        insert(word(id = 2, word = "bil"), readyForForms().copy(vocabularyId = 2))
        insert(word(id = 3, word = "hus"), readyForForms().copy(vocabularyId = 3, svDeStreak = 2))
        insert(word(id = 4, word = "sko").copy(formsNeedReview = true), readyForForms().copy(vocabularyId = 4))
        insert(word(id = 5, word = "bok"), readyForForms().copy(vocabularyId = 5, isPaused = true))
        insert(word(id = 7, word = "katt", withForms = false), readyForForms().copy(vocabularyId = 7))

        // When
        val candidates = repository.formsCandidates(minSvDeStreak = 3)

        // Then
        expectThat(candidates.map { it.id }).containsExactly(2)
    }

    @Test
    fun `A row whose forms were edited after introducing its forms card is a candidate for its new forms`() = runTest {
        // Given
        val before = word(id = 2, word = "bil")
        val edited = before.copy(form2 = "bilor")
        insert(edited, readyForForms().copy(vocabularyId = 2, formsIntroducedAt = YESTERDAY))
        reviewDao.insertFormsCardIfAbsent(
            FormsFacetCard(
                formsKey = checkNotNull(before.formsKey),
                fsrs = FsrsColumns.newCard(due = TODAY),
            ),
        )

        // When
        val candidates = repository.formsCandidates(minSvDeStreak = 3)

        // Then
        expectThat(candidates.map { it.id }).containsExactly(2)
    }

    @Test
    fun `A row whose forms card exists through another row is no candidate`() = runTest {
        // Given
        val row = word(id = 2)
        insert(row, readyForForms().copy(vocabularyId = 2))
        reviewDao.insertFormsCardIfAbsent(FormsFacetCard(checkNotNull(row.formsKey), FsrsColumns.newCard(due = TODAY)))

        // When
        val candidates = repository.formsCandidates(minSvDeStreak = 3)

        // Then
        expectThat(candidates).isEmpty()
    }

    @Test
    fun `Forms candidates come longest introduced first`() = runTest {
        // Given
        insert(word(id = 2, word = "bil"), readyForForms().copy(vocabularyId = 2, introducedAt = TODAY))
        insert(word(id = 3, word = "hus"), readyForForms().copy(vocabularyId = 3, introducedAt = YESTERDAY))

        // When
        val candidates = repository.formsCandidates(minSvDeStreak = 3)

        // Then
        expectThat(candidates.map { it.id }).containsExactly(3, 2)
    }

    @Test
    fun `Introducing creates new cards due now and marks only the introducing rows`() = runTest {
        // Given
        val newWord = word(id = 2, word = "bil")
        val formsWord = word(id = 3, word = "hus")
        insert(newWord, queued().copy(vocabularyId = 2))
        insert(formsWord, readyForForms().copy(vocabularyId = 3))
        val now = Instant.fromEpochMilliseconds(TODAY)

        // When
        val cards = repository.introduce(words = listOf(newWord), forms = listOf(formsWord), now = now)

        // Then
        expectThat(cards.map { it.vocabulary.id to it.facet })
            .containsExactly(2 to QuizFacet.SV_DE, 3 to QuizFacet.FORMS)
        expectThat(dao.get(2)?.introducedAt).isEqualTo(TODAY)
        expectThat(dao.get(3)?.formsIntroducedAt).isEqualTo(TODAY)
        expectThat(reviewDao.getTranslationCard(2, QuizTranslationDirection.SV_DE)?.fsrs?.due).isEqualTo(TODAY)
        expectThat(reviewDao.getFormsCard(checkNotNull(formsWord.formsKey))).isNotNull()
    }

    @Test
    fun `Queueing saves the forms and queues all words at the same time`() = runTest {
        // Given
        val bil = word(id = 2, word = "bil")
        val hus = word(id = 3, word = "hus").copy(formsNeedReview = true)
        db.vocabulary().upsertVocabulary(bil.withoutForms())
        db.vocabulary().upsertVocabulary(hus.withoutForms())

        // When
        repository.queueToPool(listOf(bil, hus), Instant.fromEpochMilliseconds(TODAY))

        // Then
        expectThat(stored(2)).isEqualTo(bil)
        expectThat(stored(3)).isEqualTo(hus)
        expectThat(dao.get(2)).isEqualTo(QuizPoolEntry(vocabularyId = 2, queuedAt = TODAY))
        expectThat(dao.get(3)).isEqualTo(QuizPoolEntry(vocabularyId = 3, queuedAt = TODAY))
    }

    @Test
    fun `Queueing writes only the forms, so an edit made after loading the word is kept`() = runTest {
        // Given
        val loaded = word(id = 2)
        db.vocabulary().upsertVocabulary(loaded.withoutForms().copy(translation = "edited"))

        // When
        repository.queueToPool(listOf(loaded), Instant.fromEpochMilliseconds(TODAY))

        // Then
        expectThat(stored(2)).isEqualTo(loaded.copy(translation = "edited"))
    }

    @Test
    fun `Queueing a paused word keeps its entry`() = runTest {
        // Given
        val entry = active().copy(vocabularyId = 2, isPaused = true)
        insert(word(id = 2), entry)

        // When
        repository.queueToPool(listOf(word(id = 2)), Instant.fromEpochMilliseconds(TODAY))

        // Then
        expectThat(dao.get(2)).isEqualTo(entry)
    }

    @Test
    fun `A failed queueing saves no forms`() = runTest {
        // Given: the second word was deleted after loading, so its entry breaks the foreign key
        val bil = word(id = 2, word = "bil")
        db.vocabulary().upsertVocabulary(bil.withoutForms())
        val deleted = word(id = 99)

        // When
        val result = runCatching { repository.queueToPool(listOf(bil, deleted), Instant.fromEpochMilliseconds(TODAY)) }

        // Then
        expectThat(result.isFailure).isEqualTo(true)
        expectThat(stored(2)).isEqualTo(bil.withoutForms())
    }

    private suspend fun stored(id: Int): Vocabulary = db.vocabulary().getAllVocabulariesSnapshot().single { it.id == id }

    private fun Vocabulary.withoutForms() = copy(form1 = null, form2 = null, form3 = null, formsNeedReview = false)

    private suspend fun insert(row: Vocabulary, entry: QuizPoolEntry) {
        db.vocabulary().upsertVocabulary(row)
        dao.insert(entry)
    }

    private companion object {
        const val VOCABULARY_ID = 1
        const val YESTERDAY = 1_000L
        const val TODAY = 90_000_000L

        fun queued() = QuizPoolEntry(vocabularyId = VOCABULARY_ID, queuedAt = 1_000L)

        fun active() = queued().copy(introducedAt = 2_000L, svDeStreak = 2)

        fun readyForForms() = active().copy(svDeStreak = 3)

        fun word(id: Int, word: String = "lampa", withForms: Boolean = true): Vocabulary =
            vocabulary(id = id, word = word).let {
                if (withForms) it.copy(form1 = "${word}n", form2 = "${word}r", form3 = "${word}rna") else it
            }
    }
}
