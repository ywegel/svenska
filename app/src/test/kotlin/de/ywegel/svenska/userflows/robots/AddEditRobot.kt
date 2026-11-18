package de.ywegel.svenska.userflows.robots

import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import de.ywegel.svenska.R

@Suppress("TooManyFunctions")
class AddEditRobot(composeRule: AndroidComposeTestRule<*, *>) : Robot(composeRule) {

    private val saveButton get() = hasContentDescription(string(R.string.accessibility_addEdit_fab_done))
    private val deleteButton get() = hasContentDescription(string(R.string.accessibility_addEdit_delete))
    private val favoriteButton get() = hasContentDescription(string(R.string.accessibility_addEdit_favorite))

    fun assertIsCreatingNew() = assertEventuallyDisplayed(hasText(string(R.string.addEdit_title_new)))

    fun assertIsEditing(word: String) =
        assertEventuallyDisplayed(hasText(string(R.string.addEdit_title_existing, word)))

    fun selectWordGroup(@StringRes wordGroup: Int) = clickOnText(string(wordGroup))

    fun selectWordGroup(@StringRes wordGroup: Int, @StringRes subGroup: Int) {
        selectWordGroup(wordGroup)
        clickOnText(string(subGroup))
    }

    fun assertWordGroupIsSelected(@StringRes wordGroup: Int, @StringRes subGroup: Int) {
        composeRule.onNode(hasText(string(wordGroup)) and isSelected()).assertIsDisplayed()
        composeRule.onNode(hasText(string(subGroup)) and isSelected()).assertIsDisplayed()
    }

    fun openWordGroupInfo() = clickOnDescription(R.string.accessibility_addEdit_navigate_word_group_info_screen)

    fun switchGender(from: String, to: String) =
        clickOnDescription(R.string.accessibility_addEdit_gender_toggle, from, to)

    fun assertGenderIs(article: String, other: String) = assertEventuallyDisplayed(genderToggle(article, other))

    fun assertGenderToggleIsHidden() {
        assertDoesNotExist(genderToggle(article = "en", other = "ett"))
        assertDoesNotExist(genderToggle(article = "ett", other = "en"))
    }

    fun enterWord(word: String) = enterText(R.string.addEdit_label_word, word)

    fun selectChangingWord(word: String) {
        composeRule.onNode(changingWordChip(word)).performClick()
    }

    fun assertChangingWordIs(word: String) {
        composeRule.onNode(changingWordChip(word) and isSelected()).performScrollTo().assertIsDisplayed()
    }

    fun assertHighlightErrorIsShown() =
        assertEventuallyDisplayed(hasText(string(R.string.addEdit_word_highlight_error)))

    fun enterTranslation(translation: String) = enterText(R.string.addEdit_label_translation, translation)

    fun enterEndings(endings: String) = enterText(R.string.addEdit_label_endings, endings)

    fun assertFieldShows(@StringRes label: Int, text: String) {
        composeRule.onNode(textField(label) and hasText(text)).performScrollTo().assertIsDisplayed()
    }

    /** A built form or the base form, as the forms list shows it with prefix and suffix. */
    fun assertFormIsShown(form: String) {
        composeRule.onNode(hasText(form)).performScrollTo().assertIsDisplayed()
    }

    fun writeOutForms() = scrollToAndClickOnText(string(R.string.addEdit_forms_write_out))

    fun useEndings() = scrollToAndClickOnText(string(R.string.addEdit_forms_use_endings))

    // Written out forms are fields without label, so they are found by their current text
    fun changeWrittenForm(from: String, to: String) {
        composeRule.onNode(hasSetTextAction() and hasText(from)).performScrollTo().performTextReplacement(to)
    }

    fun assertWrittenFormIsShown(form: String) {
        composeRule.onNode(hasSetTextAction() and hasText(form)).performScrollTo().assertIsDisplayed()
    }

    fun assertSwitchToEndingsIsAsked() =
        assertEventuallyDisplayed(hasText(string(R.string.addEdit_forms_switch_dialog_title)))

    fun confirmSwitchToEndings() = clickOnText(string(R.string.addEdit_forms_switch_dialog_confirm))

    fun cancelSwitchToEndings() = clickOnText(string(R.string.general_cancel))

    fun addNotes(notes: String) {
        scrollToAndClickOnText(string(R.string.addEdit_label_notes))
        enterText(R.string.addEdit_label_notes, notes)
    }

    fun removeNotes() = scrollToAndClickOn(hasContentDescription(string(R.string.accessibility_addEdit_remove_notes)))

    fun addIrregularPronunciation(pronunciation: String) {
        scrollToAndClickOnText(string(R.string.addEdit_label_irregular_pronunciation))
        enterText(R.string.addEdit_label_irregular_pronunciation, pronunciation)
    }

    fun removeIrregularPronunciation() =
        scrollToAndClickOn(hasContentDescription(string(R.string.accessibility_addEdit_remove_irregular_pronunciation)))

    fun toggleFavorite() = clickOnDescription(R.string.accessibility_addEdit_favorite)

    fun assertIsFavorite() = composeRule.onNode(favoriteButton).assertIsOn()

    fun delete() {
        composeRule.onNode(deleteButton).performClick()
        clickOnText(string(R.string.general_ok))
        waitUntilGone(saveButton)
    }

    fun assertDeleteIsHidden() = assertDoesNotExist(deleteButton)

    // Waits until the screen has left composition, otherwise its text fields match the next screen's assertions
    fun save() {
        composeRule.onNode(saveButton).performClick()
        waitUntilGone(saveButton)
    }

    /** Taps save for an input that can't be saved, so the screen stays. */
    fun trySave() {
        composeRule.onNode(saveButton).performClick()
    }

    fun assertMissingWordGroupIsReported() =
        assertEventuallyDisplayed(hasText(string(R.string.addEdit_group_selector_invalid_configuration)))

    fun close() {
        clickOnDescription(R.string.accessibility_addEdit_close)
        waitUntilGone(saveButton)
    }

    // The word field contains the same words, but has no click action
    private fun changingWordChip(word: String) = hasText(word) and hasClickAction()

    private fun genderToggle(article: String, other: String) =
        hasContentDescription(string(R.string.accessibility_addEdit_gender_toggle, article, other))

    private fun textField(@StringRes label: Int) = hasText(string(label)) and hasSetTextAction()

    private fun enterText(@StringRes label: Int, text: String) {
        composeRule.onNode(textField(label)).performScrollTo().performTextReplacement(text)
    }
}
