package de.ywegel.svenska.data.preferences.keys

import de.ywegel.svenska.data.model.OnlineSearchType
import de.ywegel.svenska.data.preferences.PreferenceStore.Overview
import de.ywegel.svenska.data.preferences.UserPreferencesManager
import de.ywegel.svenska.data.preferences.addedToFrontAndLimited
import de.ywegel.svenska.data.preferences.jsonPreference
import de.ywegel.svenska.serializers.ArrayDequeSerializer

object SearchPreferenceKeys {
    val LastSearchedItems = jsonPreference(
        store = Overview,
        name = "search_sort_last_searched_items",
        default = ArrayDeque<String>(),
        serializer = ArrayDequeSerializer,
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
