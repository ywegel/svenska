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
import strikt.assertions.isTrue

private const val TEST_DB = "forms-migration-test-db"

@RunWith(AndroidJUnit4::class)
class FormsMigrationTest {
    private val currentTime = System.currentTimeMillis()

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        instrumentation = InstrumentationRegistry.getInstrumentation(),
        assetsFolder = VocabularyDatabase::class.java.canonicalName!!,
        openFactory = FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    fun migrate4To5_existingWordsKeepTheirDataAndGetNoFormsAndNoReviewFlag() {
        // Given
        helper.createDatabase(TEST_DB, 4).apply {
            execSQL(
                """
                    INSERT INTO Vocabulary (id, word, wordPrefix, wordSuffix, translation, ending, wordHighlights, gender, wordGroup, notes, irregularPronunciation, isFavorite, containerId, lastEdited, created)
                    VALUES (1, 'känna', '', 'sig', 'sich fühlen', '-er kände känt', '', NULL, 'VERB:GROUP_2B', '', NULL, 0, 1, $currentTime, $currentTime)
                """.trimIndent(),
            )
            close()
        }

        // When
        val dbV5 = helper.runMigrationsAndValidate(TEST_DB, 5, true, MIGRATION_4_5)

        // Then
        dbV5.query("SELECT word, wordSuffix, ending, form1, form2, form3, formsNeedReview FROM Vocabulary").use {
            expectThat(it.moveToNext()).isTrue()
            expectThat(it.getString(it.getColumnIndexOrThrow("word"))).isEqualTo("känna")
            expectThat(it.getString(it.getColumnIndexOrThrow("wordSuffix"))).isEqualTo("sig")
            expectThat(it.getString(it.getColumnIndexOrThrow("ending"))).isEqualTo("-er kände känt")
            expectThat(it.isNull(it.getColumnIndexOrThrow("form1"))).isTrue()
            expectThat(it.isNull(it.getColumnIndexOrThrow("form2"))).isTrue()
            expectThat(it.isNull(it.getColumnIndexOrThrow("form3"))).isTrue()
            expectThat(it.getInt(it.getColumnIndexOrThrow("formsNeedReview"))).isEqualTo(0)
        }
    }

    @Test
    fun migrate1To5_fullChainProducesTheCurrentSchema() {
        // Given
        helper.createDatabase(TEST_DB, 1).close()

        // When / Then: validates the resulting schema against 5.json
        helper.runMigrationsAndValidate(TEST_DB, 5, true, MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
    }
}
