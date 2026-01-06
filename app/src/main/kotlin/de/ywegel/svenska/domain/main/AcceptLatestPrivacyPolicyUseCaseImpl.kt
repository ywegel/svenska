package de.ywegel.svenska.domain.main

import de.ywegel.svenska.data.preferences.UserPreferencesManager
import de.ywegel.svenska.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AcceptLatestPrivacyPolicyUseCaseImpl @Inject constructor(
    private val userPreferencesManager: UserPreferencesManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : AcceptLatestPrivacyPolicyUseCase {
    override suspend operator fun invoke() = withContext(ioDispatcher) {
        userPreferencesManager.acceptLatestPrivacyPolicy()
    }
}
