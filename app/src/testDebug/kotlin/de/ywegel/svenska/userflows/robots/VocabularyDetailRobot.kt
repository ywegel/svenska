package de.ywegel.svenska.userflows.robots

import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import de.ywegel.svenska.R

class VocabularyDetailRobot(composeRule: AndroidComposeTestRule<*, *>) : Robot(composeRule) {

    fun clickEdit() = clickOnText(string(R.string.vocabulary_detail_edit_btn))
}
