package de.ywegel.svenska.data.impl

import de.ywegel.svenska.data.QuizCardRepository
import de.ywegel.svenska.data.db.ReviewDao
import de.ywegel.svenska.data.db.quiz.toTranslationDirection
import de.ywegel.svenska.data.impl.quiz.toFacetCard
import de.ywegel.svenska.data.impl.quiz.toQuizCard
import de.ywegel.svenska.data.impl.quiz.toReviewLog
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.quiz.QuizCard
import de.ywegel.svenska.data.model.quiz.QuizFacet
import de.ywegel.svenska.data.model.quiz.SvDeOutcome
import de.ywegel.svenska.data.model.quiz.formsKey
import one.felsen.fsrskt.fsrs6.FsrsReviewLog
import javax.inject.Inject

class QuizCardRepositoryImpl @Inject constructor(
    private val reviewDao: ReviewDao,
) : QuizCardRepository {

    override suspend fun getCard(vocabulary: Vocabulary, facet: QuizFacet): QuizCard? =
        when (val direction = facet.toTranslationDirection()) {
            null -> vocabulary.formsKey?.let { reviewDao.getFormsCard(it) }?.toQuizCard(vocabulary)
            else -> reviewDao.getTranslationCard(vocabulary.id, direction)?.toQuizCard(vocabulary)
        }

    override suspend fun saveReview(card: QuizCard, log: FsrsReviewLog, rawAnswer: String, svDe: SvDeOutcome?) =
        reviewDao.saveReview(card.toFacetCard(), log.toReviewLog(card, rawAnswer), svDe)
}
