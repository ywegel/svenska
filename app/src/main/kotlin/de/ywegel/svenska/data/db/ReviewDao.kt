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

    // Counted in SQL, so that several reviews of a word in one session build on each other
    @Query(
        """
        UPDATE QuizPoolEntry SET svDeStreak = CASE WHEN :passed THEN svDeStreak + 1 ELSE 0 END
        WHERE vocabularyId = :vocabularyId
        """,
    )
    protected abstract suspend fun updateSvDeStreak(vocabularyId: Int, passed: Boolean)

    /**
     * Saves a review of [card] in one transaction.
     *
     * @param passedSvDe Whether an SV_DE review passed, `null` for the other facets, which keep the streak.
     * @param unlockedDeSv A newly unlocked DE_SV card. An existing DE_SV card keeps its progress.
     */
    @Transaction
    open suspend fun saveReview(
        card: FacetCard,
        passedSvDe: Boolean?,
        log: ReviewLog,
        unlockedDeSv: TranslationFacetCard?,
    ) {
        when (card) {
            is FormsFacetCard -> updateFormsCard(card)
            is TranslationFacetCard -> updateTranslationCard(card)
        }
        insertLog(log)
        passedSvDe?.let { updateSvDeStreak(log.vocabularyId, it) }
        unlockedDeSv?.let { insertTranslationCardIfAbsent(it) }
    }
}
