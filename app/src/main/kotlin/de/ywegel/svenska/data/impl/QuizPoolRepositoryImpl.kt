package de.ywegel.svenska.data.impl

import de.ywegel.svenska.data.QuizPoolRepository
import de.ywegel.svenska.data.db.QuizPoolDao
import de.ywegel.svenska.data.db.quiz.QuizPoolEntry
import de.ywegel.svenska.data.model.quiz.QuizPoolStatus
import javax.inject.Inject

class QuizPoolRepositoryImpl @Inject constructor(
    private val dao: QuizPoolDao,
) : QuizPoolRepository {

    override suspend fun removeFromPool(entry: QuizPoolEntry) = when (entry.poolStatus) {
        QuizPoolStatus.QUEUED -> dao.delete(entry)
        QuizPoolStatus.ACTIVE -> dao.update(entry.pause())
        QuizPoolStatus.PAUSED -> Unit
    }
}
