package de.ywegel.svenska.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import de.ywegel.svenska.domain.addEdit.MapUiStateToVocabularyUseCase

@Module
@InstallIn(ViewModelComponent::class)
class UseCaseModule {

    @Provides
    fun provideMapUiStateToVocabularyUseCase(): MapUiStateToVocabularyUseCase {
        return MapUiStateToVocabularyUseCase()
    }
}
