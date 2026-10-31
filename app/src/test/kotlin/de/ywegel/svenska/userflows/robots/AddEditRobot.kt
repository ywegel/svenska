package de.ywegel.svenska.userflows.robots

import androidx.annotation.StringRes
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.performTextReplacement
import de.ywegel.svenska.R

class AddEditRobot(composeRule: AndroidComposeTestRule<*, *>) : Robot(composeRule) {

    fun assertIsCreatingNew() = assertEventuallyDisplayed(hasText(string(R.string.addEdit_title_new)))

    fun assertIsEditing(word: String) =
        assertEventuallyDisplayed(hasText(string(R.string.addEdit_title_existing, word)))

    fun selectWordGroup(@StringRes wordGroup: Int, @StringRes subGroup: Int) {
        clickOnText(string(wordGroup))
        clickOnText(string(subGroup))
    }

    fun enterWord(word: String) = enterText(R.string.addEdit_label_word, word)

    fun enterTranslation(translation: String) = enterText(R.string.addEdit_label_translation, translation)

    fun enterEndings(endings: String) = enterText(R.string.addEdit_label_endings, endings)

    fun save() = clickOnDescription(R.string.accessibility_addEdit_fab_done)

    private fun enterText(@StringRes label: Int, text: String) {
        composeRule.onNode(hasText(string(label)) and hasSetTextAction()).performTextReplacement(text)
    }
}
