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
import de.ywegel.svenska.jsonConfig
import de.ywegel.svenska.serializers.QueueSerializer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.LinkedList
import java.util.Queue
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "UserPreferencesManager"

const val OVERVIEW_PREFERENCES_NAME = "user-preferences_overview"
val Context.dataStoreOverview: DataStore<Preferences> by preferencesDataStore(name = OVERVIEW_PREFERENCES_NAME)

data class OverviewPreferences(
    val sortOrder: SortOrder,
    val revert: Boolean,
    val lastSearchedItems: Queue<String>,
    val showCompactVocabularyItem: Boolean,
)

interface UserPreferencesManager {
    val preferencesOverviewFlow: Flow<OverviewPreferences>

    suspend fun updateOverviewSortOrder(sortOrder: SortOrder)

    suspend fun updateOverviewSortOrderRevert(revert: Boolean)

    suspend fun updateOverviewLastSearchedItems(queue: Queue<String>)

    suspend fun showCompactVocabularyItem(showCompactVocabularyItem: Boolean)
}

@Singleton
class UserPreferencesManagerImpl @Inject constructor(@ApplicationContext val context: Context) :
    UserPreferencesManager {

    override val preferencesOverviewFlow = context.dataStoreOverview.data
        .fallbackToDefaultOnError()
        .map { preferences ->
            val sortOrder = SortOrder.valueOf(
                preferences[PreferencesKeys.OVERVIEW_SORT_ORDER] ?: SortOrder.default.name,
            )
            val revert = preferences[PreferencesKeys.OVERVIEW_SORT_ORDER_REVERT] ?: false
            val lastSearchedItems: Queue<String> =
                preferences[PreferencesKeys.OVERVIEW_SORT_LAST_SEARCHED_ITEMS]?.let {
                    jsonConfig.decodeFromString(QueueSerializer, it)
                } ?: LinkedList()
            val showCompactVocabularyItem: Boolean =
                preferences[PreferencesKeys.OVERVIEW_SHOW_COMPACT_VOCABULARY_ITEM] ?: false
            OverviewPreferences(sortOrder, revert, lastSearchedItems, showCompactVocabularyItem)
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

    override suspend fun updateOverviewLastSearchedItems(queue: Queue<String>) {
        context.dataStoreOverview.edit { preferences ->
            preferences[PreferencesKeys.OVERVIEW_SORT_LAST_SEARCHED_ITEMS] =
                jsonConfig.encodeToString(QueueSerializer, queue)
        }
    }

    override suspend fun showCompactVocabularyItem(showCompactVocabularyItem: Boolean) {
        context.dataStoreOverview.edit { preferences ->
            preferences[PreferencesKeys.OVERVIEW_SHOW_COMPACT_VOCABULARY_ITEM] = showCompactVocabularyItem
        }
    }

    private object PreferencesKeys {
        val OVERVIEW_SORT_ORDER = stringPreferencesKey("overview_sort_order")
        val OVERVIEW_SORT_ORDER_REVERT = booleanPreferencesKey("overview_sort_order_revert")
        val OVERVIEW_SORT_LAST_SEARCHED_ITEMS = stringPreferencesKey("overview_sort_last_searched_items")
        val OVERVIEW_SHOW_COMPACT_VOCABULARY_ITEM = booleanPreferencesKey("overview_show_compact_vocabulary_item")
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
