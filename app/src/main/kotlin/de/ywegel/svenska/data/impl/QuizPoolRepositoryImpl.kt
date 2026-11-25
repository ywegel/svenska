package de.ywegel.svenska.data.impl

import de.ywegel.svenska.data.QuizPoolRepository
import de.ywegel.svenska.data.db.QuizPoolDao
import de.ywegel.svenska.data.model.quiz.PoolRemoval
import javax.inject.Inject

class QuizPoolRepositoryImpl @Inject constructor(
    private val dao: QuizPoolDao,
) : QuizPoolRepository {

    override suspend fun removeFromPool(vocabularyId: Int): PoolRemoval = dao.removeFromPool(vocabularyId)
}
