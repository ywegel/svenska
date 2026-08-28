package de.ywegel.svenska.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import de.ywegel.svenska.di.AddEditDataStore
import de.ywegel.svenska.di.OverviewDataStore
import de.ywegel.svenska.di.SettingsDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

const val OVERVIEW_PREFERENCES_NAME = "user-preferences_overview"

const val ADD_EDIT_PREFERENCES_NAME = "user-preferences_add-edit"

const val SETTINGS_PREFERENCES_NAME = "user-preferences_settings"

interface UserPreferencesManager {
    fun <S, V> flow(key: PreferenceKey<S, V>): Flow<V>

    suspend fun <S, V> update(key: PreferenceKey<S, V>, value: V)

    suspend fun <S, V> edit(key: PreferenceKey<S, V>, transform: (V) -> V)

    suspend fun editTransaction(store: PreferenceStore, block: MutablePreferences.() -> Unit)
}

@Singleton
class UserPreferencesManagerImpl @Inject constructor(
    @OverviewDataStore private val overview: DataStore<Preferences>,
    @AddEditDataStore private val addEdit: DataStore<Preferences>,
    @SettingsDataStore private val settings: DataStore<Preferences>,
) : UserPreferencesManager {

    override fun <S, V> flow(key: PreferenceKey<S, V>): Flow<V> = storeFor(key).data
        .fallbackToDefaultOnError()
        .map { it[key] }
        .distinctUntilChanged()

    override suspend fun <S, V> update(key: PreferenceKey<S, V>, value: V) {
        storeFor(key).edit { it[key] = value }
    }

    override suspend fun <S, V> edit(key: PreferenceKey<S, V>, transform: (V) -> V) {
        storeFor(key).edit { it[key] = transform(it[key]) }
    }

    override suspend fun editTransaction(store: PreferenceStore, block: MutablePreferences.() -> Unit) {
        storeFor(store).edit(block)
    }

    private fun storeFor(store: PreferenceStore) = when (store) {
        PreferenceStore.Overview -> overview
        PreferenceStore.AddEdit -> addEdit
        PreferenceStore.Settings -> settings
    }

    private fun storeFor(key: PreferenceKey<*, *>) = storeFor(key.store)
}
