package de.ywegel.svenska.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.random.Random

@Module
@InstallIn(SingletonComponent::class)
class RandomModule {

    @Singleton
    @Provides
    fun provideRandom(): Random = Random.Default
}
