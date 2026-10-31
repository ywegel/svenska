package de.ywegel.svenska.userflows.robots

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import de.ywegel.svenska.R

class SettingsRobot(composeRule: AndroidComposeTestRule<*, *>) : Robot(composeRule) {

    fun assertSettingsScreenIsVisible() = assertEventuallyDisplayed(hasText(string(R.string.settings_title)))
}
