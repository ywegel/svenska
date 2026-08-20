package de.ywegel.svenska.data

import de.ywegel.svenska.data.model.Vocabulary

interface FavoritesAndPronunciationsRepository {
    suspend fun getFavorites(containerId: Int?): List<Vocabulary>

    suspend fun getPronunciations(containerId: Int?): List<Vocabulary>
}
