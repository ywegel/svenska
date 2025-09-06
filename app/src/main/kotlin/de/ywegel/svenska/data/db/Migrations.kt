package de.ywegel.svenska.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create temporary column
        db.execSQL("ALTER TABLE Vocabulary ADD COLUMN temp_wordHighlights TEXT NOT NULL DEFAULT ''")

        // Convert each highlight to the new pair format
        val cursor = db.query("SELECT id, wordHighlights, word FROM Vocabulary")
        while (cursor.moveToNext()) {
            val id = cursor.getInt(0)
            val oldHighlightsStr = cursor.getString(1) // Old format: "1,3,5,7,8"
            val word = cursor.getString(2)

            val newHighlightsStr = if (oldHighlightsStr.isNotEmpty()) {
                oldHighlightsStr.split(",")
                    .mapNotNull { it.trim().toIntOrNull() } // Drop invalid highlights
                    .chunked(2)
                    .filter { it.size == 2 }
                    .filter { (first, second) -> first >= 0 && second >= 0 && first <= word.length && second <= word.length } // Filter out negative and out of bounds highlights
                    .joinToString(",") { (first, second) -> "$first:$second" } // New format: "1:3,5:7"
            } else {
                ""
            }

            // Update the temp column with new format
            db.execSQL(
                sql = "UPDATE Vocabulary SET temp_wordHighlights = ? WHERE id = ?",
                bindArgs = arrayOf<Any>(newHighlightsStr, id),
            )
        }
        cursor.close()

        // Drop the old column and rename temp_wordHighlights to wordHighlights
        db.execSQL("ALTER TABLE Vocabulary DROP COLUMN wordHighlights")
        db.execSQL("ALTER TABLE Vocabulary RENAME COLUMN temp_wordHighlights TO wordHighlights")
    }
}
