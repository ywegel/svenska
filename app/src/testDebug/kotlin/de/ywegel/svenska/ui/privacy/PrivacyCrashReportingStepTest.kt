package de.ywegel.svenska.ui.privacy

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import de.ywegel.svenska.R
import de.ywegel.svenska.ui.theme.SvenskaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import strikt.api.expectThat
import strikt.assertions.isEqualTo

// Guards the button-to-decision mapping: a swap would send reports for users who declined
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Config.TARGET_SDK])
class PrivacyCrashReportingStepTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private var decision: Boolean? = null

    @Test
    fun `clicking accept reports consent`() {
        // Given
        setContent()

        // When
        clickButton(R.string.privacy_crash_accept)

        // Then
        expectThat(decision).isEqualTo(true)
    }

    @Test
    fun `clicking decline reports no consent`() {
        // Given
        setContent()

        // When
        clickButton(R.string.privacy_crash_decline)

        // Then
        expectThat(decision).isEqualTo(false)
    }

    private fun setContent() {
        composeRule.setContent {
            SvenskaTheme {
                PrivacyCrashReportingStep(onDecision = { decision = it })
            }
        }
    }

    private fun clickButton(@StringRes label: Int) {
        composeRule.onNodeWithText(composeRule.activity.getString(label))
            .performScrollTo()
            .performClick()
    }
}
