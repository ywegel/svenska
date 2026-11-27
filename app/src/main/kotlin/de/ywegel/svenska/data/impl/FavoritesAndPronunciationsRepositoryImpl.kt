package de.ywegel.svenska.data.impl

import de.ywegel.svenska.data.FavoritesAndPronunciationsRepository
import de.ywegel.svenska.data.db.VocabularyDao
import de.ywegel.svenska.data.model.Vocabulary
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FavoritesAndPronunciationsRepositoryImpl @Inject constructor(private val dao: VocabularyDao) :
    FavoritesAndPronunciationsRepository {
    override fun getFavorites(containerId: Int?): Flow<List<Vocabulary>> = if (containerId == null) {
        dao.getAllFavorites()
    } else {
        dao.getFavoritesByContainerId(containerId)
    }

    override fun getPronunciations(containerId: Int?): Flow<List<Vocabulary>> = if (containerId == null) {
        dao.getAllPronunciations()
    } else {
        dao.getPronunciationsByContainerId(containerId)
    }
}
