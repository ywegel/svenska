package de.ywegel.svenska.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import de.ywegel.svenska.data.db.quiz.FacetCard
import de.ywegel.svenska.data.db.quiz.FormsFacetCard
import de.ywegel.svenska.data.db.quiz.QuizPoolEntry
import de.ywegel.svenska.data.db.quiz.QuizTranslationDirection
import de.ywegel.svenska.data.db.quiz.ReviewLog
import de.ywegel.svenska.data.db.quiz.TranslationFacetCard

@Dao
abstract class ReviewDao {
    @Query("SELECT * FROM TranslationFacetCard WHERE vocabularyId = :vocabularyId AND direction = :direction")
    abstract suspend fun getTranslationCard(
        vocabularyId: Int,
        direction: QuizTranslationDirection,
    ): TranslationFacetCard?

    @Query("SELECT * FROM FormsFacetCard WHERE formsKey = :formsKey")
    abstract suspend fun getFormsCard(formsKey: String): FormsFacetCard?

    /**
     * The logs of a row that still count for its cards, oldest first. A reset of the row ([QuizPoolEntry.resetAt])
     * drops its translation logs before the reset. Forms logs always count, as the reset keeps the shared forms card.
     */
    @Query(
        """
        SELECT ReviewLog.* FROM ReviewLog
        LEFT JOIN QuizPoolEntry ON QuizPoolEntry.vocabularyId = ReviewLog.vocabularyId
        WHERE ReviewLog.vocabularyId = :vocabularyId
            AND (ReviewLog.facet = 'FORMS' OR QuizPoolEntry.resetAt IS NULL OR ReviewLog.reviewAt >= QuizPoolEntry.resetAt)
        ORDER BY ReviewLog.reviewAt, ReviewLog.id
        """,
    )
    abstract suspend fun getLogs(vocabularyId: Int): List<ReviewLog>

    @Update
    protected abstract suspend fun updateTranslationCard(card: TranslationFacetCard)

    @Update
    protected abstract suspend fun updateFormsCard(card: FormsFacetCard)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertTranslationCardIfAbsent(card: TranslationFacetCard)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertFormsCardIfAbsent(card: FormsFacetCard)

    @Insert
    protected abstract suspend fun insertLog(log: ReviewLog)

    @Query("UPDATE QuizPoolEntry SET svDeStreak = :streak WHERE vocabularyId = :vocabularyId")
    protected abstract suspend fun updateSvDeStreak(vocabularyId: Int, streak: Int)

    @Transaction
    open suspend fun saveReview(
        card: FacetCard,
        svDeStreak: Int?,
        log: ReviewLog,
        unlockedDeSv: TranslationFacetCard?,
    ) {
        when (card) {
            is FormsFacetCard -> updateFormsCard(card)
            is TranslationFacetCard -> updateTranslationCard(card)
        }
        insertLog(log)
        svDeStreak?.let { updateSvDeStreak(log.vocabularyId, it) }
        unlockedDeSv?.let { insertTranslationCardIfAbsent(it) }
    }
}
