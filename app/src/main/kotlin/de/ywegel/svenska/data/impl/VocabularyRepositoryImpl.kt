package de.ywegel.svenska.data.impl

import de.ywegel.svenska.data.VocabularyRepository
import de.ywegel.svenska.data.db.VocabularyDao
import de.ywegel.svenska.data.model.SortOrder
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class VocabularyRepositoryImpl @Inject constructor(
    private val dao: VocabularyDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : VocabularyRepository {

    override fun getVocabularies(containerId: Int, sortOrder: SortOrder, reverse: Boolean): Flow<List<Vocabulary>> {
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

    override suspend fun getAllVocabulariesSnapshot(containerId: Int?): List<Vocabulary> = withContext(ioDispatcher) {
        return@withContext containerId?.let { id ->
            dao.getAllVocabulariesSnapshot(id)
        } ?: dao.getAllVocabulariesSnapshot()
    }

    override suspend fun deleteVocabulary(vocabulary: Vocabulary) = withContext(ioDispatcher) {
        dao.deleteVocabulary(vocabulary)
    }

    override suspend fun upsertVocabulary(vocabulary: Vocabulary): Long = withContext(ioDispatcher) {
        return@withContext dao.upsertVocabulary(vocabulary)
    }

    override suspend fun toggleVocabularyFavorite(vocabularyId: Int, isFavorite: Boolean) = withContext(ioDispatcher) {
        dao.toggleVocabularyFavorite(vocabularyId, isFavorite)
    }

    override suspend fun getAllVocabulariesWithEndings(containerId: Int?): List<Vocabulary> =
        withContext(ioDispatcher) {
            return@withContext containerId?.let { id ->
                dao.getAllVocabulariesWithEndings(id)
            } ?: dao.getAllVocabulariesWithEndings()
        }
}
