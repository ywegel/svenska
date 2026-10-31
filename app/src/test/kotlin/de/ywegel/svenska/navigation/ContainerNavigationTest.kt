package de.ywegel.svenska.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.ramcosta.composedestinations.generated.destinations.ContainerScreenDestination
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import de.ywegel.svenska.HiltTestActivity
import de.ywegel.svenska.R
import de.ywegel.svenska.ui.theme.SvenskaTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class, sdk = [Config.TARGET_SDK])
class ContainerNavigationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltTestActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun `clicking settings button navigates to SettingsScreen`() {
        // Given
        composeRule.setContent {
            SvenskaTheme {
                AppNavigation(startRoute = ContainerScreenDestination)
            }
        }

        // When
        composeRule.onNodeWithContentDescription(
            composeRule.activity.getString(R.string.accessibility_container_toolbar_settings),
        ).performClick()

        // Then
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.settings_title)).assertIsDisplayed()
    }
}
