package de.ywegel.svenska.data.preferences.keys

import de.ywegel.svenska.data.preferences.PreferenceStore.Overview
import de.ywegel.svenska.data.preferences.booleanPreference
import de.ywegel.svenska.data.preferences.enumPreference
import de.ywegel.svenska.data.model.SortOrder as SortOrderValue

object OverviewPreferenceKeys {
    val SortOrder = enumPreference(Overview, "overview_sort_order", SortOrderValue.default) {
        SortOrderValue.valueOf(it)
    }
    val Revert = booleanPreference(Overview, "overview_sort_order_revert", false)
    val ShowCompactVocabularyItem = booleanPreference(Overview, "overview_show_compact_vocabulary_item", false)
}
