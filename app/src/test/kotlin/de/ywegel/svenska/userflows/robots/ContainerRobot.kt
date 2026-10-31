package de.ywegel.svenska.userflows.robots

import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import de.ywegel.svenska.R

class ContainerRobot(composeRule: AndroidComposeTestRule<*, *>) : Robot(composeRule) {

    fun openSettings() = clickOnDescription(R.string.accessibility_container_toolbar_settings)
}
