package de.ywegel.svenska.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.ywegel.svenska.data.preferences.UserPreferencesManager
import de.ywegel.svenska.data.preferences.UserPreferencesManagerImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UserPreferencesBindModule {
    @Binds
    @Singleton
    abstract fun bindUserPreferencesManager(impl: UserPreferencesManagerImpl): UserPreferencesManager
}
