@file:Suppress("TooManyFunctions")

package de.ywegel.svenska.data

import de.ywegel.svenska.data.model.SortOrder
import de.ywegel.svenska.data.model.Vocabulary
import kotlinx.coroutines.flow.Flow

interface VocabularyRepository {

    fun getVocabularies(containerId: Int, sortOrder: SortOrder, reverse: Boolean = false): Flow<List<Vocabulary>>

    suspend fun getAllVocabulariesSnapshot(containerId: Int?): List<Vocabulary>

    suspend fun deleteVocabulary(vocabulary: Vocabulary)

    suspend fun upsertVocabulary(vocabulary: Vocabulary): Long

    suspend fun toggleVocabularyFavorite(vocabularyId: Int, isFavorite: Boolean)

    suspend fun getAllVocabulariesWithEndings(containerId: Int?): List<Vocabulary>
}
