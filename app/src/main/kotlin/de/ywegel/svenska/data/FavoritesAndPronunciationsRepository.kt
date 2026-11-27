package de.ywegel.svenska.data

import de.ywegel.svenska.data.model.Vocabulary
import kotlinx.coroutines.flow.Flow

interface FavoritesAndPronunciationsRepository {
    fun getFavorites(containerId: Int?): Flow<List<Vocabulary>>

    fun getPronunciations(containerId: Int?): Flow<List<Vocabulary>>
}
