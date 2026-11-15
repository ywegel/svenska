package de.ywegel.svenska.data.db

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.VocabularyContainer
import de.ywegel.svenska.data.model.WordGroup
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo

private const val TEST_DB_NAME = "test_db"
private const val CHAIN_TEST_DB_NAME = "full-chain-migration-test-db"
private val SCHEMA_ASSETS_FOLDER = VocabularyDatabase::class.java.canonicalName!!

@RunWith(AndroidJUnit4::class)
class VocabularyMigrationTest {

    private val currentTime = System.currentTimeMillis()

    private val highlightConverter = HighlightConverter()

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        instrumentation = InstrumentationRegistry.getInstrumentation(),
        assetsFolder = SCHEMA_ASSETS_FOLDER,
        openFactory = FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    fun migrate1ToX_fullChainProducesTheCurrentSchema() = runTest {
        // Given
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        // The newest version is the highest exported schema file, so the target never has to be updated by hand
        val latestSchemaVersion = instrumentation.context.assets.list(SCHEMA_ASSETS_FOLDER).orEmpty()
            .mapNotNull { it.removeSuffix(".json").toIntOrNull() }
            .max()

        helper.createDatabase(CHAIN_TEST_DB_NAME, 1).apply {
            execSQL("INSERT INTO VocabularyContainer (id, name) VALUES (1, 'Kapitel 1')")
            execSQL(
                """
                INSERT INTO Vocabulary (id, word, wordHighlights, translation, gender, wordGroup, ending, notes, irregularPronunciation, isFavorite, containerId, lastEdited, created)
                VALUES (1, 'bil', '0;1', 'Auto', 'Ultra', 'NOUN:AR', '-en -ar -arna', 'Nur Pkw', 'biil', 1, 1, $currentTime, $currentTime)
                """.trimIndent(),
            )
            close()
        }

        // When
        // Fails if a step is missing in ALL_MIGRATIONS or the result differs from the latest schema file
        val migratedVersion = helper.runMigrationsAndValidate(
            CHAIN_TEST_DB_NAME,
            latestSchemaVersion,
            true,
            *ALL_MIGRATIONS,
        ).use { it.version }

        val roomDb = Room.databaseBuilder(instrumentation.targetContext, VocabularyDatabase::class.java, CHAIN_TEST_DB_NAME)
            .addMigrations(*ALL_MIGRATIONS)
            .build()
        helper.closeWhenFinished(roomDb)

        // Then
        // Check that nothing from version 1 got lost
        expectThat(roomDb.openHelper.readableDatabase.version).isEqualTo(migratedVersion)
        expectThat(roomDb.vocabulary().getAllVocabulariesSnapshot()).containsExactly(
            Vocabulary(
                word = "bil",
                wordHighlights = listOf(0 to 1),
                translation = "Auto",
                gender = Gender.Ultra,
                wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.AR),
                ending = "-en -ar -arna",
                notes = "Nur Pkw",
                irregularPronunciation = "biil",
                isFavorite = true,
                containerId = 1,
                lastEdited = currentTime,
                created = currentTime,
                id = 1,
            ),
        )
        expectThat(roomDb.container().getContainerById(1)).isEqualTo(VocabularyContainer(id = 1, name = "Kapitel 1"))
    }

    @Test
    fun migrate1To2_withExistingHighlights_convertsCorrectly() {
        // Given
        val dbV1 = helper.createDatabase(TEST_DB_NAME, 1).apply {
            execSQL(
                """
                INSERT INTO Vocabulary (id, word, wordHighlights, translation, gender, wordGroup, ending, notes, irregularPronunciation, isFavorite, containerId, lastEdited, created)
                VALUES (1, 'fråga', '2;3', 'question', 'Ultra', 'Other', '', '', NULL, 0, 1, $currentTime, $currentTime)
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

            val highlights = highlightConverter.toHighlightRanges(it.getString(0))
            expectThat(highlights).containsExactly(listOf(2 to 3))
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
                VALUES (1, 'abcdefgh', '0;1;2;4;5;6;7;8', 'test', 'Ultra', 'Other', '', '', NULL, 0, 1, $currentTime, $currentTime)
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

            val highlights = highlightConverter.toHighlightRanges(it.getString(0))
            expectThat(highlights).containsExactly(listOf(0 to 1, 2 to 4, 5 to 6, 7 to 8))
        }
    }

    @Test
    fun migrate1To2_withMultipleEntries_convertsAllCorrectly() {
        // Given
        val dbV1 = helper.createDatabase(TEST_DB_NAME, 1).apply {
            execSQL(
                """
                INSERT INTO Vocabulary (id, word, wordHighlights, translation, gender, wordGroup, ending, notes, irregularPronunciation, isFavorite, containerId, lastEdited, created)
                VALUES 
                    (1, 'fråga', '2;3', 'question', 'Ultra', 'Other', '', '', NULL, 0, 1, $currentTime, $currentTime),
                    (2, 'abcdefgh', '0;1;2;4;5;6;7;8', 'test', 'Ultra', 'Other', '', '', NULL, 0, 1, $currentTime, $currentTime),
                    (3, 'abc', '0;2', 'abc', 'Ultra', 'Verb', '', '', NULL, 0, 1, $currentTime, $currentTime)
                """.trimIndent(),
            )
            close()
        }

        // When
        val dbV2 = helper.runMigrationsAndValidate(TEST_DB_NAME, 2, true, MIGRATION_1_2)

        // Then
        val cursor = dbV2.query("SELECT id, wordHighlights FROM Vocabulary ORDER BY id")
        cursor.use {
            it.moveToNext()
            expectThat(it.getInt(it.getColumnIndexOrThrow("id"))).isEqualTo(1)
            expectThat(it.getString(it.getColumnIndexOrThrow("wordHighlights"))).isEqualTo("2:3")
            expectThat(highlightConverter.toHighlightRanges(it.getString(it.getColumnIndexOrThrow("wordHighlights"))))
                .containsExactly(listOf(2 to 3))

            it.moveToNext()
            expectThat(it.getInt(it.getColumnIndexOrThrow("id"))).isEqualTo(2)
            expectThat(it.getString(it.getColumnIndexOrThrow("wordHighlights"))).isEqualTo("0:1,2:4,5:6,7:8")
            expectThat(highlightConverter.toHighlightRanges(it.getString(it.getColumnIndexOrThrow("wordHighlights"))))
                .containsExactly(listOf(0 to 1, 2 to 4, 5 to 6, 7 to 8))

            it.moveToNext()
            expectThat(it.getInt(it.getColumnIndexOrThrow("id"))).isEqualTo(3)
            expectThat(it.getString(it.getColumnIndexOrThrow("wordHighlights"))).isEqualTo("0:2")
            expectThat(highlightConverter.toHighlightRanges(it.getString(it.getColumnIndexOrThrow("wordHighlights"))))
                .containsExactly(listOf(0 to 2))
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

            val highlights = highlightConverter.toHighlightRanges(it.getString(0))
            expectThat(highlights).containsExactly(emptyList())
        }
    }

    @Test
    fun migrate1To2_withNegativeHighlightIndex_convertsToEmpty() {
        // Given
        val dbV1 = helper.createDatabase(TEST_DB_NAME, 1).apply {
            execSQL(
                sql = """   
                    INSERT INTO Vocabulary (id, word, wordHighlights, translation, gender, wordGroup, ending, notes, irregularPronunciation, isFavorite, containerId, lastEdited, created)
                    VALUES (1, 'test', '-1;4', 'test', 'Ultra', 'Other', '', '', NULL, 0, 1, $currentTime, $currentTime)
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
                    VALUES (1, 'test', '10;12', 'test', 'Ultra', 'Other', '', '', NULL, 0, 1, $currentTime, $currentTime)
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
                    VALUES (1, 'test', '2;10', 'test', 'Ultra', 'Other', '', '', NULL, 0, 1, $currentTime, $currentTime)
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
                    VALUES (1, 'test', '-4;-2;-1;4;2;3;10;12', 'test', 'Ultra', 'Other', '', '', NULL, 0, 1, $currentTime, $currentTime)
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

            val highlights = highlightConverter.toHighlightRanges(it.getString(0))
            expectThat(highlights).containsExactly(listOf(2 to 3))
        }
    }
}
