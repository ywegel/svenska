package de.ywegel.svenska.data.impl

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import de.ywegel.svenska.RobolectricTest
import de.ywegel.svenska.data.db.VocabularyDatabase
import de.ywegel.svenska.data.db.quiz.FormsFacetCard
import de.ywegel.svenska.data.db.quiz.FsrsColumns
import de.ywegel.svenska.data.db.quiz.QuizPoolEntry
import de.ywegel.svenska.data.db.quiz.TranslationFacetCard
import de.ywegel.svenska.data.model.Vocabulary
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
import strikt.assertions.containsExactlyInAnyOrder

class QuizCardRepositoryImplTest : RobolectricTest() {
    private lateinit var db: VocabularyDatabase
    private lateinit var repository: QuizCardRepositoryImpl

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), VocabularyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = QuizCardRepositoryImpl(db.reviewDao(), db.quizPool())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `Only the translation cards of active words are loaded`() = runTest {
        // Given
        insert(word(id = 1), ACTIVE)
        insert(word(id = 2), ACTIVE.copy(isPaused = true))
        insert(word(id = 3), QUEUED)
        listOf(1, 2, 3).forEach { insertSvDeCard(vocabularyId = it) }

        // When
        val cards = repository.getActiveCards()

        // Then
        expectThat(cards.map { it.vocabulary.id }).containsExactly(1)
    }

    @Test
    fun `A shared forms card comes once, for the active row with the smallest id`() = runTest {
        // Given
        val paused = word(id = 1)
        insert(paused, ACTIVE.copy(isPaused = true))
        insert(word(id = 3).copy(wordSuffix = "sig"), ACTIVE)
        insert(word(id = 2).copy(wordSuffix = "upp"), ACTIVE)
        db.reviewDao()
            .insertFormsCardIfAbsent(FormsFacetCard(checkNotNull(paused.formsKey), FsrsColumns.newCard(due = 0L)))

        // When
        val cards = repository.getActiveCards()

        // Then
        expectThat(cards.map { it.vocabulary.id to it.facet }).containsExactlyInAnyOrder(2 to QuizFacet.FORMS)
    }

    @Test
    fun `A forms card without an active row is not loaded`() = runTest {
        // Given
        val paused = word(id = 1)
        insert(paused, ACTIVE.copy(isPaused = true))
        db.reviewDao()
            .insertFormsCardIfAbsent(FormsFacetCard(checkNotNull(paused.formsKey), FsrsColumns.newCard(due = 0L)))

        // When
        val cards = repository.getActiveCards()

        // Then
        expectThat(cards.map { it.vocabulary.id }).containsExactly()
    }

    private suspend fun insert(row: Vocabulary, entry: QuizPoolEntry) {
        db.vocabulary().upsertVocabulary(row)
        db.quizPool().insert(entry.copy(vocabularyId = row.id))
    }

    private suspend fun insertSvDeCard(vocabularyId: Int) = db.reviewDao().insertTranslationCardIfAbsent(
        TranslationFacetCard(vocabularyId, QuizTranslationDirection.SV_DE, FsrsColumns.newCard(due = 0L)),
    )

    private companion object {
        val QUEUED = QuizPoolEntry(vocabularyId = 0, queuedAt = 1_000L)
        val ACTIVE = QUEUED.copy(introducedAt = 2_000L)

        fun word(id: Int): Vocabulary =
            vocabulary(id = id).copy(form1 = "lampan", form2 = "lampor", form3 = "lamporna")
    }
}
