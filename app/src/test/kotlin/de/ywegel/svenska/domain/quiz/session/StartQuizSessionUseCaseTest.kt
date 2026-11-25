package de.ywegel.svenska.domain.quiz.session

import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.data.model.quiz.QuizCard
import de.ywegel.svenska.data.model.quiz.QuizFacet
import de.ywegel.svenska.data.model.vocabulary
import de.ywegel.svenska.fakes.QuizCardRepositoryFake
import de.ywegel.svenska.fakes.QuizPoolRepositoryFake
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import one.felsen.fsrskt.fsrs6.FsrsCard
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

class StartQuizSessionUseCaseTest {
    @Test
    fun `Forms cards get the daily limit first, queued words the rest`() = runTest {
        // Given
        val pool = QuizPoolRepositoryFake(
            introducedToday = 8,
            formsCandidates = listOf(HUS),
            queuedWords = listOf(BIL, KATT),
        )

        // When
        start(pool)

        // Then
        expectThat(pool.introducedForms).containsExactly(HUS)
        expectThat(pool.introducedWords).containsExactly(BIL)
    }

    @Test
    fun `Rows that share their forms introduce one card, through the smallest id`() = runTest {
        // Given
        val husSig = HUS.copy(id = 5, wordSuffix = "sig")
        val pool = QuizPoolRepositoryFake(formsCandidates = listOf(husSig, HUS))

        // When
        start(pool)

        // Then
        expectThat(pool.introducedForms).containsExactly(HUS)
    }

    @Test
    fun `A row without forms to ask doesn't keep a row with the same forms from its forms card`() = runTest {
        // Given
        val withoutGender = HUS.copy(gender = null)
        val husSig = HUS.copy(id = 5, wordSuffix = "sig")
        val pool = QuizPoolRepositoryFake(formsCandidates = listOf(withoutGender, husSig))

        // When
        start(pool)

        // Then
        expectThat(pool.introducedForms).containsExactly(husSig)
    }

    @Test
    fun `New words are spread between the due ones`() = runTest {
        // Given
        val due = (10..13).map { vocabulary(id = it) }
        val cards = QuizCardRepositoryFake(*due.map { card(it, QuizFacet.SV_DE, due = NOW) }.toTypedArray())
        val pool = QuizPoolRepositoryFake(queuedWords = listOf(BIL, KATT))

        // When
        val items = start(pool, cards)

        // Then
        expectThat(items.map { it.vocabulary.id }).containsExactly(BIL.id, 10, 11, KATT.id, 12, 13)
    }

    @Test
    fun `A regular adjective gets no forms card`() = runTest {
        // Given
        val pool = QuizPoolRepositoryFake(formsCandidates = listOf(STOR))

        // When
        start(pool)

        // Then
        expectThat(pool.introducedForms).isEmpty()
    }

    @Test
    fun `Nothing new is introduced while more reviews than the backlog limit are due`() = runTest {
        // Given
        val dueCards = (1..101).map { card(vocabulary(id = it), QuizFacet.SV_DE, due = NOW) }
        val pool = QuizPoolRepositoryFake(formsCandidates = listOf(HUS), queuedWords = listOf(BIL))

        // When
        start(pool, QuizCardRepositoryFake(*dueCards.toTypedArray()))

        // Then
        expectThat(pool.introducedForms + pool.introducedWords).isEmpty()
    }

    @Test
    fun `A new forms card is asked along with the due DE_SV card of its word`() = runTest {
        // Given
        val cards = QuizCardRepositoryFake(card(HUS, QuizFacet.DE_SV, due = NOW))
        val pool = QuizPoolRepositoryFake(formsCandidates = listOf(HUS))

        // When
        val items = start(pool, cards)

        // Then
        expectThat(items).containsExactly(QuizItem.DeSv(HUS, withForms = true))
    }

    @Test
    fun `A session before 4 AM counts the new cards of the previous day`() = runTest {
        // Given
        val pool = QuizPoolRepositoryFake()
        val night = LocalDateTime.parse("2026-11-25T02:00").toInstant(TIME_ZONE)

        // When
        start(pool, now = night)

        // Then
        expectThat(pool.countedSince).isEqualTo(LocalDateTime.parse("2026-11-24T04:00").toInstant(TIME_ZONE))
    }

    private suspend fun start(
        pool: QuizPoolRepositoryFake,
        cards: QuizCardRepositoryFake = QuizCardRepositoryFake(),
        now: Instant = NOW,
    ): List<QuizItem> = StartQuizSessionUseCase(cards, pool, TIME_ZONE).invoke(now)

    private companion object {
        val TIME_ZONE = TimeZone.of("Europe/Stockholm")
        val NOW: Instant = LocalDateTime.parse("2026-11-25T18:00").toInstant(TIME_ZONE)

        val HUS = noun(id = 1, word = "hus", "huset", "hus", "husen")
        val BIL = noun(id = 2, word = "bil", "bilen", "bilar", "bilarna")
        val KATT = noun(id = 3, word = "katt", "katten", "katter", "katterna")
        val STOR =
            vocabulary(id = 4, word = "stor", wordGroup = WordGroup.Adjective).copy(form1 = "stort", form2 = "stora")

        fun noun(id: Int, word: String, vararg forms: String): Vocabulary =
            vocabulary(id = id, word = word).copy(form1 = forms[0], form2 = forms[1], form3 = forms[2])

        fun card(vocabulary: Vocabulary, facet: QuizFacet, due: Instant) =
            QuizCard(vocabulary, facet, FsrsCard(due = due, lastReview = due - 1.days))
    }
}
