@file:Suppress("TooManyFunctions")

package de.ywegel.svenska.data

import de.ywegel.svenska.data.model.Vocabulary
import kotlinx.coroutines.flow.Flow

interface VocabularyRepository {

    fun getVocabularies(containerId: Int, sortOrder: SortOrder, reverse: Boolean = false): Flow<List<Vocabulary>>

    suspend fun getAllVocabulariesSnapshot(containerId: Int?): List<Vocabulary>

    suspend fun deleteVocabulary(vocabulary: Vocabulary)

    suspend fun upsertVocabulary(vocabulary: Vocabulary): Long

    suspend fun getVocabularyById(id: Int): Vocabulary?

    suspend fun toggleVocabularyFavorite(vocabularyId: Int, isFavorite: Boolean)

    fun isVocabularyFavorite(vocabularyId: Int): Flow<Boolean>

    suspend fun getAllVocabulariesWithEndings(containerId: Int?): List<Vocabulary>
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
