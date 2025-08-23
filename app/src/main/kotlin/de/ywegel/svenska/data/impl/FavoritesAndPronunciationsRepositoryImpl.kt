package de.ywegel.svenska.data.impl

import de.ywegel.svenska.data.FavoritesAndPronunciationsRepository
import de.ywegel.svenska.data.db.VocabularyDao
import de.ywegel.svenska.data.model.Vocabulary

class FavoritesAndPronunciationsRepositoryImpl(private val dao: VocabularyDao) :
    FavoritesAndPronunciationsRepository {
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
}
