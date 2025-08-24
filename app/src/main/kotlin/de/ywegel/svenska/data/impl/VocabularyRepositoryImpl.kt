package de.ywegel.svenska.data.impl

import de.ywegel.svenska.data.SortOrder
import de.ywegel.svenska.data.VocabularyRepository
import de.ywegel.svenska.data.db.VocabularyDao
import de.ywegel.svenska.data.model.Vocabulary
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VocabularyRepositoryImpl @Inject constructor(private val dao: VocabularyDao) : VocabularyRepository {

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

    override suspend fun toggleVocabularyFavorite(vocabularyId: Int, isFavorite: Boolean) {
        dao.toggleVocabularyFavorite(vocabularyId, isFavorite)
    }

    override suspend fun getAllVocabulariesWithEndings(containerId: Int?): List<Vocabulary> {
        return containerId?.let { id ->
            dao.getAllVocabulariesWithEndings(id)
        } ?: dao.getAllVocabulariesWithEndings()
    }
}
