package de.ywegel.svenska.domain

import de.ywegel.svenska.data.VocabularyRepository
import de.ywegel.svenska.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ToggleVocabularyFavoriteUseCase @Inject constructor(
    private val repository: VocabularyRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(vocabularyId: Int, isFavorite: Boolean) {
        withContext(ioDispatcher) {
            repository.toggleVocabularyFavorite(vocabularyId, isFavorite)
        }
    }
}