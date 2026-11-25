package de.ywegel.svenska.data.model.quiz

import one.felsen.fsrskt.fsrs6.FsrsCard

/**
 * An unlocked facet card as domain and UI see it: which card it is and its FSRS state.
 *
 * Read with the quiz card repository and passed back with a new [fsrs] after a review, so that the repository knows
 * which card to write without looking it up again.
 *
 * @property vocabularyId The row that is quizzed. For [QuizFacet.FORMS] the row the card was read for, one of the rows
 * that share it. The review log of the card belongs to this row.
 * @property facet What is quizzed.
 * @property formsKey The shared forms card ([formsKey] of the row), set exactly for [QuizFacet.FORMS].
 * @property fsrs The FSRS state of the card.
 */
data class QuizCard(
    val vocabularyId: Int,
    val facet: QuizFacet,
    val formsKey: String?,
    val fsrs: FsrsCard,
) {
    init {
        require((facet == QuizFacet.FORMS) == (formsKey != null)) {
            "formsKey must be set exactly for FORMS, but was $this."
        }
    }
}
