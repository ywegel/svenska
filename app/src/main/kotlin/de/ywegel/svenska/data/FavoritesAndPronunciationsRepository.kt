package de.ywegel.svenska.data

import de.ywegel.svenska.data.model.Vocabulary

interface FavoritesAndPronunciationsRepository {
    fun getFavorites(containerId: Int?): List<Vocabulary>

    fun getPronunciations(containerId: Int?): List<Vocabulary>
}
