package de.ywegel.svenska.domain.quiz.session

import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.quiz.QuizCard
import de.ywegel.svenska.data.model.quiz.QuizFacet
import de.ywegel.svenska.data.model.vocabulary
import de.ywegel.svenska.domain.quiz.session.DueCardsTest.Companion.LAMPA
import one.felsen.fsrskt.fsrs6.FsrsCard
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

class DueCardsTest {
    @ParameterizedTest
    @MethodSource("singleCards")
    fun `A card on its own is asked only when it is due`(facet: QuizFacet, due: Instant, isAsked: Boolean) {
        // Given
        val card = card(LAMPA, facet, due)

        // When
        val items = planDueCards(listOf(card), NOW, START_OF_TODAY)

        // Then
        expectThat(items.size).isEqualTo(if (isAsked) 1 else 0)
    }

    @ParameterizedTest
    @MethodSource("formsAlongDeSv")
    fun `A due DE_SV card takes the forms card along once it is due or far enough into its interval`(
        forms: QuizCard,
        isTakenAlong: Boolean,
    ) {
        // Given
        val deSv = card(LAMPA, QuizFacet.DE_SV, due = NOW)

        // When
        val items = planDueCards(listOf(deSv, forms), NOW, START_OF_TODAY)

        // Then
        expectThat(items).containsExactly(
            QuizItem.DeSv(LAMPA, withForms = isTakenAlong),
        )
    }

    @Test
    fun `A forms card that is only far into its interval is not asked without a due DE_SV card`() {
        // Given
        val deSv = card(LAMPA, QuizFacet.DE_SV, due = NOW + 1.days)
        val forms = formsCardAt(elapsedShare = 0.8)

        // When
        val items = planDueCards(listOf(deSv, forms), NOW, START_OF_TODAY)

        // Then
        expectThat(items).isEmpty()
    }

    @Test
    fun `Duplicates ask their shared forms card only with the first due DE_SV card`() {
        // Given
        val first = card(LAMPA, QuizFacet.DE_SV, due = NOW)
        val duplicate = card(LAMPA_DUPLICATE, QuizFacet.DE_SV, due = NOW)
        val forms = card(LAMPA, QuizFacet.FORMS, due = NOW)

        // When
        val items = planDueCards(listOf(first, duplicate, forms), NOW, START_OF_TODAY)

        // Then
        expectThat(items).containsExactly(
            QuizItem.DeSv(LAMPA, withForms = true),
            QuizItem.DeSv(LAMPA_DUPLICATE),
        )
    }

    @ParameterizedTest
    @MethodSource("svDeHoldBacks")
    fun `A due SV_DE card waits while the DE_SV card of its word is due or was answered today`(
        deSv: QuizCard?,
        isAsked: Boolean,
    ) {
        // Given
        val svDe = card(LAMPA, QuizFacet.SV_DE, due = NOW)

        // When
        val items = planDueCards(listOfNotNull(svDe, deSv), NOW, START_OF_TODAY)

        // Then
        expectThat(QuizItem.SvDe(LAMPA) in items).isEqualTo(isAsked)
    }

    @Test
    fun `DE_SV items come first, then forms, then SV_DE`() {
        // Given
        val svDe = card(BIL, QuizFacet.SV_DE, due = NOW)
        val forms = card(LAMPA, QuizFacet.FORMS, due = NOW)
        val deSv = card(HUS, QuizFacet.DE_SV, due = NOW)

        // When
        val items = planDueCards(listOf(svDe, forms, deSv), NOW, START_OF_TODAY)

        // Then
        expectThat(items).containsExactly(
            QuizItem.DeSv(HUS),
            QuizItem.Forms(LAMPA),
            QuizItem.SvDe(BIL),
        )
    }

    companion object {
        private val NOW = Instant.parse("2026-11-25T18:00:00Z")
        private val START_OF_TODAY = Instant.parse("2026-11-25T03:00:00Z")
        private val FORMS_INTERVAL = 100.hours

        private val LAMPA = wordWithForms(id = 1, word = "lampa", "lampan", "lampor", "lamporna")
        private val LAMPA_DUPLICATE = LAMPA.copy(id = 2, translation = "Leuchte")
        private val HUS = wordWithForms(id = 3, word = "hus", "huset", "hus", "husen")
        private val BIL = wordWithForms(id = 4, word = "bil", "bilen", "bilar", "bilarna")

        private fun wordWithForms(id: Int, word: String, vararg forms: String): Vocabulary =
            vocabulary(id = id, word = word).copy(form1 = forms[0], form2 = forms[1], form3 = forms[2])

        private fun card(vocabulary: Vocabulary, facet: QuizFacet, due: Instant, lastReview: Instant? = null) =
            QuizCard(vocabulary, facet, FsrsCard(due = due, lastReview = lastReview))

        /** A reviewed forms card of [LAMPA], [elapsedShare] of its interval after its last review. */
        private fun formsCardAt(elapsedShare: Double): QuizCard {
            val lastReview = NOW - FORMS_INTERVAL * elapsedShare
            return card(LAMPA, QuizFacet.FORMS, due = lastReview + FORMS_INTERVAL, lastReview = lastReview)
        }

        @JvmStatic
        fun singleCards() = QuizFacet.entries.flatMap { facet ->
            listOf(
                Arguments.of(facet, NOW, true),
                Arguments.of(facet, NOW - 1.days, true),
                Arguments.of(facet, NOW + 1.hours, false),
            )
        }

        @JvmStatic
        fun formsAlongDeSv() = listOf(
            argumentSet("due", formsCardAt(elapsedShare = 1.0), true),
            argumentSet("80 % into its interval", formsCardAt(elapsedShare = 0.8), true),
            argumentSet("79 % into its interval", formsCardAt(elapsedShare = 0.79), false),
            argumentSet("new and due", card(LAMPA, QuizFacet.FORMS, due = NOW), true),
            argumentSet("new and not due", card(LAMPA, QuizFacet.FORMS, due = NOW + 1.hours), false),
        )

        @JvmStatic
        fun svDeHoldBacks() = listOf(
            argumentSet("no DE_SV card yet", null, true),
            argumentSet("DE_SV due", card(LAMPA, QuizFacet.DE_SV, due = NOW), false),
            argumentSet(
                "DE_SV answered today",
                card(LAMPA, QuizFacet.DE_SV, due = NOW + 3.days, lastReview = START_OF_TODAY + 1.hours),
                false,
            ),
            argumentSet(
                "DE_SV answered before today",
                card(LAMPA, QuizFacet.DE_SV, due = NOW + 3.days, lastReview = START_OF_TODAY - 1.hours),
                true,
            ),
            argumentSet(
                "DE_SV of another word due",
                card(HUS, QuizFacet.DE_SV, due = NOW),
                true,
            ),
        )
    }
}
