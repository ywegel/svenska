package de.ywegel.svenska.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import de.ywegel.svenska.RobolectricTest
import de.ywegel.svenska.data.db.quiz.CardPhase
import de.ywegel.svenska.data.db.quiz.FormsFacetCard
import de.ywegel.svenska.data.db.quiz.FsrsColumns
import de.ywegel.svenska.data.db.quiz.QuizPoolEntry
import de.ywegel.svenska.data.db.quiz.QuizTranslationDirection
import de.ywegel.svenska.data.db.quiz.ReviewLog
import de.ywegel.svenska.data.db.quiz.TranslationFacetCard
import de.ywegel.svenska.data.model.quiz.QuizFacet
import de.ywegel.svenska.data.model.quiz.SvDeOutcome
import de.ywegel.svenska.data.model.vocabulary
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isNull
import kotlin.time.Instant

class ReviewDaoTest : RobolectricTest() {
    private lateinit var db: VocabularyDatabase
    private lateinit var reviewDao: ReviewDao
    private lateinit var poolDao: QuizPoolDao

    @Before
    fun setUp() = runTest {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), VocabularyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        reviewDao = db.reviewDao()
        poolDao = db.quizPool()
        db.vocabulary().upsertVocabulary(vocabulary(id = VOCABULARY_ID))
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `Saving a review writes the reviewed card, the log and the streak`() = runTest {
        // Given
        poolDao.insert(activeEntry(svDeStreak = 1))
        reviewDao.insertTranslationCardIfAbsent(svDeCard(due = NOW))
        val reviewed = svDeCard(due = LATER)

        // When
        reviewDao.saveReview(reviewed, log = svDeLog(), svDe = PASSED)

        // Then
        expectThat(reviewDao.getTranslationCard(VOCABULARY_ID, QuizTranslationDirection.SV_DE)).isEqualTo(reviewed)
        expectThat(reviewDao.getLogs(VOCABULARY_ID).map { it.rating }).containsExactly(GOOD)
        expectThat(poolDao.get(VOCABULARY_ID)?.svDeStreak).isEqualTo(2)
    }

    @Test
    fun `A failed SV_DE review resets the streak and unlocks nothing`() = runTest {
        // Given
        poolDao.insert(activeEntry(svDeStreak = 2))
        reviewDao.insertTranslationCardIfAbsent(svDeCard(due = NOW))

        // When
        reviewDao.saveReview(svDeCard(due = LATER), log = svDeLog(), svDe = SvDeOutcome.Failed)

        // Then
        expectThat(poolDao.get(VOCABULARY_ID)?.svDeStreak).isEqualTo(0)
        expectThat(reviewDao.getTranslationCard(VOCABULARY_ID, QuizTranslationDirection.DE_SV)).isNull()
    }

    @Test
    fun `Saving a review without streak keeps the pool entry`() = runTest {
        // Given
        poolDao.insert(activeEntry(svDeStreak = 3))
        reviewDao.insertFormsCardIfAbsent(formsCard(due = NOW))

        // When
        reviewDao.saveReview(formsCard(due = LATER), log = formsLog(), svDe = null)

        // Then
        expectThat(reviewDao.getFormsCard(FORMS_KEY)?.fsrs?.due).isEqualTo(LATER)
        expectThat(poolDao.get(VOCABULARY_ID)?.svDeStreak).isEqualTo(3)
    }

    @Test
    fun `Saving a review keeps a pause made during the session`() = runTest {
        // Given
        val entryAtSessionStart = activeEntry()
        poolDao.insert(entryAtSessionStart)
        poolDao.update(entryAtSessionStart.copy(isPaused = true))
        reviewDao.insertTranslationCardIfAbsent(svDeCard(due = NOW))

        // When
        reviewDao.saveReview(svDeCard(due = LATER), log = svDeLog(), svDe = PASSED)

        // Then
        expectThat(poolDao.get(VOCABULARY_ID)?.isPaused).isEqualTo(true)
    }

    @Test
    fun `Saving a review of a deleted card drops the review`() = runTest {
        // Given
        poolDao.insert(activeEntry())

        // When
        reviewDao.saveReview(deSvCard(due = LATER), log = deSvLog(), svDe = null)

        // Then
        expectThat(reviewDao.getTranslationCard(VOCABULARY_ID, QuizTranslationDirection.DE_SV)).isNull()
        expectThat(reviewDao.getLogs(VOCABULARY_ID)).isEmpty()
    }

    @Test
    fun `Saving a review inserts a newly unlocked DE_SV card`() = runTest {
        // Given
        poolDao.insert(activeEntry())
        reviewDao.insertTranslationCardIfAbsent(svDeCard(due = NOW))
        val unlocked = deSvCard(due = TOMORROW)

        // When
        reviewDao.saveReview(svDeCard(due = LATER), log = svDeLog(), svDe = PASSED)

        // Then
        expectThat(reviewDao.getTranslationCard(VOCABULARY_ID, QuizTranslationDirection.DE_SV)).isEqualTo(unlocked)
    }

