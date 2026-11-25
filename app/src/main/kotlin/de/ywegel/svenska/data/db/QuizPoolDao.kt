package de.ywegel.svenska.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import de.ywegel.svenska.data.db.quiz.QuizPoolEntry
import de.ywegel.svenska.data.model.quiz.PoolRemoval
import de.ywegel.svenska.data.model.quiz.QuizPoolStatus

@Dao
abstract class QuizPoolDao {
    @Insert
    abstract suspend fun insert(entry: QuizPoolEntry)

    @Update
    abstract suspend fun update(entry: QuizPoolEntry)

    @Delete
    abstract suspend fun delete(entry: QuizPoolEntry)

    @Query("SELECT * FROM QuizPoolEntry WHERE vocabularyId = :vocabularyId")
    abstract suspend fun get(vocabularyId: Int): QuizPoolEntry?

    // Reads and writes in one transaction, so that the decision is made on the current state of the row.
    @Transaction
    open suspend fun removeFromPool(vocabularyId: Int): PoolRemoval {
        val entry = get(vocabularyId) ?: return PoolRemoval.UNCHANGED
        return when (entry.poolStatus) {
            QuizPoolStatus.QUEUED -> {
                delete(entry)
                PoolRemoval.DELETED
            }

            QuizPoolStatus.ACTIVE -> {
                update(entry.pause())
                PoolRemoval.PAUSED
            }

            QuizPoolStatus.PAUSED -> PoolRemoval.UNCHANGED
        }
    }
}
