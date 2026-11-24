package de.ywegel.svenska.data

import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.quiz.QuizFacet
import one.felsen.fsrskt.fsrs6.FsrsCard
import one.felsen.fsrskt.fsrs6.FsrsReviewLog
import kotlin.time.Instant

interface QuizCardRepository {
    /**
     * The card of a facet of [vocabulary], `null` while the facet is locked.
     */
    suspend fun getCard(vocabulary: Vocabulary, facet: QuizFacet): FsrsCard?

    /**
     * Saves a review in one transaction: the reviewed [card], its [log], the SV_DE streak and a newly unlocked DE_SV
     * card. Does nothing if the card no longer exists, e.g. after a reset during the session.
     *
     * @param passedSvDe Whether an SV_DE review passed, `null` for the other facets, which keep the streak.
     * @param unlockDeSvDue When the newly unlocked DE_SV card is due, `null` if nothing is unlocked. An existing DE_SV
     * card keeps its progress.
     */
    suspend fun saveReview(
        vocabulary: Vocabulary,
        facet: QuizFacet,
        card: FsrsCard,
        log: FsrsReviewLog,
        rawAnswer: String,
        passedSvDe: Boolean?,
        unlockDeSvDue: Instant?,
    )
}
