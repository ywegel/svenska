package de.ywegel.svenska.data.impl

import de.ywegel.svenska.data.ContainerRepository
import de.ywegel.svenska.data.db.ContainerDao
import de.ywegel.svenska.data.model.VocabularyContainer
import de.ywegel.svenska.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ContainerRepositoryImpl @Inject constructor(
    private val containerDao: ContainerDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ContainerRepository {
    override suspend fun getContainerById(id: Int): VocabularyContainer? = withContext(ioDispatcher) {
        return@withContext containerDao.getContainerById(id)
    }

    override suspend fun upsertContainer(container: VocabularyContainer): Long = withContext(ioDispatcher) {
        return@withContext containerDao.upsertContainer(container)
    }

    override suspend fun deleteContainerWithAllVocabulary(container: VocabularyContainer) = withContext(ioDispatcher) {
        containerDao.deleteContainerWithVocabulary(container)
    }

    override fun getAllContainers(): Flow<List<VocabularyContainer>> {
        return containerDao.getAllContainers()
    }
}
