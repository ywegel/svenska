package de.ywegel.svenska.data.preferences

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import de.ywegel.svenska.data.SortOrder
import de.ywegel.svenska.domain.search.OnlineSearchType
import de.ywegel.svenska.jsonConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "UserPreferencesManager"

// TODO: Maybe have everything in one file? Or split it up?
const val OVERVIEW_PREFERENCES_NAME = "user-preferences_overview"
val Context.dataStoreOverview: DataStore<Preferences> by preferencesDataStore(name = OVERVIEW_PREFERENCES_NAME)

data class OverviewPreferences(
    val sortOrder: SortOrder,
    val revert: Boolean,
    val showCompactVocabularyItem: Boolean,
)

data class SearchPreferences(
    val lastSearchedItems: ArrayDeque<String>,
    val showCompactVocabularyItem: Boolean,
    val showOnlineRedirectFirst: Boolean,
    val onlineRedirectType: OnlineSearchType,
)

data class AppPreferences(
    val hasCompletedOnboarding: Boolean,
)

interface UserPreferencesManager {
    val preferencesOverviewFlow: Flow<OverviewPreferences>

    val preferencesSearchFlow: Flow<SearchPreferences>

    val preferencesAppFlow: Flow<AppPreferences>

    suspend fun updateOverviewSortOrder(sortOrder: SortOrder)

    suspend fun updateOverviewSortOrderRevert(revert: Boolean)

    suspend fun showCompactVocabularyItem(showCompactVocabularyItem: Boolean)

    suspend fun addLastSearchedItem(item: String)

    suspend fun showCompactVocabularyItemInSearch(show: Boolean)

    suspend fun updateOnlineRedirectPosition(first: Boolean)

    suspend fun updateOnlineRedirectType(type: OnlineSearchType)

    suspend fun updateHasCompletedOnboarding(hasCompleted: Boolean)
}

