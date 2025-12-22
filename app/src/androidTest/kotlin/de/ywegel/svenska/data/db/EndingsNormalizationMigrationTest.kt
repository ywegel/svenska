package de.ywegel.svenska.data.db

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import strikt.api.expectThat
import strikt.assertions.doesNotContain
import strikt.assertions.isEqualTo

private const val TEST_DB = "endings-normalization-migration-test-db"

@RunWith(AndroidJUnit4::class)
class EndingsNormalizationMigrationTest {
    private val currentTime = System.currentTimeMillis()

    private val unicodeMinus = "\u2212"
    private val enDash = "\u2013"
    private val emDash = "\u2014"
    private val hyphen = "\u2010"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        instrumentation = InstrumentationRegistry.getInstrumentation(),
        assetsFolder = VocabularyDatabase::class.java.canonicalName!!,
        openFactory = FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    fun migrate2To3_allPdfDashVariantsAreConvertedToNormalHyphenMinus() {
        // Given
        val dbV2 = helper.createDatabase(TEST_DB, 2).apply {
            execSQL(
                """
                    INSERT INTO Vocabulary (id, word, translation, ending, wordHighlights, gender, wordGroup, notes, irregularPronunciation, isFavorite, containerId, lastEdited, created)
                    VALUES 
                        (1, 'hus1', 'house', '${unicodeMinus}et $enDash ${emDash}en', '', NULL, 'Other', '', NULL, 0, 1, $currentTime, $currentTime),
                        (2, 'hus2', 'house', '${enDash}et $emDash ${hyphen}en', '', NULL, 'Other', '', NULL, 0, 1, $currentTime, $currentTime),
                        (3, 'hus3', 'house', '${emDash}et $hyphen ${unicodeMinus}en', '', NULL, 'Other', '', NULL, 0, 1, $currentTime, $currentTime),
                        (4, 'hus4', 'house', '${hyphen}et $unicodeMinus ${enDash}en', '', NULL, 'Other', '', NULL, 0, 1, $currentTime, $currentTime)
                """.trimIndent(),
            )
            close()
        }

        // When
        val dbV3 = helper.runMigrationsAndValidate(TEST_DB, 3, true, MIGRATION_2_3)

        // Then
        val cursor = dbV3.query("SELECT id, word, ending FROM Vocabulary ORDER BY id")

        cursor.use {
            while (cursor.moveToNext()) {
                val id = it.getString(it.getColumnIndexOrThrow("id"))
                val word = it.getString(it.getColumnIndexOrThrow("word"))
                val ending = it.getString(it.getColumnIndexOrThrow("ending"))

                expectThat(ending)
                    .isEqualTo("-et - -en")
                    .get { this.asIterable() }
                    .doesNotContain(unicodeMinus, enDash, emDash, hyphen)
                expectThat(id.toInt()).isEqualTo(cursor.position + 1)
                expectThat(word).isEqualTo("hus${cursor.position + 1}")
            }
        }
    }
}
