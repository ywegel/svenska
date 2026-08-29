package de.ywegel.svenska.data.db

import android.util.Log
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.data.model.WordGroup
import io.sentry.Sentry

private const val TAG = "Migrations"

@Suppress("detekt:TooGenericExceptionCaught")
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        Log.i(TAG, "migrate: Starting migration from 1 to 2...")

        db.beginTransaction()
        try {
            val statement = db.compileStatement("UPDATE Vocabulary SET wordHighlights = ? WHERE id = ?")

            // Convert each highlight to the new pair format
            val cursor = db.query("SELECT id, wordHighlights, word FROM Vocabulary")

            while (cursor.moveToNext()) {
                val id = cursor.getInt(0)
                val oldHighlightsStr = cursor.getString(1) // Old format: "1;3;5;7;8"
                val word = cursor.getString(2)

                val newHighlightsStr = if (oldHighlightsStr.isNotEmpty()) {
                    oldHighlightsStr.split(";")
                        .mapNotNull { it.trim().toIntOrNull() } // Drop invalid highlights
                        .chunked(2)
                        .filter { it.size == 2 }
                        .filter { (first, second) ->
                            // Filter out negative and out of bounds highlights
                            first >= 0 && second >= 0 && first <= word.length && second <= word.length
                        }
                        .joinToString(",") { (first, second) -> "$first:$second" } // New format: "1:3,5:7"
                } else {
                    ""
                }

                // Update the temp column with new format
                statement.apply {
                    bindString(1, newHighlightsStr)
                    bindLong(2, id.toLong())
                    executeUpdateDelete()
                    clearBindings()
                }
            }
            cursor.close()
            statement.close()
            db.setTransactionSuccessful()
        } catch (t: Throwable) {
            Sentry.captureException(t)
            Log.e(TAG, "migrate: Migration from 1 to 2 failed", t)
            throw t
        } finally {
            db.endTransaction()
        }
        Log.i(TAG, "migrate: Migration from 1 to 2 finished successfully")
    }
}

@Suppress("detekt:MagicNumber", "detekt:NestedBlockDepth", "detekt:TooGenericExceptionCaught")
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        Log.i(TAG, "migrate: Starting migration from 2 to 3...")

        val dbWordGroupConverter = WordGroupConverter()

        db.beginTransaction()
        try {
            val cursor = db.query(
                "SELECT id, word, ending FROM Vocabulary",
            )

            val updates = mutableListOf<Triple<Int, String, WordGroup>>()

            while (cursor.moveToNext()) {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val wordString = cursor.getString(cursor.getColumnIndexOrThrow("word")) ?: ""
                val endingString = cursor.getString(cursor.getColumnIndexOrThrow("ending")) ?: ""

                val normalizedEnding = FrozenDashNormalizerAtV2ToV3.normalize(endingString)

                // Only collect if something actually changed
                if (normalizedEnding != endingString) {
                    val redeterminedWordGroup = FrozenWordGroupMatcherAtV2ToV3.determineWordGroup(
                        baseWord = wordString,
                        endings = normalizedEnding.split(" "),
                    )
                    updates.add(Triple(id, normalizedEnding, redeterminedWordGroup))
                }
            }
            cursor.close()

            if (updates.isNotEmpty()) {
                val updateStmt = db.compileStatement(
                    "UPDATE Vocabulary SET ending = ?, wordGroup = ? WHERE id = ?",
                )

                for ((id, normalizedEnding, wordGroup) in updates) {
                    val encodedWordGroup = dbWordGroupConverter.toString(wordGroup)
                    updateStmt.apply {
                        bindString(1, normalizedEnding)
                        bindString(2, encodedWordGroup)
                        bindLong(3, id.toLong())
                        executeUpdateDelete()
                        clearBindings()
                    }
                }
                updateStmt.close()
                Log.i(TAG, "migrate: Normalized dashes in ${updates.size} entries")
            } else {
                Log.i(TAG, "migrate: All dashes are already normalized")
            }
            db.setTransactionSuccessful()
        } catch (t: Throwable) {
            Sentry.captureException(t)
            Log.e(TAG, "migrate: Migration from 2 to 3 failed", t)
            throw t
        } finally {
            db.endTransaction()
        }
        Log.i(TAG, "migrate: Migration from 2 to 3 finished")
    }
}

/**
 * Frozen snapshot of [de.ywegel.svenska.domain.wordImporter.WordExtractor.normalizePdfDashes]. Migrations must keep
 * reproducing the exact transformation.
 */
private object FrozenDashNormalizerAtV2ToV3 {
    fun normalize(input: String): String {
        return input
            .replace('\u2212', '-')
            .replace('\u2013', '-')
            .replace('\u2014', '-')
            .replace('\u2010', '-')
    }
}

