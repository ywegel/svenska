package de.ywegel.svenska.data

import de.ywegel.svenska.data.model.VocabularyContainer
import kotlinx.coroutines.flow.Flow

interface ContainerRepository {
    fun getAllContainers(): Flow<List<VocabularyContainer>>

    suspend fun getContainerById(id: Int): VocabularyContainer?

    suspend fun upsertContainer(container: VocabularyContainer): Long

    suspend fun deleteContainerWithAllVocabulary(container: VocabularyContainer)
}
