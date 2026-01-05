package de.ywegel.svenska.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import de.ywegel.svenska.data.VocabularyRepository
import de.ywegel.svenska.data.preferences.UserPreferencesManager
import de.ywegel.svenska.domain.ToggleVocabularyFavoriteUseCase
import de.ywegel.svenska.domain.addEdit.MapUiStateToVocabularyUseCase
import de.ywegel.svenska.domain.main.AcceptLatestPrivacyPolicyUseCase
import de.ywegel.svenska.domain.main.AcceptLatestPrivacyPolicyUseCaseImpl
import de.ywegel.svenska.domain.main.LoadInitialAppStateUseCase
import de.ywegel.svenska.domain.main.LoadInitialAppStateUseCaseImpl
import kotlinx.coroutines.CoroutineDispatcher

@Module
@InstallIn(ViewModelComponent::class)
class UseCaseModule {

    @Provides
    fun provideMapUiStateToVocabularyUseCase(): MapUiStateToVocabularyUseCase {
        return MapUiStateToVocabularyUseCase()
    }

    @Provides
    fun provideToggleVocabularyFavoriteUseCase(
        repository: VocabularyRepository,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): ToggleVocabularyFavoriteUseCase {
        return ToggleVocabularyFavoriteUseCase(repository, ioDispatcher)
    }

    @Provides
    fun provideLoadInitialAppStateUseCase(userPreferencesManager: UserPreferencesManager): LoadInitialAppStateUseCase {
        return LoadInitialAppStateUseCaseImpl(userPreferencesManager)
    }

    @Provides
    fun provideAcceptLatestPrivacyPolicyUseCase(
        userPreferencesManager: UserPreferencesManager,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): AcceptLatestPrivacyPolicyUseCase {
        return AcceptLatestPrivacyPolicyUseCaseImpl(userPreferencesManager, ioDispatcher)
    }
}
