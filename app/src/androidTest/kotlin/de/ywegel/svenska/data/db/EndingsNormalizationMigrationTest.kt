package de.ywegel.svenska.data.db

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.ywegel.svenska.data.model.WordGroup
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import strikt.api.expectThat
import strikt.assertions.doesNotContain
import strikt.assertions.isEqualTo
import strikt.assertions.isNotEqualTo

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

    /**
     * The word group does not get re-determined, if the word did not contain any unnormalized dashes.
     * This is tested, by settings the wordGroup to "Other", even though the WordGroupMatcher would determine its word
     * group as WordGroup.Noun(WordGroup.NounSubgroup.AR)
     */
    @Test
    fun migrate2To3_wordGroupsAreRedeterminedOnlyForNewlyNormalizedWords() {
        // Given
        val expectedWord = "word"
        val expectedTranslation = "ShouldStayTheSame"
        val expectedEndings = "-n -ar -en"
        val expectedWordGroup = WordGroup.Other
        // This is the word group, that would get determined by the WordGroupMatcher.
        val redeterminedWordGroup = WordGroup.Noun(WordGroup.NounSubgroup.AR)

        val dbWordGroupConverter = WordGroupConverter()

        val dbV2 = helper.createDatabase(TEST_DB, 2).apply {
            execSQL(
                """
                    INSERT INTO Vocabulary (id, word, translation, ending, wordHighlights, gender, wordGroup, notes, irregularPronunciation, isFavorite, containerId, lastEdited, created)
                    VALUES 
                        (1, '$expectedWord', '$expectedTranslation', '$expectedEndings', '', NULL, 'Other', '', NULL, 0, 1, $currentTime, $currentTime)
                """.trimIndent(),
            )
            close()
        }

        // When
        val dbV3 = helper.runMigrationsAndValidate(TEST_DB, 3, true, MIGRATION_2_3)

        // Then
        val cursor = dbV3.query("SELECT id, word, translation, wordGroup, ending FROM Vocabulary ORDER BY id")

        cursor.use {
            it.moveToNext()
            val id = it.getString(it.getColumnIndexOrThrow("id"))
            val word = it.getString(it.getColumnIndexOrThrow("word"))
            val translation = it.getString(it.getColumnIndexOrThrow("translation"))
            val ending = it.getString(it.getColumnIndexOrThrow("ending"))
            val wordGroup = it.getString(it.getColumnIndexOrThrow("wordGroup"))

            expectThat(id.toInt()).isEqualTo(1)
            expectThat(word).isEqualTo(expectedWord)
            expectThat(translation).isEqualTo(expectedTranslation)
            expectThat(ending).isEqualTo(expectedEndings)
            expectThat(dbWordGroupConverter.fromString(wordGroup))
                .isEqualTo(expectedWordGroup)
                .isNotEqualTo(redeterminedWordGroup)
        }
    }

    @Test
    fun migrate2To3_wordGroupIsRedeterminedCorrectlyForAllNewlyNormalizedWords() {
        // Given
        val dbWordGroupConverter = WordGroupConverter()

        val dbV2 = helper.createDatabase(TEST_DB, 2).apply {
            execSQL(
                """
                    INSERT INTO Vocabulary (id, word, translation, ending, wordHighlights, gender, wordGroup, notes, irregularPronunciation, isFavorite, containerId, lastEdited, created)
                    VALUES 
                        (1, 'hus', 'house', '${unicodeMinus}et $enDash ${emDash}en', '', NULL, 'Other', '', NULL, 0, 1, $currentTime, $currentTime),
                        (2, 'word', 'word', '${enDash}n ${emDash}ar ${hyphen}en', '', NULL, 'Other', '', NULL, 0, 1, $currentTime, $currentTime),
                        (3, 'sko', 'shoe', '${emDash}n ${hyphen}r ${unicodeMinus}rna', '', NULL, 'Other', '', NULL, 0, 1, $currentTime, $currentTime),
                        (4, 'example', 'e', '${emDash}x ${hyphen}y ${unicodeMinus}z', '', NULL, 'Other', '', NULL, 0, 1, $currentTime, $currentTime)
                """.trimIndent(),
            )
            close()
        }

        // When
        val dbV3 = helper.runMigrationsAndValidate(TEST_DB, 3, true, MIGRATION_2_3)

        // Then
        val cursor = dbV3.query("SELECT id, word, wordGroup, ending FROM Vocabulary ORDER BY id")

        cursor.use {
            val idIndex = it.getColumnIndexOrThrow("id")
            val wordIndex = it.getColumnIndexOrThrow("word")
            val endingIndex = it.getColumnIndexOrThrow("ending")
            val wordGroupIndex = it.getColumnIndexOrThrow("wordGroup")

            it.moveToNext()
            expectThat(it.getInt(idIndex)).isEqualTo(1)
            expectThat(it.getString(wordIndex)).isEqualTo("hus")
            expectThat(it.getString(endingIndex))
                .isEqualTo("-et - -en")
                .get { this.asIterable() }
                .doesNotContain(unicodeMinus, enDash, emDash, hyphen)
            expectThat(dbWordGroupConverter.fromString(it.getString(wordGroupIndex)))
                .isNotEqualTo(WordGroup.Other)
                .isEqualTo(WordGroup.Noun(WordGroup.NounSubgroup.UNCHANGED_ETT))

            it.moveToNext()
            expectThat(it.getInt(idIndex)).isEqualTo(2)
            expectThat(it.getString(wordIndex)).isEqualTo("word")
            expectThat(it.getString(endingIndex))
                .isEqualTo("-n -ar -en")
                .get { this.asIterable() }
                .doesNotContain(unicodeMinus, enDash, emDash, hyphen)
            expectThat(dbWordGroupConverter.fromString(it.getString(wordGroupIndex)))
                .isNotEqualTo(WordGroup.Other)
                .isEqualTo(WordGroup.Noun(WordGroup.NounSubgroup.AR))

            it.moveToNext()
            expectThat(it.getInt(idIndex)).isEqualTo(3)
            expectThat(it.getString(wordIndex)).isEqualTo("sko")
            expectThat(it.getString(endingIndex))
                .isEqualTo("-n -r -rna")
                .get { this.asIterable() }
                .doesNotContain(unicodeMinus, enDash, emDash, hyphen)
            expectThat(dbWordGroupConverter.fromString(it.getString(wordGroupIndex)))
                .isNotEqualTo(WordGroup.Other)
                .isEqualTo(WordGroup.Noun(WordGroup.NounSubgroup.R))

            it.moveToNext()
            expectThat(it.getInt(idIndex)).isEqualTo(4)
            expectThat(it.getString(wordIndex)).isEqualTo("example")
            expectThat(it.getString(endingIndex))
                .isEqualTo("-x -y -z")
                .get { this.asIterable() }
                .doesNotContain(unicodeMinus, enDash, emDash, hyphen)
            expectThat(dbWordGroupConverter.fromString(it.getString(wordGroupIndex)))
                .isNotEqualTo(WordGroup.Other)
                .isEqualTo(WordGroup.Verb(WordGroup.VerbSubgroup.GROUP_4_SPECIAL))
        }
    }
}