/**
 * Frozen snapshot of [de.ywegel.svenska.domain.wordImporter.WordGroupMatcher]. Migrations must keep reproducing the
 * exact transformation.
 */
private object FrozenWordGroupMatcherAtV2ToV3 {
    fun determineWordGroup(baseWord: String, endings: List<String>): WordGroup {
        return when {
            isAdjective(endings) -> WordGroup.Adjective
            isNoun(endings) -> determineNounSubgroup(endings)
            isVerb(endings) -> determineVerbSubgroup(baseWord, endings)
            else -> WordGroup.Other
        }
    }

    private fun isAdjective(endings: List<String>): Boolean {
        // Adjectives always only have 2 endings (mostly "-t", "-a")
        return endings.size == ADJECTIVE_ENDINGS
    }

    private fun isNoun(endings: List<String>): Boolean {
        // Nouns always have 3 endings and the first ending is -n/-en/-t/-et
        return endings.size == NOUN_ENDINGS && endings[0].removePrefix("-") in nounIndicatorEndings
    }

    private fun isVerb(endings: List<String>): Boolean {
        // Verbs always have 3 endings
        return endings.size == VERB_ENDINGS
    }

    private fun determineNounSubgroup(endings: List<String>): WordGroup.Noun {
        val pluralEnding = endings[1]
        val lazyGender by lazy { determineGender(WordGroup.Noun(WordGroup.NounSubgroup.UNDEFINED), endings) }

        return when {
            pluralEnding.endsWith("or") -> WordGroup.Noun(WordGroup.NounSubgroup.OR)
            pluralEnding.endsWith("ar") -> WordGroup.Noun(WordGroup.NounSubgroup.AR)
            pluralEnding.endsWith("er") -> WordGroup.Noun(WordGroup.NounSubgroup.ER)
            pluralEnding.endsWith("r") -> WordGroup.Noun(WordGroup.NounSubgroup.R)
            pluralEnding.endsWith("n") -> WordGroup.Noun(WordGroup.NounSubgroup.N)
            pluralEnding.endsWith("") && lazyGender == Gender.Neutra -> WordGroup.Noun(
                WordGroup.NounSubgroup.UNCHANGED_ETT,
            )

            pluralEnding.endsWith("") && lazyGender == Gender.Ultra -> WordGroup.Noun(
                WordGroup.NounSubgroup.UNCHANGED_EN,
            )

            else -> WordGroup.Noun(WordGroup.NounSubgroup.UNDEFINED)
        }
    }

    @Suppress("detekt:CyclomaticComplexMethod")
    private fun determineVerbSubgroup(baseWord: String, endings: List<String>): WordGroup.Verb {
        val present = endings[0]
        val past = endings[1]
        return when {
            baseWord.endsWith("a") && present.endsWith("ar") && past.endsWith("ade") -> WordGroup.Verb(
                WordGroup.VerbSubgroup.GROUP_1,
            )

            baseWord.endsWith("a") && present.endsWith("r") && past.endsWith("de") -> WordGroup.Verb(
                WordGroup.VerbSubgroup.GROUP_1,
            )

            baseWord.endsWith("a") && present.endsWith("er") && past.endsWith("de") -> WordGroup.Verb(
                WordGroup.VerbSubgroup.GROUP_2A,
            )

            baseWord.endsWith("a") && present.endsWith("er") && past.endsWith("te") -> WordGroup.Verb(
                WordGroup.VerbSubgroup.GROUP_2B,
            )

            baseWord.last() in setOf('a', 'e', 'i', 'o', 'u', 'y') &&
                present.endsWith("r") &&
                past.endsWith("dde")
            -> WordGroup.Verb(WordGroup.VerbSubgroup.GROUP_3)

            else -> WordGroup.Verb(WordGroup.VerbSubgroup.GROUP_4_SPECIAL)
        }
    }

    fun determineGender(wordGroup: WordGroup, endings: List<String>): Gender? {
        if (endings.isEmpty()) return null
        if (wordGroup !is WordGroup.Noun) return null

        val firstEnding = endings[0].removePrefix("-")

        return when {
            firstEnding == "n" || firstEnding == "en" -> Gender.Ultra // En-word
            firstEnding == "t" || firstEnding == "et" -> Gender.Neutra // Ett-word
            else -> null // Unknown
        }
    }

    private val nounIndicatorEndings = setOf("n", "en", "t", "et")
    const val NOUN_ENDINGS = 3
    const val VERB_ENDINGS = 3
    const val ADJECTIVE_ENDINGS = 2
}
