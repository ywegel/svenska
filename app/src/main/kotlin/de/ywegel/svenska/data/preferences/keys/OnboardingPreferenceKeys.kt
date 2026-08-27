package de.ywegel.svenska.data.preferences.keys

import de.ywegel.svenska.data.preferences.PreferenceStore.Overview
import de.ywegel.svenska.data.preferences.booleanPreference

object OnboardingPreferenceKeys {
    val HasCompleted = booleanPreference(
        store = Overview,
        name = "app_has_completed_onboarding",
        default = false,
    )
}
