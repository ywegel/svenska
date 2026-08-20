package de.ywegel.svenska.data.impl

import de.ywegel.svenska.data.QuizRepository
import de.ywegel.svenska.data.db.QuizDao
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class QuizRepositoryImpl @Inject constructor(
    private val dao: QuizDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : QuizRepository {
    override suspend fun getAllNouns(containerId: Int?): List<Vocabulary> = withContext(ioDispatcher) {
        return@withContext containerId?.let { dao.getAllNouns(it) } ?: dao.getAllNouns()
    }
}
