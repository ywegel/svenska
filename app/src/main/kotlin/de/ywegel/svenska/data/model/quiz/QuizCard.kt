package de.ywegel.svenska.data.model.quiz

import de.ywegel.svenska.data.model.Vocabulary
import one.felsen.fsrskt.fsrs6.FsrsCard

/**
 * An unlocked facet card as domain and UI see it: which card it is and its FSRS state.
 *
 * Read with the quiz card repository and passed back with a new [fsrs] after a review, so that the repository knows
 * which card to write without looking it up again.
 *
 * @property vocabulary The row that is quizzed. For [QuizFacet.FORMS] the row the card was read for, one of the rows
 * that share it via their [formsKey]. The review log of the card belongs to this row.
 * @property facet What is quizzed.
 * @property fsrs The FSRS state of the card.
 */
data class QuizCard(
    val vocabulary: Vocabulary,
    val facet: QuizFacet,
    val fsrs: FsrsCard,
) {
    init {
        require(facet != QuizFacet.FORMS || vocabulary.formsKey != null) {
            "A FORMS card needs a vocabulary with forms, but was $this."
        }
    }
}
