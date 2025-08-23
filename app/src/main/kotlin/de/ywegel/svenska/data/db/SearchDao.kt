package de.ywegel.svenska.data.db

import androidx.room.Dao
import androidx.room.Query
import de.ywegel.svenska.data.model.Vocabulary
import kotlinx.coroutines.flow.Flow

@Suppress(
    "ktlint:standard:max-line-length",
    "ktlint:standard:argument-list-wrapping",
    "ktlint:standard:parameter-list-wrapping",
    "detekt:MaxLineLength",
    "detekt:TooManyFunctions",
)@Dao
interface SearchDao {
    @Query("SELECT * FROM vocabulary WHERE containerId = :containerId AND (word LIKE '%' || :searchQuery || '%' OR translation LIKE '%' || :searchQuery || '%')")
    fun searchVocabulariesById(containerId: Int, searchQuery: String): Flow<List<Vocabulary>>

    @Query("SELECT * FROM vocabulary WHERE (word LIKE '%' || :searchQuery || '%' OR translation LIKE '%' || :searchQuery || '%')")
    fun searchVocabularies(searchQuery: String): Flow<List<Vocabulary>>
}
