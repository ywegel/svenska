package de.ywegel.svenska.data.impl

import de.ywegel.svenska.data.QuizCardRepository
import de.ywegel.svenska.data.db.ReviewDao
import de.ywegel.svenska.data.db.quiz.FacetCard
import de.ywegel.svenska.data.db.quiz.toTranslationDirection
import de.ywegel.svenska.data.impl.quiz.toFsrsCard
import de.ywegel.svenska.data.impl.quiz.toFsrsColumns
import de.ywegel.svenska.data.impl.quiz.toReviewLog
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.quiz.QuizFacet
import de.ywegel.svenska.data.model.quiz.SvDeOutcome
import de.ywegel.svenska.data.model.quiz.formsKey
import one.felsen.fsrskt.fsrs6.FsrsCard
import one.felsen.fsrskt.fsrs6.FsrsReviewLog
import javax.inject.Inject

class QuizCardRepositoryImpl @Inject constructor(
    private val reviewDao: ReviewDao,
) : QuizCardRepository {

    override suspend fun getCard(vocabulary: Vocabulary, facet: QuizFacet): FsrsCard? =
        findCard(vocabulary, facet)?.fsrs?.toFsrsCard()

    override suspend fun saveReview(
        vocabulary: Vocabulary,
        facet: QuizFacet,
        card: FsrsCard,
        log: FsrsReviewLog,
        rawAnswer: String,
        svDe: SvDeOutcome?,
    ) {
        val stored = findCard(vocabulary, facet) ?: return
        reviewDao.saveReview(
            card = stored.withFsrs(card.toFsrsColumns()),
            log = log.toReviewLog(vocabulary, facet, rawAnswer),
            svDe = svDe,
        )
    }

    private suspend fun findCard(vocabulary: Vocabulary, facet: QuizFacet): FacetCard? =
        when (val direction = facet.toTranslationDirection()) {
            null -> vocabulary.formsKey?.let { reviewDao.getFormsCard(it) }
            else -> reviewDao.getTranslationCard(vocabulary.id, direction)
        }
}
