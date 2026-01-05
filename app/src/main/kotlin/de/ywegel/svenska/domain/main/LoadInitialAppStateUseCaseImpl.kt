package de.ywegel.svenska.domain.main

import de.ywegel.svenska.data.preferences.LATEST_PRIVACY_VERSION
import de.ywegel.svenska.data.preferences.UserPreferencesManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LoadInitialAppStateUseCaseImpl @Inject constructor(
    private val userPreferencesManager: UserPreferencesManager,
) : LoadInitialAppStateUseCase {
    override operator fun invoke(): Flow<InitialAppState> = userPreferencesManager.preferencesAppFlow
        .map { settings ->
            InitialAppState(
                hasCompletedOnboarding = settings.hasCompletedOnboarding,
                isLatestPrivacyPolicyAccepted = settings.acceptedPrivacyVersion == LATEST_PRIVACY_VERSION,
            )
        }
        .distinctUntilChanged()
}

data class InitialAppState(
    val hasCompletedOnboarding: Boolean,
    val isLatestPrivacyPolicyAccepted: Boolean,
)
