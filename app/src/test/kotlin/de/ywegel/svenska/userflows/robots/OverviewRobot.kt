package de.ywegel.svenska.userflows.robots

import androidx.annotation.StringRes
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import de.ywegel.svenska.R

class OverviewRobot(composeRule: AndroidComposeTestRule<*, *>) : Robot(composeRule) {

    fun clickAddVocabulary() = clickOnDescription(R.string.accessibility_overview_fab_add_new)

    fun clickVocabulary(word: String) = clickOnText(word)

    fun assertOverviewScreenIsVisible(containerName: String) {
        assertEventuallyDisplayed(hasText(containerName))
        assertEventuallyDisplayed(hasContentDescription(string(R.string.accessibility_overview_fab_add_new)))
    }

    fun assertVocabularyIsListed(
        word: String,
        translation: String,
        @StringRes wordGroup: Int,
        @StringRes subGroup: Int,
    ) {
        assertEventuallyDisplayed(hasText(word))
        assertEventuallyDisplayed(hasText(translation))
        val badgeDescription = string(R.string.accessibility_word_group_badge, string(wordGroup), string(subGroup))
        assertEventuallyDisplayed(hasContentDescription(badgeDescription))
    }

    fun assertTranslationIsNotListed(translation: String) = assertDoesNotExist(hasText(translation))
}
