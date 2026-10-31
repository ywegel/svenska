package de.ywegel.svenska.userflows

import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class SettingsFlowTest : UserFlowTest() {

    @Test
    fun `clicking settings button navigates to SettingsScreen`() {
        // Given
        launchApp()

        // When
        container { openSettings() }

        // Then
        settings { assertSettingsScreenIsVisible() }
    }
}
