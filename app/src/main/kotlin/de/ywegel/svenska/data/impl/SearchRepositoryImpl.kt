package de.ywegel.svenska.data.impl

import de.ywegel.svenska.data.SearchRepository
import de.ywegel.svenska.data.db.SearchDao
import de.ywegel.svenska.data.model.Vocabulary
import kotlinx.coroutines.flow.Flow

class SearchRepositoryImpl(private val searchDao: SearchDao) : SearchRepository {
    override fun searchVocabularies(query: String, containerId: Int?): Flow<List<Vocabulary>> {
        return containerId?.let {
            searchDao.searchVocabulariesById(it, query)
        } ?: searchDao.searchVocabularies(query)
    }
}
