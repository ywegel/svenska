package de.ywegel.svenska.data.model.quiz

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import de.ywegel.svenska.data.model.Vocabulary

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Vocabulary::class,
            parentColumns = ["id"],
            childColumns = ["vocabularyId"],
            onDelete = CASCADE,
        ),
    ],
    indices = [Index("vocabularyId", "facet"), Index("formsKey")],
)
data class ReviewLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vocabularyId: Int,
    val facet: QuizFacet,
    val formsKey: String?, // the shared forms card at the time of the review, as the forms can change later
    val rating: Int,
    val rawAnswer: String,
    val reviewAt: Long,
    val durationMillis: Long?,
) {
    init {
        require((facet == QuizFacet.FORMS) == (formsKey != null)) { "formsKey must be set exactly for FORMS, but was $this." }
    }
}