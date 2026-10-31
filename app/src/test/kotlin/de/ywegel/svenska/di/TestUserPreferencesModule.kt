package de.ywegel.svenska.di

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import de.ywegel.svenska.data.preferences.UserPreferencesManager
import de.ywegel.svenska.fakes.UserPreferencesManagerFake
import javax.inject.Singleton

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [UserPreferencesModule::class])
object TestUserPreferencesModule {
    @Provides
    @Singleton
    fun provideUserPreferencesManager(): UserPreferencesManager = UserPreferencesManagerFake()
}
