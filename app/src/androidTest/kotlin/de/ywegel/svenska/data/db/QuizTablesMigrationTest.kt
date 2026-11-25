package de.ywegel.svenska.data.db

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isTrue

private const val TEST_DB = "quiz-tables-migration-test-db"

private val QUIZ_TABLES = listOf("QuizPoolEntry", "TranslationFacetCard", "FormsFacetCard", "ReviewLog")

@RunWith(AndroidJUnit4::class)
class QuizTablesMigrationTest {
    private val currentTime = System.currentTimeMillis()

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        instrumentation = InstrumentationRegistry.getInstrumentation(),
        assetsFolder = VocabularyDatabase::class.java.canonicalName!!,
        openFactory = FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    fun migrate5To6_existingWordsKeepTheirDataAndTheQuizTablesStartEmpty() {
        // Given
        createV5DatabaseWithOneWord()

        // When
        // Also validates the new tables, indices and foreign keys against the exported schema 6
        val dbV6 = helper.runMigrationsAndValidate(TEST_DB, 6, true, MIGRATION_5_6)

        // Then
        dbV6.query("SELECT word, translation, form1, formsNeedReview FROM Vocabulary WHERE id = 1").use {
            expectThat(it.moveToNext()).isTrue()
            expectThat(it.getString(it.getColumnIndexOrThrow("word"))).isEqualTo("känna")
            expectThat(it.getString(it.getColumnIndexOrThrow("translation"))).isEqualTo("sich fühlen")
            expectThat(it.getString(it.getColumnIndexOrThrow("form1"))).isEqualTo("känner")
            expectThat(it.getInt(it.getColumnIndexOrThrow("formsNeedReview"))).isEqualTo(0)
        }
        QUIZ_TABLES.forEach { table ->
            expectThat(dbV6.count(table)).describedAs("rows in $table").isEqualTo(0)
        }
    }

    @Test
    fun migrate5To6_deletingAWordDeletesItsQuizRowsButKeepsTheSharedFormsCard() {
        // Given
        createV5DatabaseWithOneWord()
        val dbV6 = helper.runMigrationsAndValidate(TEST_DB, 6, true, MIGRATION_5_6)
        // Room turns foreign keys on for its own connections, the raw helper connection does not
        dbV6.execSQL("PRAGMA foreign_keys = ON")
        dbV6.execSQL(
            "INSERT INTO QuizPoolEntry " +
                "(vocabularyId, queuedAt, introducedAt, formsIntroducedAt, isPaused, svDeStreak, resetAt) " +
                "VALUES (1, $currentTime, $currentTime, $currentTime, 0, 3, NULL)",
        )
        dbV6.execSQL(
            "INSERT INTO TranslationFacetCard " +
                "(vocabularyId, direction, phase, step, stability, difficulty, due, lastReview) " +
                "VALUES (1, 'SV_DE', 'REVIEW', NULL, 4.5, 5.2, $currentTime, $currentTime)",
        )
        dbV6.execSQL(
            "INSERT INTO FormsFacetCard (formsKey, phase, step, stability, difficulty, due, lastReview) " +
                "VALUES ('känna|känner|kände|känt', 'LEARNING', 0, NULL, NULL, $currentTime, NULL)",
        )
        dbV6.execSQL(
            "INSERT INTO ReviewLog (vocabularyId, facet, formsKey, rating, rawAnswer, reviewAt, durationMillis) " +
                "VALUES (1, 'SV_DE', NULL, 3, 'sich fühlen', $currentTime, 1200)",
        )

        // When
        dbV6.execSQL("DELETE FROM Vocabulary WHERE id = 1")

        // Then
        expectThat(dbV6.count("QuizPoolEntry")).isEqualTo(0)
        expectThat(dbV6.count("TranslationFacetCard")).isEqualTo(0)
        expectThat(dbV6.count("ReviewLog")).isEqualTo(0)
        expectThat(dbV6.count("FormsFacetCard")).isEqualTo(1)
    }

    private fun createV5DatabaseWithOneWord() {
        helper.createDatabase(TEST_DB, 5).apply {
            execSQL(
                """
                    INSERT INTO Vocabulary (id, word, wordPrefix, wordSuffix, translation, ending, form1, form2, form3, formsNeedReview, wordHighlights, gender, wordGroup, notes, irregularPronunciation, isFavorite, containerId, lastEdited, created)
                    VALUES (1, 'känna', '', 'sig', 'sich fühlen', '-er kände känt', 'känner', 'kände', 'känt', 0, '', NULL, 'VERB:GROUP_2B', '', NULL, 0, 1, $currentTime, $currentTime)
                """.trimIndent(),
            )
            close()
        }
    }

    private fun SupportSQLiteDatabase.count(table: String): Int = query("SELECT COUNT(*) FROM `$table`").use {
        it.moveToNext()
        it.getInt(0)
    }
}