@Singleton
class UserPreferencesManagerImpl @Inject constructor(@ApplicationContext val context: Context) :
    UserPreferencesManager {

    override val preferencesAppFlow = context.dataStoreOverview.data
        .fallbackToDefaultOnError()
        .map { preferences ->
            val hasCompletedOnboarding = preferences[PreferencesKeys.APP_HAS_COMPLETED_ONBOARDING] ?: false
            AppPreferences(hasCompletedOnboarding)
        }

    override val preferencesOverviewFlow = context.dataStoreOverview.data
        .fallbackToDefaultOnError()
        .map { preferences ->
            val sortOrder = SortOrder.valueOf(
                preferences[PreferencesKeys.OVERVIEW_SORT_ORDER] ?: SortOrder.default.name,
            )
            val revert = preferences[PreferencesKeys.OVERVIEW_SORT_ORDER_REVERT] ?: false
            val showCompactVocabularyItem: Boolean =
                preferences[PreferencesKeys.OVERVIEW_SHOW_COMPACT_VOCABULARY_ITEM] ?: false
            OverviewPreferences(sortOrder, revert, showCompactVocabularyItem)
        }

    override suspend fun updateOverviewSortOrder(sortOrder: SortOrder) {
        context.dataStoreOverview.edit { preferences ->
            preferences[PreferencesKeys.OVERVIEW_SORT_ORDER] = sortOrder.name
        }
    }

    override suspend fun updateOverviewSortOrderRevert(revert: Boolean) {
        context.dataStoreOverview.edit { preferences ->
            preferences[PreferencesKeys.OVERVIEW_SORT_ORDER_REVERT] = revert
        }
    }

    override suspend fun showCompactVocabularyItem(showCompactVocabularyItem: Boolean) {
        context.dataStoreOverview.edit { preferences ->
            preferences[PreferencesKeys.OVERVIEW_SHOW_COMPACT_VOCABULARY_ITEM] = showCompactVocabularyItem
        }
    }

    override val preferencesSearchFlow: Flow<SearchPreferences> = context.dataStoreOverview.data
        .fallbackToDefaultOnError()
        .map { preferences ->
            val lastSearchedItems: ArrayDeque<String> =
                preferences[PreferencesKeys.SEARCH_SORT_LAST_SEARCHED_ITEMS]?.let {
                    jsonConfig.decodeFromString(ArrayDequeSerializer, it)
                } ?: ArrayDeque()

            val showCompactVocabularyItem = preferences[PreferencesKeys.SEARCH_SHOW_COMPACT_VOCABULARY_ITEM] ?: false
            val showOnlineRedirectFirst = preferences[PreferencesKeys.SEARCH_ONLINE_REDIRECT_POSITION] ?: false
            val onlineRedirectUrl = preferences[PreferencesKeys.SEARCH_ONLINE_REDIRECT_TYPE]?.let {
                jsonConfig.decodeFromString<OnlineSearchType>(it)
            } ?: OnlineSearchType.DictCC

            SearchPreferences(lastSearchedItems, showCompactVocabularyItem, showOnlineRedirectFirst, onlineRedirectUrl)
        }

    override suspend fun addLastSearchedItem(item: String) {
        context.dataStoreOverview.edit { preferences ->
            val currentDeque: ArrayDeque<String> = preferences[PreferencesKeys.SEARCH_SORT_LAST_SEARCHED_ITEMS]?.let {
                jsonConfig.decodeFromString(ArrayDequeSerializer, it)
            } ?: ArrayDeque()

            currentDeque.addToFrontAndLimit(item)

            preferences[PreferencesKeys.SEARCH_SORT_LAST_SEARCHED_ITEMS] =
                jsonConfig.encodeToString(ArrayDequeSerializer, currentDeque)
        }
    }

    override suspend fun showCompactVocabularyItemInSearch(show: Boolean) {
        context.dataStoreOverview.edit { preferences ->
            preferences[PreferencesKeys.SEARCH_SHOW_COMPACT_VOCABULARY_ITEM] = show
        }
    }

    override suspend fun updateOnlineRedirectPosition(first: Boolean) {
        context.dataStoreOverview.edit { preferences ->
            preferences[PreferencesKeys.SEARCH_ONLINE_REDIRECT_POSITION] = first
        }
    }

    override suspend fun updateOnlineRedirectType(type: OnlineSearchType) {
        context.dataStoreOverview.edit { preferences ->
            preferences[PreferencesKeys.SEARCH_ONLINE_REDIRECT_TYPE] = jsonConfig.encodeToString(type)
        }
    }

    override suspend fun updateHasCompletedOnboarding(hasCompleted: Boolean) {
        context.dataStoreOverview.edit { preferences ->
            preferences[PreferencesKeys.APP_HAS_COMPLETED_ONBOARDING] = hasCompleted
        }
    }

    private object PreferencesKeys {
        val OVERVIEW_SORT_ORDER = stringPreferencesKey("overview_sort_order")
        val OVERVIEW_SORT_ORDER_REVERT = booleanPreferencesKey("overview_sort_order_revert")
        val OVERVIEW_SHOW_COMPACT_VOCABULARY_ITEM = booleanPreferencesKey("overview_show_compact_vocabulary_item")

        val SEARCH_SORT_LAST_SEARCHED_ITEMS = stringPreferencesKey("search_sort_last_searched_items")
        val SEARCH_SHOW_COMPACT_VOCABULARY_ITEM = booleanPreferencesKey("search_show_compact_vocabulary_item")
        val SEARCH_ONLINE_REDIRECT_POSITION = booleanPreferencesKey("search_online_redirect_position")
        val SEARCH_ONLINE_REDIRECT_TYPE = stringPreferencesKey("search_online_redirect_type")

        val APP_HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("app_has_completed_onboarding")
    }
}

fun Flow<Preferences>.fallbackToDefaultOnError(): Flow<Preferences> {
    return this.catch { exception ->
        if (exception is IOException) {
            Log.e(TAG, "Error reading preferences", exception)
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }
}
