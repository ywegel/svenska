package de.ywegel.svenska.userflows.robots

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction

abstract class Robot(protected val composeRule: AndroidComposeTestRule<*, *>) {

    protected fun string(@StringRes id: Int, vararg args: Any): String = composeRule.activity.getString(id, *args)

    protected fun quantityString(@PluralsRes id: Int, quantity: Int): String =
        composeRule.activity.resources.getQuantityString(id, quantity, quantity)

    protected fun clickOnText(text: String) = clickOn(hasText(text))

    protected fun scrollToAndClickOnText(text: String) = scrollToAndClickOn(hasText(text))

    // Clicks through the semantics action: scrolled to the bottom edge, a touch could hit a floating action button
    protected fun scrollToAndClickOn(matcher: SemanticsMatcher) {
        composeRule.onNode(matcher).performScrollTo().performSemanticsAction(SemanticsActions.OnClick)
    }

    protected fun clickOnDescription(@StringRes id: Int, vararg args: Any) =
        clickOn(hasContentDescription(string(id, *args)))

    protected fun assertEventuallyDisplayed(matcher: SemanticsMatcher) {
        waitUntilExists(matcher)
        composeRule.onNode(matcher).assertIsDisplayed()
    }

    protected fun assertDoesNotExist(matcher: SemanticsMatcher) {
        composeRule.onNode(matcher).assertDoesNotExist()
    }

    protected fun waitUntilGone(matcher: SemanticsMatcher) {
        composeRule.waitUntil(TIMEOUT_MS) { composeRule.onAllNodes(matcher).fetchSemanticsNodes().isEmpty() }
    }

    private fun clickOn(matcher: SemanticsMatcher) {
        composeRule.onNode(matcher).performClick()
    }

    // Room queries and navigation events run on Dispatchers.IO, which waitForIdle does not track
    private fun waitUntilExists(matcher: SemanticsMatcher) {
        composeRule.waitUntil(TIMEOUT_MS) { composeRule.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty() }
    }

    private companion object {
        const val TIMEOUT_MS = 5_000L
    }
}
