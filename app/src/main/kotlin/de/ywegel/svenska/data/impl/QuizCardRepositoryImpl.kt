package de.ywegel.svenska.data.impl

import de.ywegel.svenska.data.QuizCardRepository
import de.ywegel.svenska.data.db.ReviewDao
import de.ywegel.svenska.data.db.quiz.FacetCard
import de.ywegel.svenska.data.db.quiz.ReviewLog
import de.ywegel.svenska.data.db.quiz.TranslationFacetCard
import javax.inject.Inject

class QuizCardRepositoryImpl @Inject constructor(
    private val reviewDao: ReviewDao,
) : QuizCardRepository {

    override suspend fun saveReview(
        card: FacetCard,
        svDeStreak: Int?,
        log: ReviewLog,
        unlockedDeSv: TranslationFacetCard?,
    ) = reviewDao.saveReview(card, svDeStreak, log, unlockedDeSv)
}
