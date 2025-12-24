package de.ywegel.svenska.data.db

import androidx.room.Dao
import androidx.room.Query
import de.ywegel.svenska.data.model.Vocabulary

@Dao
interface QuizDao {
    @Query("SELECT * from vocabulary where containerId = :containerId AND wordGroup LIKE 'NOUN:' || '%'")
    fun getAllNouns(containerId: Int): List<Vocabulary>

    @Query("SELECT * from vocabulary where wordGroup LIKE 'NOUN:' || '%'")
    fun getAllNouns(): List<Vocabulary>
}
