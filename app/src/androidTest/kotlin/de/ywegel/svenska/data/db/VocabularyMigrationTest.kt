package de.ywegel.svenska.data.db

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import strikt.api.expectThat
import strikt.assertions.isEqualTo

private const val TEST_DB_NAME = "test_db"

@RunWith(AndroidJUnit4::class)
class VocabularyMigrationTest {

    private val currentTime = System.currentTimeMillis()

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        instrumentation = InstrumentationRegistry.getInstrumentation(),
        assetsFolder = VocabularyDatabase::class.java.canonicalName,
        openFactory = FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    fun migrate1To2_withExistingHighlights_convertsCorrectly() {
        // Given
        val dbV1 = helper.createDatabase(TEST_DB_NAME, 1).apply {
            execSQL(
                """
                INSERT INTO Vocabulary (id, word, wordHighlights, translation, gender, wordGroup, ending, notes, irregularPronunciation, isFavorite, containerId, lastEdited, created)
                VALUES (1, 'fråga', '2,3', 'question', 'Ultra', 'Other', '', '', NULL, 0, 1, $currentTime, $currentTime)
                """.trimIndent(),
            )
            close()
        }

        // When
        val dbV2 = helper.runMigrationsAndValidate(TEST_DB_NAME, 2, true, MIGRATION_1_2)

        // Then
        val cursor = dbV2.query("SELECT wordHighlights FROM Vocabulary WHERE id = 1")
        cursor.use {
            it.moveToNext()
            val newHighlights = it.getString(0)
            expectThat(newHighlights).isEqualTo("2:3")
        }
    }

    @Test
    fun migrate1To2_withEmptyHighlights_keepsEmpty() {
        // Given
        val dbV1 = helper.createDatabase(TEST_DB_NAME, 1).apply {
            execSQL(
                """
                INSERT INTO Vocabulary (id, word, wordHighlights, translation, gender, wordGroup, ending, notes, irregularPronunciation, isFavorite, containerId, lastEdited, created)
                VALUES (1, 'test', '', 'test', 'Ultra', 'Other', '', '', NULL, 0, 1, $currentTime, $currentTime)
                """.trimIndent(),
            )
            close()
        }

        // When
        val dbV2 = helper.runMigrationsAndValidate(TEST_DB_NAME, 2, true, MIGRATION_1_2)

        // Then
        val cursor = dbV2.query("SELECT wordHighlights FROM Vocabulary WHERE id = 1")
        cursor.use {
            it.moveToNext()
            expectThat(it.getString(0)).isEqualTo("")
        }
    }

    @Test
    fun migrate1To2_withMultipleHighlights_convertsCorrectly() {
        // Given
        val dbV1 = helper.createDatabase(TEST_DB_NAME, 1).apply {
            execSQL(
                """
                INSERT INTO Vocabulary (id, word, wordHighlights, translation, gender, wordGroup, ending, notes, irregularPronunciation, isFavorite, containerId, lastEdited, created)
                VALUES (1, 'abcdefgh', '0,1,2,4,5,6,7,8', 'test', 'Ultra', 'Other', '', '', NULL, 0, 1, $currentTime, $currentTime)
                """.trimIndent(),
            )
            close()
        }

        // When
        val dbV2 = helper.runMigrationsAndValidate(TEST_DB_NAME, 2, true, MIGRATION_1_2)

        // Then
        val cursor = dbV2.query("SELECT wordHighlights FROM Vocabulary WHERE id = 1")
        cursor.use {
            it.moveToNext()
            expectThat(it.getString(0)).isEqualTo("0:1,2:4,5:6,7:8")
        }
    }

    @Test
    fun migrate1To2_withEmptyDatabase_succeeds() {
        // Given
        val dbV1 = helper.createDatabase(TEST_DB_NAME, 1).apply { close() }

        // When
        val dbV2 = helper.runMigrationsAndValidate(TEST_DB_NAME, 2, true, MIGRATION_1_2)

        // Then
        dbV2.query("SELECT * FROM Vocabulary")
    }

    @Test
    fun migrate1To2_withInvalidHighlightFormat_convertsToEmpty() {
        // Given
        val dbV1 = helper.createDatabase(TEST_DB_NAME, 1).apply {
            execSQL(
                sql = """
                    INSERT INTO Vocabulary (id, word, wordHighlights, translation, gender, wordGroup, ending, notes, irregularPronunciation, isFavorite, containerId, lastEdited, created)
                    VALUES (1, 'test', 'invalid', 'test', 'Ultra', 'Other', '', '', NULL, 0, 1, $currentTime, $currentTime)
                """.trimIndent(),
            )
            close()
        }

        // When
        val dbV2 = helper.runMigrationsAndValidate(TEST_DB_NAME, 2, true, MIGRATION_1_2)

        // Then
        val cursor = dbV2.query("SELECT wordHighlights FROM Vocabulary WHERE id = 1")
        cursor.use {
            it.moveToNext()
            expectThat(it.getString(0)).isEqualTo("")
        }
    }

