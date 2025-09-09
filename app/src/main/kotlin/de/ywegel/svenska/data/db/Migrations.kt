package de.ywegel.svenska.data.db

import android.util.Log
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

private const val TAG = "Migrations"

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        Log.i(TAG, "migrate: Starting migration from 1 to 2...")

        db.beginTransaction()
        try {
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
                db.execSQL(
                    sql = "UPDATE Vocabulary SET wordHighlights = ? WHERE id = ?",
                    bindArgs = arrayOf<Any>(newHighlightsStr, id),
                )
            }
            cursor.close()
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            Log.i(TAG, "migrate: Migration from 1 to 2 finished")
        }
    }
}
