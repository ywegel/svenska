package de.ywegel.svenska.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import de.ywegel.svenska.data.db.quiz.FacetCard
import de.ywegel.svenska.data.db.quiz.FormsFacetCard
import de.ywegel.svenska.data.db.quiz.FsrsColumns
import de.ywegel.svenska.data.db.quiz.QuizPoolEntry
import de.ywegel.svenska.data.db.quiz.ReviewLog
import de.ywegel.svenska.data.db.quiz.TranslationFacetCard
import de.ywegel.svenska.data.model.quiz.QuizPoolStatus
import de.ywegel.svenska.data.model.quiz.QuizTranslationDirection
import de.ywegel.svenska.data.model.quiz.SvDeOutcome

@Suppress("detekt:TooManyFunctions")
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
     * The translation cards of all [QuizPoolStatus.ACTIVE] words.
     */
    @Query(
        """
        SELECT TranslationFacetCard.* FROM TranslationFacetCard
        JOIN QuizPoolEntry ON QuizPoolEntry.vocabularyId = TranslationFacetCard.vocabularyId
        WHERE QuizPoolEntry.introducedAt IS NOT NULL AND NOT QuizPoolEntry.isPaused
        """,
    )
    abstract suspend fun getActiveTranslationCards(): List<TranslationFacetCard>

    // All of them, as the forms key of the rows is not stored, so the active ones can't be joined in SQL
    // TODO(formsKey column): Remove, replaced by joins in getActiveCards and getFormsCandidates
    @Query("SELECT * FROM FormsFacetCard")
    abstract suspend fun getAllFormsCards(): List<FormsFacetCard>

    /**
     * The translation logs of a row that still count for its cards, oldest first. A reset of the row
     * ([QuizPoolEntry.resetAt]) drops the logs before it. Forms logs are not included, as they belong to the shared
     * forms card and are found by its [ReviewLog.formsKey].
     */
    @Query(
        """
        SELECT ReviewLog.* FROM ReviewLog
        LEFT JOIN QuizPoolEntry ON QuizPoolEntry.vocabularyId = ReviewLog.vocabularyId
        WHERE ReviewLog.vocabularyId = :vocabularyId
            AND ReviewLog.facet != 'FORMS'
            AND (QuizPoolEntry.resetAt IS NULL OR ReviewLog.reviewAt >= QuizPoolEntry.resetAt)
        ORDER BY ReviewLog.reviewAt, ReviewLog.id
        """,
    )
    abstract suspend fun getLogs(vocabularyId: Int): List<ReviewLog>

    // Returns the number of updated rows, 0 if the card no longer exists
    @Update
    protected abstract suspend fun updateTranslationCard(card: TranslationFacetCard): Int

    @Update
    protected abstract suspend fun updateFormsCard(card: FormsFacetCard): Int

    // TODO: Duplicates QuizPoolDao's insert…IfAbsent, move introduce to the session DAO
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
     * Saves a review of [card] in one transaction. A card that no longer exists, e.g. after a reset during the
     * session, is not created again, and its review is dropped completely.
     *
     * @param svDe The streak and unlock of an SV_DE review, `null` for the other facets, which keep the streak. An
     * existing DE_SV card keeps its progress.
     */
    @Transaction
    open suspend fun saveReview(
        card: FacetCard,
        log: ReviewLog,
        svDe: SvDeOutcome?,
    ) {
        val updatedRows = when (card) {
            is FormsFacetCard -> updateFormsCard(card)
            is TranslationFacetCard -> updateTranslationCard(card)
        }
        if (updatedRows == 0) return
        insertLog(log)
        when (svDe) {
            null -> Unit
            SvDeOutcome.Failed -> updateSvDeStreak(log.vocabularyId, passed = false)
            is SvDeOutcome.Passed -> {
                updateSvDeStreak(log.vocabularyId, passed = true)
                insertTranslationCardIfAbsent(
                    TranslationFacetCard(
                        vocabularyId = log.vocabularyId,
                        direction = QuizTranslationDirection.DE_SV,
                        fsrs = FsrsColumns.newCard(due = svDe.deSvDue.toEpochMilliseconds()),
                    ),
                )
            }
        }
    }
}
