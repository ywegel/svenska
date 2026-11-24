package de.ywegel.svenska.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import de.ywegel.svenska.data.db.quiz.QuizPoolEntry

@Dao
interface QuizPoolDao {
    @Insert
    suspend fun insert(entry: QuizPoolEntry)

    @Update
    suspend fun update(entry: QuizPoolEntry)

    @Query("SELECT * FROM QuizPoolEntry WHERE vocabularyId = :vocabularyId")
    suspend fun get(vocabularyId: Int): QuizPoolEntry?
}
