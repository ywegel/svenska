package de.ywegel.svenska.data

import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.quiz.QuizFacet
import one.felsen.fsrskt.fsrs6.FsrsCard
import one.felsen.fsrskt.fsrs6.FsrsReviewLog

interface ReviewRepository {
    // null = the card is not unlocked yet
    suspend fun getCard(vocabulary: Vocabulary, facet: QuizFacet): FsrsCard?

    // Writes the card, the log and, for SV_DE, the streak in one transaction
    suspend fun saveReview(
        vocabulary: Vocabulary,
        facet: QuizFacet,
        card: FsrsCard,
        log: FsrsReviewLog,
        rawAnswer: String,
    )
}
