package de.ywegel.svenska.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.ywegel.svenska.analytics.SentryController
import de.ywegel.svenska.analytics.SentryControllerImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {
    @Singleton
    @Binds
    abstract fun bindSentryController(controller: SentryControllerImpl): SentryController
}
