package de.ywegel.svenska.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import de.ywegel.svenska.data.model.VocabularyContainer
import kotlinx.coroutines.flow.Flow

@Dao
interface ContainerDao {
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

    @Query("SELECT id, name FROM vocabularycontainer ORDER BY id ASC")
    fun getAllContainerNamesWithId(): List<VocabularyContainer>
}
