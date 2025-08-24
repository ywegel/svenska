package de.ywegel.svenska.data

import de.ywegel.svenska.data.model.Vocabulary
import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    fun searchVocabularies(query: String, containerId: Int?): Flow<List<Vocabulary>>
}
