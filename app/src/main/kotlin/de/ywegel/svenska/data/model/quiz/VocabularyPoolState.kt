package de.ywegel.svenska.data.model.quiz

/**
 * The quiz pool state of one vocabulary row, as the vocabulary list shows it.
 *
 * - [NotInPool]: the row has no pool entry.
 * - [Queued], [Paused]: see [QuizPoolStatus].
 * - [Active]: introduced and not paused. [Active.unlocked] holds every facet whose card exists. The facets don't always
 *   build on each other: A reset deletes the DE_SV card, but keeps the shared forms card.
 */
sealed interface VocabularyPoolState {
    data object NotInPool : VocabularyPoolState
    data object Queued : VocabularyPoolState
    data object Paused : VocabularyPoolState
    data class Active(val unlocked: Set<QuizFacet>) : VocabularyPoolState

    companion object {
        /**
         * A facet is unlocked if its card exists
         */
        fun of(
            status: QuizPoolStatus?,
            hasDeSvCard: Boolean,
            hasFormsCard: Boolean,
        ): VocabularyPoolState = when (status) {
            null -> NotInPool
            QuizPoolStatus.QUEUED -> Queued
            QuizPoolStatus.PAUSED -> Paused
            QuizPoolStatus.ACTIVE -> Active(
                buildSet {
                    add(QuizFacet.SV_DE)
                    if (hasDeSvCard) add(QuizFacet.DE_SV)
                    if (hasFormsCard) add(QuizFacet.FORMS)
                },
            )
        }
    }
}
