package de.ywegel.svenska.data.preferences.keys

import de.ywegel.svenska.data.preferences.PreferenceStore.Overview
import de.ywegel.svenska.data.preferences.booleanPreference

object AppPreferenceKeys {
    val UseNewQuiz = booleanPreference(Overview, "app_uses_new_quiz", false)
}
