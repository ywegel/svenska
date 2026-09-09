package de.ywegel.svenska.domain

import de.ywegel.svenska.data.preferences.PreferenceKey
import de.ywegel.svenska.data.preferences.UserPreferencesManager
import de.ywegel.svenska.data.preferences.keys.PrivacyPreferenceKeys
import de.ywegel.svenska.data.preferences.set
import de.ywegel.svenska.fakes.UserPreferencesManagerFake
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.matches
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class GetOrCreateCrashReportingIdUseCaseTest {

    private fun useCase(preferences: UserPreferencesManager) =
        GetOrCreateCrashReportingIdUseCase(preferences, UnconfinedTestDispatcher())

    @Test
    fun `generates and persists an id when none exists yet`() = runTest {
        val preferences = UserPreferencesManagerFake()

        val id = useCase(preferences)()

        // Prefixed, and carrying an actual uuid after the prefix rather than just the prefix itself
        expectThat(id).isNotNull().matches(Regex("svenska-[0-9a-f-]{36}"))
        expectThat(preferences.flow(PrivacyPreferenceKeys.CrashReportingId).first()).isEqualTo(id)
    }

    @Test
    fun `returns the same id on every later call`() = runTest {
        val preferences = UserPreferencesManagerFake()
        val subject = useCase(preferences)

        val first = subject()
        val second = subject()

        expectThat(second).isEqualTo(first)
    }

    @Test
    fun `returns null instead of an empty id when the store falls back to its default`() = runTest {
        // Given a store that accepts the write but reads back the key's default, as
        // UserPreferencesManagerImpl.flow does when fallbackToDefaultOnError swallows an IOException
        val preferences = object : UserPreferencesManager by UserPreferencesManagerFake() {
            override fun <S, V> flow(key: PreferenceKey<S, V>): Flow<V> = flowOf(key.default)
        }

        // Then no unattributable id is handed out
        expectThat(useCase(preferences)()).isNull()
    }

    @Test
    fun `returns null when the id cannot be written`() = runTest {
        val preferences = object : UserPreferencesManager by UserPreferencesManagerFake() {
            override suspend fun <S, V> edit(key: PreferenceKey<S, V>, transform: (V) -> V) {
                throw IOException("cannot write preferences")
            }
        }

        expectThat(useCase(preferences)()).isNull()
    }

    @Test
    fun `keeps an id that was already stored`() = runTest {
        val existing = "11111111-2222-3333-4444-555555555555"
        val preferences = UserPreferencesManagerFake {
            set(PrivacyPreferenceKeys.CrashReportingId, existing)
        }

        expectThat(useCase(preferences)()).isEqualTo(existing)
    }
}
