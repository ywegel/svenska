package de.ywegel.svenska.data.impl

import de.ywegel.svenska.data.ContainerRepository
import de.ywegel.svenska.data.db.ContainerDao
import de.ywegel.svenska.data.model.VocabularyContainer
import kotlinx.coroutines.flow.Flow

class ContainerRepositoryImpl(private val containerDao: ContainerDao) : ContainerRepository {
    override suspend fun getContainerById(id: Int): VocabularyContainer? {
        return containerDao.getContainerById(id)
    }

    override suspend fun upsertContainer(container: VocabularyContainer): Long {
        return containerDao.upsertContainer(container)
    }

    override suspend fun deleteContainerWithAllVocabulary(container: VocabularyContainer) {
        containerDao.deleteContainerWithVocabulary(container)
    }

    override fun getAllContainers(): Flow<List<VocabularyContainer>> {
        return containerDao.getAllContainers()
    }
}
