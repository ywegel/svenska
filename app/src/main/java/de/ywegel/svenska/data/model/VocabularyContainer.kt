package de.ywegel.svenska.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class VocabularyContainer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
)