    @Test
    fun `Unlocking DE_SV again keeps the progress of the existing card`() = runTest {
        // Given
        poolDao.insert(activeEntry())
        reviewDao.insertTranslationCardIfAbsent(svDeCard(due = NOW))
        val learned = deSvCard(due = LATER)
        reviewDao.insertTranslationCardIfAbsent(learned)

        // When
        reviewDao.saveReview(
            svDeCard(due = LATER),
            log = svDeLog(),
            svDe = PASSED,
        )

        // Then
        expectThat(reviewDao.getTranslationCard(VOCABULARY_ID, QuizTranslationDirection.DE_SV)).isEqualTo(learned)
    }

    @Test
    fun `Logs of a row that was never reset all count`() = runTest {
        // Given
        poolDao.insert(activeEntry())
        reviewDao.insertTranslationCardIfAbsent(svDeCard(due = NOW))
        reviewDao.saveReview(svDeCard(due = LATER), log = svDeLog(reviewAt = NOW), svDe = PASSED)
        reviewDao.saveReview(svDeCard(due = LATER), log = svDeLog(reviewAt = LATER), svDe = PASSED)

        // When
        val logs = reviewDao.getLogs(VOCABULARY_ID)

        // Then
        expectThat(logs.map { it.reviewAt }).containsExactly(NOW, LATER)
    }

    @Test
    fun `Forms logs are not part of the logs of a row`() = runTest {
        // Given
        poolDao.insert(activeEntry())
        reviewDao.insertFormsCardIfAbsent(formsCard(due = NOW))
        reviewDao.saveReview(formsCard(due = LATER), log = formsLog(), svDe = null)

        // When
        val logs = reviewDao.getLogs(VOCABULARY_ID)

        // Then
        expectThat(logs).isEmpty()
    }

    @Test
    fun `A reset drops the logs before it`() = runTest {
        // Given
        val entry = activeEntry()
        poolDao.insert(entry)
        reviewDao.insertTranslationCardIfAbsent(svDeCard(due = NOW))
        reviewDao.saveReview(svDeCard(due = LATER), log = svDeLog(reviewAt = NOW), svDe = PASSED)
        poolDao.update(entry.reset(now = RESET))
        reviewDao.saveReview(svDeCard(due = LATER), log = svDeLog(reviewAt = LATER), svDe = PASSED)

        // When
        val logs = reviewDao.getLogs(VOCABULARY_ID)

        // Then
        expectThat(logs.map { it.reviewAt }).containsExactly(LATER)
    }

    private fun activeEntry(svDeStreak: Int = 0) =
        QuizPoolEntry(vocabularyId = VOCABULARY_ID, queuedAt = NOW, introducedAt = NOW, svDeStreak = svDeStreak)

    private fun svDeCard(due: Long) = TranslationFacetCard(VOCABULARY_ID, QuizTranslationDirection.SV_DE, fsrs(due))

    private fun deSvCard(due: Long) = TranslationFacetCard(VOCABULARY_ID, QuizTranslationDirection.DE_SV, fsrs(due))

    private fun formsCard(due: Long) = FormsFacetCard(FORMS_KEY, fsrs(due))

    private fun fsrs(due: Long) = FsrsColumns(
        phase = CardPhase.LEARNING,
        step = 0,
        stability = null,
        difficulty = null,
        due = due,
        lastReview = null,
    )

    private fun svDeLog(reviewAt: Long = NOW) = log(QuizFacet.SV_DE, formsKey = null, reviewAt = reviewAt)

    private fun deSvLog() = log(QuizFacet.DE_SV, formsKey = null, reviewAt = NOW)

    private fun formsLog(reviewAt: Long = NOW) = log(QuizFacet.FORMS, formsKey = FORMS_KEY, reviewAt = reviewAt)

    private fun log(facet: QuizFacet, formsKey: String?, reviewAt: Long) = ReviewLog(
        vocabularyId = VOCABULARY_ID,
        facet = facet,
        formsKey = formsKey,
        rating = GOOD,
        rawAnswer = "lamp",
        reviewAt = reviewAt,
        durationMillis = null,
    )

    private companion object {
        const val VOCABULARY_ID = 1
        const val FORMS_KEY = "lampa|lampan|lampor|lamporna"
        const val GOOD = 3
        const val NOW = 1_000L
        const val RESET = 1_500L
        const val LATER = 2_000L
        const val TOMORROW = 90_000_000L
        val PASSED = SvDeOutcome.Passed(deSvDue = Instant.fromEpochMilliseconds(TOMORROW))
    }
}