    @Test
    fun migrate1To2_withNegativeHighlightIndex_convertsToEmpty() {
        // Given
        val dbV1 = helper.createDatabase(TEST_DB_NAME, 1).apply {
            execSQL(
                sql = """   
                    INSERT INTO Vocabulary (id, word, wordHighlights, translation, gender, wordGroup, ending, notes, irregularPronunciation, isFavorite, containerId, lastEdited, created)
                    VALUES (1, 'test', '-1,4', 'test', 'Ultra', 'Other', '', '', NULL, 0, 1, $currentTime, $currentTime)
                """.trimIndent(),
            )
            close()
        }

        // When
        val dbV2 = helper.runMigrationsAndValidate(TEST_DB_NAME, 2, true, MIGRATION_1_2)

        // Then
        val cursor = dbV2.query("SELECT wordHighlights FROM Vocabulary WHERE id = 1")
        cursor.use {
            it.moveToNext()
            expectThat(it.getString(0)).isEqualTo("")
        }
    }

    @Test
    fun migrate1To2_withOutOfBoundsHighlightIndex_convertsToEmpty() {
        // Given
        val dbV1 = helper.createDatabase(TEST_DB_NAME, 1).apply {
            execSQL(
                """
                    INSERT INTO Vocabulary (id, word, wordHighlights, translation, gender, wordGroup, ending, notes, irregularPronunciation, isFavorite, containerId, lastEdited, created)
                    VALUES (1, 'test', '10,12', 'test', 'Ultra', 'Other', '', '', NULL, 0, 1, $currentTime, $currentTime)
                """.trimIndent(),
            )
            close()
        }

        // When
        val dbV2 = helper.runMigrationsAndValidate(TEST_DB_NAME, 2, true, MIGRATION_1_2)

        // Then
        val cursor = dbV2.query("SELECT wordHighlights FROM Vocabulary WHERE id = 1")
        cursor.use {
            it.moveToNext()
            expectThat(it.getString(0)).isEqualTo("")
        }
    }

    @Test
    fun migrate1To2_withPartialOutOfBoundsHighlightIndex_convertsToEmpty() {
        // Given
        val dbV1 = helper.createDatabase(TEST_DB_NAME, 1).apply {
            execSQL(
                """
                    INSERT INTO Vocabulary (id, word, wordHighlights, translation, gender, wordGroup, ending, notes, irregularPronunciation, isFavorite, containerId, lastEdited, created)
                    VALUES (1, 'test', '2,10', 'test', 'Ultra', 'Other', '', '', NULL, 0, 1, $currentTime, $currentTime)
                """.trimIndent(),
            )
            close()
        }

        // When
        val dbV2 = helper.runMigrationsAndValidate(TEST_DB_NAME, 2, true, MIGRATION_1_2)

        // Then
        val cursor = dbV2.query("SELECT wordHighlights FROM Vocabulary WHERE id = 1")
        cursor.use {
            it.moveToNext()
            expectThat(it.getString(0)).isEqualTo("")
        }
    }

    @Test
    fun migrate1To2_withSomeValidAndInvalidHighlights_removesInvalid() {
        // Given
        val dbV1 = helper.createDatabase(TEST_DB_NAME, 1).apply {
            execSQL(
                """
                    INSERT INTO Vocabulary (id, word, wordHighlights, translation, gender, wordGroup, ending, notes, irregularPronunciation, isFavorite, containerId, lastEdited, created)
                    VALUES (1, 'test', '-4,-2,-1,4,2,3,10,12', 'test', 'Ultra', 'Other', '', '', NULL, 0, 1, $currentTime, $currentTime)
                """.trimIndent(),
            )
            close()
        }

        // When: Migrate
        val dbV2 = helper.runMigrationsAndValidate(TEST_DB_NAME, 2, true, MIGRATION_1_2)

        // Then
        val cursor = dbV2.query("SELECT wordHighlights FROM Vocabulary WHERE id = 1")
        cursor.use {
            it.moveToNext()
            expectThat(it.getString(0)).isEqualTo("2:3")
        }
    }
}
