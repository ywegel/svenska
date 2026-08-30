package de.ywegel.svenska.data.preferences

import android.util.Log
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import de.ywegel.svenska.jsonConfig
import io.sentry.Sentry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.serialization.KSerializer
import java.io.IOException

private const val TAG = "UserPreferencesManager"

enum class PreferenceStore { Overview, AddEdit, Settings }

/**
 * Describes a single preference: where it is stored, how it is persisted ([S]) and how it is
 * exposed to the rest of the app ([V]).
 */
class PreferenceKey<S, V>(
    val store: PreferenceStore,
    val key: Preferences.Key<S>,
    val default: V,
    val decode: (S) -> V,
    val encode: (V) -> S,
)

fun booleanPreference(store: PreferenceStore, name: String, default: Boolean): PreferenceKey<Boolean, Boolean> =
    PreferenceKey(
        store = store,
        key = booleanPreferencesKey(name),
        default = default,
        decode = { it },
        encode = { it },
    )

fun intPreference(store: PreferenceStore, name: String, default: Int): PreferenceKey<Int, Int> = PreferenceKey(
    store = store,
    key = intPreferencesKey(name),
    default = default,
    decode = { it },
    encode = { it },
)

fun stringPreference(store: PreferenceStore, name: String, default: String): PreferenceKey<String, String> =
    PreferenceKey(
        store = store,
        key = stringPreferencesKey(name),
        default = default,
        decode = { it },
        encode = { it },
    )

fun <V : Enum<V>> enumPreference(
    store: PreferenceStore,
    name: String,
    default: V,
    decode: (String) -> V,
): PreferenceKey<String, V> = PreferenceKey(
    store = store,
    key = stringPreferencesKey(name),
    default = default,
    decode = decode,
    encode = { it.name },
)

fun <V> jsonPreference(
    store: PreferenceStore,
    name: String,
    default: V,
    serializer: KSerializer<V>,
): PreferenceKey<String, V> = PreferenceKey(
    store = store,
    key = stringPreferencesKey(name),
    default = default,
    decode = { jsonConfig.decodeFromString(serializer, it) },
    encode = { jsonConfig.encodeToString(serializer, it) },
)

/**
 * Falls back to [PreferenceKey.default] if the value is absent or cannot be decoded, so a
 * persisted value that is no longer valid never kills the flow.
 */
operator fun <S, V> Preferences.get(key: PreferenceKey<S, V>): V =
    this[key.key]?.let { runCatching { key.decode(it) }.getOrNull() } ?: key.default

operator fun <S, V> MutablePreferences.set(key: PreferenceKey<S, V>, value: V) {
    this[key.key] = key.encode(value)
}

fun Flow<Preferences>.fallbackToDefaultOnError(): Flow<Preferences> {
    return this.catch { exception ->
        if (exception is IOException) {
            Sentry.captureException(exception)
            Log.e(TAG, "Error reading preferences", exception)
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }
}
