package de.ywegel.svenska.data.preferences.keys

import de.ywegel.svenska.data.model.OnlineSearchType
import de.ywegel.svenska.data.preferences.PreferenceStore.Overview
import de.ywegel.svenska.data.preferences.UserPreferencesManager
import de.ywegel.svenska.data.preferences.addedToFrontAndLimited
import de.ywegel.svenska.data.preferences.jsonPreference
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

object SearchPreferenceKeys {
    val LastSearchedItems = jsonPreference(
        store = Overview,
        name = "search_sort_last_searched_items",
        default = emptyList(),
        serializer = ListSerializer(String.serializer()),
    )

    val OnlineRedirectType = jsonPreference(
        store = Overview,
        name = "search_online_redirect_type",
        default = OnlineSearchType.DictCC,
        serializer = OnlineSearchType.serializer(),
    )
}

suspend fun UserPreferencesManager.addLastSearchedItem(item: String) =
    edit(SearchPreferenceKeys.LastSearchedItems) { it.addedToFrontAndLimited(item) }
