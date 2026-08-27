package de.ywegel.svenska.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import de.ywegel.svenska.di.AddEditDataStore
import de.ywegel.svenska.di.OverviewDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

const val OVERVIEW_PREFERENCES_NAME = "user-preferences_overview"

const val ADD_EDIT_PREFERENCES_NAME = "user-preferences_add-edit"

interface UserPreferencesManager {
    fun <S, V> flow(key: PreferenceKey<S, V>): Flow<V>

    suspend fun <S, V> update(key: PreferenceKey<S, V>, value: V)

    suspend fun <S, V> edit(key: PreferenceKey<S, V>, transform: (V) -> V)
}

@Singleton
class UserPreferencesManagerImpl @Inject constructor(
    @OverviewDataStore private val overview: DataStore<Preferences>,
    @AddEditDataStore private val addEdit: DataStore<Preferences>,
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

    private fun storeFor(key: PreferenceKey<*, *>) = when (key.store) {
        PreferenceStore.Overview -> overview
        PreferenceStore.AddEdit -> addEdit
    }
}
