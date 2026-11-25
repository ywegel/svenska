package de.ywegel.svenska.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import de.ywegel.svenska.data.db.quiz.FormsFacetCard
import de.ywegel.svenska.data.db.quiz.FsrsColumns
import de.ywegel.svenska.data.db.quiz.QuizPoolEntry
import de.ywegel.svenska.data.db.quiz.TranslationFacetCard
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.quiz.PoolRemoval
import de.ywegel.svenska.data.model.quiz.QuizPoolStatus
import de.ywegel.svenska.data.model.quiz.QuizTranslationDirection

@Suppress("detekt:TooManyFunctions", "detekt:MaxLineLength")
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

    /**
     * The rows of all [QuizPoolStatus.ACTIVE] words.
     */
    @Query(
        """
        SELECT Vocabulary.* FROM Vocabulary
        JOIN QuizPoolEntry ON QuizPoolEntry.vocabularyId = Vocabulary.id
        WHERE QuizPoolEntry.introducedAt IS NOT NULL AND NOT QuizPoolEntry.isPaused
        """,
    )
    abstract suspend fun getActiveVocabularies(): List<Vocabulary>

    /**
     * The new cards since [since], in epoch milliseconds: introduced words and introduced forms cards together, as they
     * share the daily limit.
     */
    @Query(
        """
        SELECT (SELECT COUNT(*) FROM QuizPoolEntry WHERE introducedAt >= :since)
             + (SELECT COUNT(*) FROM QuizPoolEntry WHERE formsIntroducedAt >= :since)
        """,
    )
    abstract suspend fun countIntroducedSince(since: Long): Int

    /**
     * The next [limit] [QuizPoolStatus.QUEUED] words, first in, first out.
     */
    @Query(
        """
        SELECT Vocabulary.* FROM Vocabulary
        JOIN QuizPoolEntry ON QuizPoolEntry.vocabularyId = Vocabulary.id
        WHERE QuizPoolEntry.introducedAt IS NULL
        ORDER BY QuizPoolEntry.queuedAt, QuizPoolEntry.vocabularyId
        LIMIT :limit
        """,
    )
    abstract suspend fun getQueued(limit: Int): List<Vocabulary>

    /**
     * Active rows that may introduce their forms card, longest introduced first. Rows whose forms card already exists
     * are not filtered out here, as the forms key is not stored. Not even by [QuizPoolEntry.formsIntroducedAt]: After
     * an edit of the forms, the row points to a new forms key, which needs its own card.
     */
    // TODO(formsKey column): Filter rows whose forms card exists with NOT EXISTS on FormsFacetCard
    @Query(
        """
        SELECT Vocabulary.* FROM Vocabulary
        JOIN QuizPoolEntry ON QuizPoolEntry.vocabularyId = Vocabulary.id
        WHERE QuizPoolEntry.introducedAt IS NOT NULL AND NOT QuizPoolEntry.isPaused
            AND QuizPoolEntry.svDeStreak >= :minSvDeStreak
            AND NOT Vocabulary.formsNeedReview
        ORDER BY QuizPoolEntry.introducedAt, QuizPoolEntry.vocabularyId
        """,
    )
    abstract suspend fun getFormsCandidates(minSvDeStreak: Int): List<Vocabulary>

    @Query("UPDATE QuizPoolEntry SET introducedAt = :now WHERE vocabularyId IN (:vocabularyIds) AND introducedAt IS NULL")
    protected abstract suspend fun markIntroduced(vocabularyIds: List<Int>, now: Long)

    @Query("UPDATE QuizPoolEntry SET formsIntroducedAt = :now WHERE vocabularyId IN (:vocabularyIds)")
    protected abstract suspend fun markFormsIntroduced(vocabularyIds: List<Int>, now: Long)

    // TODO: Duplicates ReviewDao's insert…IfAbsent, move introduce to the session DAO
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertTranslationCardsIfAbsent(cards: List<TranslationFacetCard>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertFormsCardsIfAbsent(cards: List<FormsFacetCard>)

    /**
     * Introduces queued words with a new SV_DE card and new forms cards, all due at [now], in one transaction.
     *
     * @param wordIds The rows of queued words. A row that is no longer queued keeps its introduction.
     * @param formsKeysByRow The forms key of each row that introduces its shared forms card. Only this row gets
     * [QuizPoolEntry.formsIntroducedAt], so the card counts once towards the daily limit.
     */
    @Transaction
    open suspend fun introduce(wordIds: List<Int>, formsKeysByRow: Map<Int, String>, now: Long) {
        markIntroduced(wordIds, now)
        insertTranslationCardsIfAbsent(
            wordIds.map { TranslationFacetCard(it, QuizTranslationDirection.SV_DE, FsrsColumns.newCard(due = now)) },
        )
        markFormsIntroduced(formsKeysByRow.keys.toList(), now)
        insertFormsCardsIfAbsent(formsKeysByRow.values.map { FormsFacetCard(it, FsrsColumns.newCard(due = now)) })
    }
}
