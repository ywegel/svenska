package de.ywegel.svenska.data

import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.quiz.QuizCard
import de.ywegel.svenska.data.model.quiz.QuizFacet
import de.ywegel.svenska.data.model.quiz.SvDeOutcome
import one.felsen.fsrskt.fsrs6.FsrsReviewLog

interface QuizCardRepository {
    /**
     * The card of a facet of [vocabulary], `null` while the facet is locked.
     */
    suspend fun getCard(vocabulary: Vocabulary, facet: QuizFacet): QuizCard?

    /**
     * Saves a review in one transaction: the reviewed [card] with its new FSRS state, its [log] and the [svDe]
     * outcome. Does nothing if the card no longer exists, e.g. after a reset during the session.
     *
     * @param card The card from [getCard], with the FSRS state after the review.
     * @param svDe The streak and unlock of an SV_DE review, `null` for the other facets.
     */
    suspend fun saveReview(card: QuizCard, log: FsrsReviewLog, rawAnswer: String, svDe: SvDeOutcome?)
}
