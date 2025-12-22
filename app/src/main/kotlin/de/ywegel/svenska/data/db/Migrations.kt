package de.ywegel.svenska.data.db

import android.util.Log
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import de.ywegel.svenska.domain.wordImporter.WordExtractor.normalizePdfDashes
import kotlinx.serialization.InternalSerializationApi

private const val TAG = "Migrations"

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
        } finally {
            db.endTransaction()
            Log.i(TAG, "migrate: Migration from 1 to 2 finished")
        }
    }
}

@Suppress("detekt:MagicNumber", "detekt:NestedBlockDepth")
@OptIn(InternalSerializationApi::class)
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        Log.i(TAG, "migrate: Starting migration from 2 to 3...")

        db.beginTransaction()
        try {
            val cursor = db.query(
                "SELECT id, ending FROM Vocabulary",
            )

            val updates = mutableListOf<Pair<Int, String>>()

            while (cursor.moveToNext()) {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val endingString = cursor.getString(cursor.getColumnIndexOrThrow("ending")) ?: ""

                val normalizedEnding = endingString.normalizePdfDashes()

                // Only collect if something actually changed
                if (normalizedEnding != endingString) {
                    updates.add(Pair(id, normalizedEnding))
                }
            }
            cursor.close()

            if (updates.isNotEmpty()) {
                val updateStmt = db.compileStatement(
                    "UPDATE Vocabulary SET ending = ? WHERE id = ?",
                )

                for ((id, normalizedEnding) in updates) {
                    updateStmt.apply {
                        bindString(1, normalizedEnding)
                        bindLong(2, id.toLong())
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
        } finally {
            db.endTransaction()
            Log.i(TAG, "migrate: Migration from 2 to 3 finished")
        }
    }
}
