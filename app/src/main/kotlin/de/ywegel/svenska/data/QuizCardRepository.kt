package de.ywegel.svenska.data

import de.ywegel.svenska.data.db.quiz.FacetCard
import de.ywegel.svenska.data.db.quiz.ReviewLog
import de.ywegel.svenska.data.db.quiz.TranslationFacetCard

interface QuizCardRepository {
    suspend fun saveReview(card: FacetCard, svDeStreak: Int?, log: ReviewLog, unlockedDeSv: TranslationFacetCard?)
}
