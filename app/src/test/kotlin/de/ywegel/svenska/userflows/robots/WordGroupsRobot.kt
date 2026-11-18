package de.ywegel.svenska.userflows.robots

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import de.ywegel.svenska.R

class WordGroupsRobot(composeRule: AndroidComposeTestRule<*, *>) : Robot(composeRule) {

    fun assertWordGroupsScreenIsVisible() = assertEventuallyDisplayed(hasText(string(R.string.word_groups_title)))
}
