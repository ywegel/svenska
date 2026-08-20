package de.ywegel.svenska.data.impl

import de.ywegel.svenska.data.FavoritesAndPronunciationsRepository
import de.ywegel.svenska.data.db.VocabularyDao
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FavoritesAndPronunciationsRepositoryImpl @Inject constructor(
    private val dao: VocabularyDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) :
    FavoritesAndPronunciationsRepository {
    override suspend fun getFavorites(containerId: Int?): List<Vocabulary> = withContext(ioDispatcher) {
        return@withContext if (containerId == null) {
            dao.getAllFavorites()
        } else {
            dao.getFavoritesByContainerId(containerId)
        }
    }

    override suspend fun getPronunciations(containerId: Int?): List<Vocabulary> = withContext(ioDispatcher) {
        return@withContext if (containerId == null) {
            dao.getAllPronunciations()
        } else {
            dao.getPronunciationsByContainerId(containerId)
        }
    }
}
