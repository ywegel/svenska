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

private const val TEST_DB = "word-prefix-suffix-migration-test-db"

@RunWith(AndroidJUnit4::class)
class WordPrefixSuffixMigrationTest {
    private val currentTime = System.currentTimeMillis()

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        instrumentation = InstrumentationRegistry.getInstrumentation(),
        assetsFolder = VocabularyDatabase::class.java.canonicalName!!,
        openFactory = FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    fun migrate3To4_existingWordsKeepTheirTextAndGetAnEmptyPrefixAndSuffix() {
        // Given
        helper.createDatabase(TEST_DB, 3).apply {
            execSQL(
                """
                    INSERT INTO Vocabulary (id, word, translation, ending, wordHighlights, gender, wordGroup, notes, irregularPronunciation, isFavorite, containerId, lastEdited, created)
                    VALUES (1, 'högsta dröm', 'größter Traum', '-men', '', NULL, 'Other', '', NULL, 0, 1, $currentTime, $currentTime)
                """.trimIndent(),
            )
            close()
        }

        // When
        val dbV4 = helper.runMigrationsAndValidate(TEST_DB, 4, true, MIGRATION_3_4)

        // Then
        dbV4.query("SELECT word, wordPrefix, wordSuffix FROM Vocabulary").use {
            it.moveToNext()
            expectThat(it.getString(it.getColumnIndexOrThrow("word"))).isEqualTo("högsta dröm")
            expectThat(it.getString(it.getColumnIndexOrThrow("wordPrefix"))).isEqualTo("")
            expectThat(it.getString(it.getColumnIndexOrThrow("wordSuffix"))).isEqualTo("")
        }
    }
}
