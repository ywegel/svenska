package de.ywegel.svenska.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import de.ywegel.svenska.data.model.Vocabulary
import kotlinx.coroutines.flow.Flow

@Suppress(
    "ktlint:standard:max-line-length",
    "ktlint:standard:argument-list-wrapping",
    "ktlint:standard:parameter-list-wrapping",
    "detekt:MaxLineLength",
    "detekt:TooManyFunctions",
)
@Dao
interface VocabularyDao {
    // Vocabulary operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVocabulary(vocabulary: Vocabulary): Long

    @Delete
    suspend fun deleteVocabulary(vocabulary: Vocabulary)

    @Upsert
    suspend fun upsertVocabulary(vocabulary: Vocabulary): Long

    @Query("SELECT * FROM vocabulary WHERE id = :vocabularyId")
    suspend fun getVocabularyById(vocabularyId: Int): Vocabulary?

    @Query("SELECT * FROM vocabulary")
    suspend fun getAllVocabulariesSnapshot(): List<Vocabulary>

    @Query("SELECT * FROM vocabulary WHERE containerId = :containerId")
    suspend fun getAllVocabulariesSnapshot(containerId: Int): List<Vocabulary>

    @Query("UPDATE vocabulary SET isFavorite = :isFavorite WHERE id = :id ")
    suspend fun toggleVocabularyFavorite(id: Int, isFavorite: Boolean)

    @Query("SELECT isFavorite FROM vocabulary WHERE id = :vocabularyId")
    fun isVocabularyFavorite(vocabularyId: Int): Flow<Boolean>

    // Combined operations
    @Query("SELECT * FROM vocabulary ORDER BY created ASC")
    fun getVocabularies(): Flow<List<Vocabulary>>

    @Query("SELECT * FROM vocabulary WHERE containerId = :containerId ORDER BY created ASC")
    fun getVocabularies(containerId: Int): Flow<List<Vocabulary>>

    @Query("SELECT * FROM vocabulary WHERE containerId = :containerId ORDER BY created ASC")
    fun getVocabulariesByCreatedASC(containerId: Int): Flow<List<Vocabulary>>

    @Query("SELECT * FROM vocabulary WHERE containerId = :containerId ORDER BY created DESC")
    fun getVocabulariesByCreatedDESC(containerId: Int): Flow<List<Vocabulary>>

    @Query("SELECT * FROM vocabulary WHERE containerId = :containerId ORDER BY lastEdited ASC")
    fun getVocabulariesByEditedASC(containerId: Int): Flow<List<Vocabulary>>

    @Query("SELECT * FROM vocabulary WHERE containerId = :containerId ORDER BY lastEdited DESC")
    fun getVocabulariesByEditedDESC(containerId: Int): Flow<List<Vocabulary>>

    @Query("SELECT * FROM vocabulary WHERE containerId = :containerId ORDER BY word ASC")
    fun getVocabulariesByWordASC(containerId: Int): Flow<List<Vocabulary>>

    @Query("SELECT * FROM vocabulary WHERE containerId = :containerId ORDER BY word DESC")
    fun getVocabulariesByWordDESC(containerId: Int): Flow<List<Vocabulary>>

    @Query("SELECT * FROM vocabulary WHERE containerId = :containerId ORDER BY translation ASC")
    fun getVocabulariesByTranslationASC(containerId: Int): Flow<List<Vocabulary>>

    @Query("SELECT * FROM vocabulary WHERE containerId = :containerId ORDER BY translation DESC")
    fun getVocabulariesByTranslationDESC(containerId: Int): Flow<List<Vocabulary>>

    @Query("SELECT * FROM vocabulary WHERE containerId = :containerId ORDER BY created ASC")
    fun getVocabulariesByCustomASC(containerId: Int): Flow<List<Vocabulary>> // TODO implement custom sorting

    @Query("SELECT * FROM vocabulary WHERE containerId = :containerId ORDER BY created DESC")
    fun getVocabulariesByCustomDESC(containerId: Int): Flow<List<Vocabulary>> // TODO implement custom sorting. This requires adjusting the Repository Fake!

    @Query("SELECT * FROM vocabulary WHERE isFavorite IS 1 AND containerId = :containerId ORDER BY word ASC")
    fun getFavoritesByContainerId(containerId: Int): List<Vocabulary>

    @Query("SELECT * FROM vocabulary WHERE isFavorite IS 1 ORDER BY word ASC")
    fun getAllFavorites(): List<Vocabulary>

    @Query("SELECT * FROM vocabulary WHERE irregularPronunciation IS NOT NULL AND containerId = :containerId ORDER BY word ASC")
    fun getPronunciationsByContainerId(containerId: Int): List<Vocabulary>

    @Query("SELECT * FROM vocabulary WHERE irregularPronunciation IS NOT NULL ORDER BY word ASC")
    fun getAllPronunciations(): List<Vocabulary>

    @Query("SELECT * FROM vocabulary WHERE ending IS NOT NULL AND TRIM(ending, '') != ''")
    suspend fun getAllVocabulariesWithEndings(): List<Vocabulary>

    @Query("SELECT * FROM vocabulary WHERE containerId = :containerId AND ending IS NOT NULL AND TRIM(ending, '') != ''")
    suspend fun getAllVocabulariesWithEndings(containerId: Int): List<Vocabulary>
}
