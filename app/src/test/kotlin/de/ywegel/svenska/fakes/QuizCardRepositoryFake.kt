package de.ywegel.svenska.fakes

import de.ywegel.svenska.data.QuizCardRepository
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.quiz.QuizCard
import de.ywegel.svenska.data.model.quiz.QuizFacet
import de.ywegel.svenska.data.model.quiz.SvDeOutcome
import one.felsen.fsrskt.fsrs6.FsrsReviewLog

/**
 * Holds the unlocked cards by vocabulary id and facet, and records every saved review in [savedReviews].
 */
class QuizCardRepositoryFake(vararg unlockedCards: QuizCard) : QuizCardRepository {
    private val cards = unlockedCards.associateBy { it.vocabularyId to it.facet }.toMutableMap()

    val savedReviews = mutableListOf<SavedReview>()

    override suspend fun getCard(vocabulary: Vocabulary, facet: QuizFacet): QuizCard? = cards[vocabulary.id to facet]

    override suspend fun saveReview(card: QuizCard, log: FsrsReviewLog, rawAnswer: String, svDe: SvDeOutcome?) {
        cards[card.vocabularyId to card.facet] = card
        savedReviews += SavedReview(card, log, rawAnswer, svDe)
    }

    data class SavedReview(
        val card: QuizCard,
        val log: FsrsReviewLog,
        val rawAnswer: String,
        val svDe: SvDeOutcome?,
    )
}
