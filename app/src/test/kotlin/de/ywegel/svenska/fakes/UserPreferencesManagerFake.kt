package de.ywegel.svenska.fakes

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import de.ywegel.svenska.data.preferences.PreferenceKey
import de.ywegel.svenska.data.preferences.UserPreferencesManager
import de.ywegel.svenska.data.preferences.get
import de.ywegel.svenska.data.preferences.set
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class UserPreferencesManagerFake(
    initial: MutablePreferences.() -> Unit = {},
) : UserPreferencesManager {

    private val prefs = MutableStateFlow<Preferences>(
        emptyPreferences().toMutablePreferences().apply(initial),
    )

    override fun <S, V> flow(key: PreferenceKey<S, V>): Flow<V> = prefs.map { it[key] }.distinctUntilChanged()

    override suspend fun <S, V> update(key: PreferenceKey<S, V>, value: V) = edit(key) { value }

    override suspend fun <S, V> edit(key: PreferenceKey<S, V>, transform: (V) -> V) {
        prefs.update { current ->
            current.toMutablePreferences().apply { set(key, transform(current[key])) }
        }
    }
}
