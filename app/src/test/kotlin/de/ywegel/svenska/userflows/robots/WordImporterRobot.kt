package de.ywegel.svenska.userflows.robots

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import de.ywegel.svenska.R
import org.robolectric.Shadows.shadowOf

class WordImporterRobot(composeRule: AndroidComposeTestRule<*, *>) : Robot(composeRule) {

    fun assertWordImporterScreenIsVisible() =
        assertEventuallyDisplayed(hasText(string(R.string.wordImporter_idle_title)))

    // Robolectric has no system file picker, so the picker result is delivered to the activity directly
    fun selectFile(resourceName: String) {
        clickOnText(string(R.string.wordImporter_idle_selectFile))

        val activity = composeRule.activity
        val fileUri = Uri.parse("content://de.ywegel.svenska.test/$resourceName")
        val fileStream = checkNotNull(javaClass.classLoader?.getResourceAsStream(resourceName))
        shadowOf(activity.contentResolver).registerInputStream(fileUri, fileStream)

        val pickerRequest = checkNotNull(shadowOf(activity).nextStartedActivityForResult)
        shadowOf(activity).receiveResult(pickerRequest.intent, Activity.RESULT_OK, Intent().setData(fileUri))
    }

    fun assertFoundWords(words: Int, chapters: Int) {
        val wordsText = quantityString(R.plurals.wordImporter_parsed_words, words)
        val chaptersText = quantityString(R.plurals.wordImporter_parsed_chapters, chapters)
        assertEventuallyDisplayed(hasText(string(R.string.wordImporter_parsed_summary, wordsText, chaptersText)))
    }

    fun proceed() = clickOnText(string(R.string.wordImporter_parsed_proceed))

    fun assertWordsImported(words: Int) =
        assertEventuallyDisplayed(hasText(quantityString(R.plurals.wordImporter_finished_success, words)))

    fun exit() = clickOnText(string(R.string.wordImporter_finished_exit))
}
