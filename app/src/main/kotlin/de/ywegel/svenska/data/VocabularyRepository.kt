@file:Suppress("TooManyFunctions")

package de.ywegel.svenska.data

import de.ywegel.svenska.data.db.VocabularyDao
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.VocabularyContainer
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface VocabularyRepository {

    fun getVocabularies(
        query: String,
        containerId: Int?,
        sortOrder: SortOrder,
        reverse: Boolean = false,
    ): Flow<List<Vocabulary>>

    suspend fun getAllVocabulariesSnapshot(containerId: Int?): List<Vocabulary>

    suspend fun deleteVocabulary(vocabulary: Vocabulary)

    suspend fun upsertVocabulary(vocabulary: Vocabulary): Long

    suspend fun getVocabularyById(id: Int): Vocabulary?

    suspend fun toggleVocabularyFavorite(vocabularyId: Int, isFavorite: Boolean)

    fun isVocabularyFavorite(vocabularyId: Int): Flow<Boolean>

    fun getAllContainers(): Flow<List<VocabularyContainer>>

    suspend fun getContainerById(id: Int): VocabularyContainer?

    suspend fun upsertContainer(container: VocabularyContainer): Long

    suspend fun deleteContainerWithAllVocabulary(container: VocabularyContainer)

    fun getFavorites(containerId: Int?): List<Vocabulary>

    fun getPronunciations(containerId: Int?): List<Vocabulary>

    fun getAllContainerNamesWithIds(): List<VocabularyContainer>

    suspend fun getAllVocabulariesWithEndings(containerId: Int?): List<Vocabulary>
}

@Singleton
class VocabularyRepositoryImpl @Inject constructor(private val dao: VocabularyDao) : VocabularyRepository {

    override fun getVocabularies(
        query: String,
        containerId: Int?,
        sortOrder: SortOrder,
        reverse: Boolean,
    ): Flow<List<Vocabulary>> {
        if (containerId == null) {
            return dao.searchAllVocabularies(query)
        }

        if (query.isNotBlank()) {
            return dao.searchVocabulariesById(containerId, query)
        }

        return if (!reverse) {
            when (sortOrder) {
                SortOrder.Word -> dao.getVocabulariesByWordASC(containerId)
                SortOrder.Translation -> dao.getVocabulariesByTranslationASC(containerId)
                SortOrder.Created -> dao.getVocabulariesByCreatedASC(containerId)
                SortOrder.LastEdited -> dao.getVocabulariesByEditedASC(containerId)
            }
        } else {
            when (sortOrder) {
                SortOrder.Word -> dao.getVocabulariesByWordDESC(containerId)
                SortOrder.Translation -> dao.getVocabulariesByTranslationDESC(containerId)
                SortOrder.Created -> dao.getVocabulariesByCreatedDESC(containerId)
                SortOrder.LastEdited -> dao.getVocabulariesByEditedDESC(containerId)
            }
        }
    }

    // TODO: Evaluate, if using withContext(ioDispatcher) here instead at each usage of the function
    override suspend fun getAllVocabulariesSnapshot(containerId: Int?): List<Vocabulary> {
        return containerId?.let { id ->
            dao.getAllVocabulariesSnapshot(id)
        } ?: dao.getAllVocabulariesSnapshot()
    }

    override suspend fun deleteVocabulary(vocabulary: Vocabulary) {
        dao.deleteVocabulary(vocabulary)
    }

    override suspend fun upsertVocabulary(vocabulary: Vocabulary): Long {
        return dao.upsertVocabulary(vocabulary)
    }

    override suspend fun getVocabularyById(id: Int) = dao.getVocabularyById(id)

    override fun getAllContainers(): Flow<List<VocabularyContainer>> {
        return dao.getAllContainers()
    }

    override suspend fun toggleVocabularyFavorite(vocabularyId: Int, isFavorite: Boolean) {
        dao.toggleVocabularyFavorite(vocabularyId, isFavorite)
    }

    override fun isVocabularyFavorite(vocabularyId: Int): Flow<Boolean> {
        return dao.isVocabularyFavorite(vocabularyId)
    }

    override suspend fun getContainerById(id: Int): VocabularyContainer? {
        return dao.getContainerById(id)
    }

    override suspend fun upsertContainer(container: VocabularyContainer): Long {
        return dao.upsertContainer(container)
    }

    override suspend fun deleteContainerWithAllVocabulary(container: VocabularyContainer) {
        dao.deleteContainerWithVocabulary(container)
    }

    override fun getAllContainerNamesWithIds(): List<VocabularyContainer> {
        return dao.getAllContainerNamesWithId()
    }

    override fun getFavorites(containerId: Int?): List<Vocabulary> {
        return if (containerId == null) {
            dao.getAllFavorites()
        } else {
            dao.getFavoritesByContainerId(containerId)
        }
    }

    override fun getPronunciations(containerId: Int?): List<Vocabulary> {
        return if (containerId == null) {
            dao.getAllPronunciations()
        } else {
            dao.getPronunciationsByContainerId(containerId)
        }
    }

    override suspend fun getAllVocabulariesWithEndings(containerId: Int?): List<Vocabulary> {
        return containerId?.let { id ->
            dao.getAllVocabulariesWithEndings(id)
        } ?: dao.getAllVocabulariesWithEndings()
    }
}

enum class SortOrder {
    Word,
    Translation,
    Created,
    LastEdited,
    ;

    companion object {
        val default = Created
    }
}
