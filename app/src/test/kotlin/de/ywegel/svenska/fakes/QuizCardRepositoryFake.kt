package de.ywegel.svenska.fakes

import de.ywegel.svenska.data.QuizCardRepository
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.quiz.QuizFacet
import de.ywegel.svenska.data.model.quiz.SvDeOutcome
import one.felsen.fsrskt.fsrs6.FsrsCard
import one.felsen.fsrskt.fsrs6.FsrsReviewLog

/**
 * Holds the unlocked cards by vocabulary id and facet, and records every saved review in [savedReviews].
 */
class QuizCardRepositoryFake(
    vararg unlockedCards: Pair<Pair<Int, QuizFacet>, FsrsCard>,
) : QuizCardRepository {
    private val cards = unlockedCards.toMap().toMutableMap()

    val savedReviews = mutableListOf<SavedReview>()

    override suspend fun getCard(vocabulary: Vocabulary, facet: QuizFacet): FsrsCard? = cards[vocabulary.id to facet]

    override suspend fun saveReview(
        vocabulary: Vocabulary,
        facet: QuizFacet,
        card: FsrsCard,
        log: FsrsReviewLog,
        rawAnswer: String,
        svDe: SvDeOutcome?,
    ) {
        cards[vocabulary.id to facet] = card
        savedReviews += SavedReview(vocabulary.id, facet, card, log, rawAnswer, svDe)
    }

    data class SavedReview(
        val vocabularyId: Int,
        val facet: QuizFacet,
        val card: FsrsCard,
        val log: FsrsReviewLog,
        val rawAnswer: String,
        val svDe: SvDeOutcome?,
    )
}
