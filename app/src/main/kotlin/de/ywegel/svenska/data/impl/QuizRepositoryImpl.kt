package de.ywegel.svenska.data.impl

import de.ywegel.svenska.data.QuizRepository
import de.ywegel.svenska.data.db.QuizDao
import de.ywegel.svenska.data.model.Vocabulary

class QuizRepositoryImpl(private val dao: QuizDao) : QuizRepository {
    override suspend fun getAllNouns(containerId: Int?): List<Vocabulary> {
        return containerId?.let { dao.getAllNouns(it) } ?: dao.getAllNouns()
    }
}
