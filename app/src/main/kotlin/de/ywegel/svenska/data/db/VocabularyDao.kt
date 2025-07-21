package de.ywegel.svenska.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.VocabularyContainer
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

    // Container operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContainer(container: VocabularyContainer): Long

    @Upsert
    suspend fun upsertContainer(container: VocabularyContainer): Long

    @Query("SELECT * FROM vocabularycontainer WHERE id = :containerId")
    suspend fun getContainerById(containerId: Int): VocabularyContainer?

    @Query("SELECT * FROM vocabularycontainer")
    fun getAllContainers(): Flow<List<VocabularyContainer>>

    @Query("SELECT * FROM vocabularycontainer")
    fun getAllContainersSnapshot(): List<VocabularyContainer>

    @Query("DELETE FROM vocabulary WHERE containerId = :containerId")
    suspend fun deleteVocabularyByContainerId(containerId: Int)

    @Delete
    suspend fun deleteContainer(container: VocabularyContainer)

    @Transaction
    suspend fun deleteContainerWithVocabulary(container: VocabularyContainer) {
        deleteVocabularyByContainerId(container.id)
        deleteContainer(container)
    }

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

    @Query("SELECT * FROM vocabulary WHERE containerId = :containerId AND (word LIKE '%' || :searchQuery || '%' OR translation LIKE '%' || :searchQuery || '%')")
    fun searchVocabulariesById(containerId: Int, searchQuery: String): Flow<List<Vocabulary>>

    @Query("SELECT * FROM vocabulary WHERE (word LIKE '%' || :searchQuery || '%' OR translation LIKE '%' || :searchQuery || '%')")
    fun searchAllVocabularies(searchQuery: String): Flow<List<Vocabulary>>

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

    @Query("SELECT id, name FROM vocabularycontainer ORDER BY id ASC")
    fun getAllContainerNamesWithId(): List<VocabularyContainer>

    @Query("SELECT * FROM vocabulary WHERE isFavorite IS 1 AND containerId = :containerId ORDER BY word ASC")
    fun getFavoritesByContainerId(containerId: Int): List<Vocabulary>

    @Query("SELECT * FROM vocabulary WHERE isFavorite IS 1 ORDER BY word ASC")
    fun getAllFavorites(): List<Vocabulary>

    @Query("SELECT * FROM vocabulary WHERE irregularPronunciation IS NOT NULL AND containerId = :containerId ORDER BY word ASC")
    fun getPronunciationsByContainerId(containerId: Int): List<Vocabulary>

    @Query("SELECT * FROM vocabulary WHERE irregularPronunciation IS NOT NULL ORDER BY word ASC")
    fun getAllPronunciations(): List<Vocabulary>
}
