package de.ywegel.svenska.data.model.quiz

import de.ywegel.svenska.common.streamOf
import de.ywegel.svenska.data.model.quiz.QuizFacet.DE_SV
import de.ywegel.svenska.data.model.quiz.QuizFacet.FORMS
import de.ywegel.svenska.data.model.quiz.QuizFacet.SV_DE
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class VocabularyPoolStateTest {

    @ParameterizedTest
    @MethodSource("poolStateCases")
    fun `of derives the state from the pool status and the existing cards`(
        status: QuizPoolStatus?,
        hasDeSvCard: Boolean,
        hasFormsCard: Boolean,
        expected: VocabularyPoolState,
    ) {
        // When
        val poolState = VocabularyPoolState.of(status, hasDeSvCard, hasFormsCard)

        // Then
        expectThat(poolState).isEqualTo(expected)
    }

    companion object {
        @JvmStatic
        fun poolStateCases() = streamOf(
            Arguments.of(null, false, false, VocabularyPoolState.NotInPool),
            argumentSet("not in pool, shared forms card", null, false, true, VocabularyPoolState.NotInPool),
            argumentSet("queued, no shared forms card", QuizPoolStatus.QUEUED, false, false, VocabularyPoolState.Queued),
            argumentSet("queued, shared forms card", QuizPoolStatus.QUEUED, false, true, VocabularyPoolState.Queued),
            argumentSet("paused with all its cards", QuizPoolStatus.PAUSED, true, true, VocabularyPoolState.Paused),
            Arguments.of(QuizPoolStatus.ACTIVE, false, false, active(SV_DE)),
            Arguments.of(QuizPoolStatus.ACTIVE, true, false, active(SV_DE, DE_SV)),
            Arguments.of(QuizPoolStatus.ACTIVE, true, true, active(SV_DE, DE_SV, FORMS)),
            argumentSet("after a reset, forms without DE_SV", QuizPoolStatus.ACTIVE, false, true, active(SV_DE, FORMS)),
        )

        private fun active(vararg facets: QuizFacet) = VocabularyPoolState.Active(facets.toSet())
    }
}
