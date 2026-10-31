package de.ywegel.svenska.userflows.robots

import androidx.annotation.StringRes
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.performClick

abstract class Robot(protected val composeRule: AndroidComposeTestRule<*, *>) {

    protected fun string(@StringRes id: Int, vararg args: Any): String = composeRule.activity.getString(id, *args)

    protected fun clickOnText(text: String) {
        composeRule.onNode(hasText(text)).performClick()
    }

    protected fun clickOnDescription(@StringRes id: Int) {
        composeRule.onNode(hasContentDescription(string(id))).performClick()
    }

    // Room queries and navigation events run on Dispatchers.IO, which waitForIdle does not track
    protected fun assertEventuallyDisplayed(matcher: SemanticsMatcher) {
        composeRule.waitUntil(TIMEOUT_MS) { composeRule.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty() }
        composeRule.onNode(matcher).assertIsDisplayed()
    }

    private companion object {
        const val TIMEOUT_MS = 5_000L
    }
}
