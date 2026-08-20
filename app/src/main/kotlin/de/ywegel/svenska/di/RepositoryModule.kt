package de.ywegel.svenska.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.ywegel.svenska.data.ContainerRepository
import de.ywegel.svenska.data.FavoritesAndPronunciationsRepository
import de.ywegel.svenska.data.FileRepository
import de.ywegel.svenska.data.QuizRepository
import de.ywegel.svenska.data.SearchRepository
import de.ywegel.svenska.data.VocabularyRepository
import de.ywegel.svenska.data.impl.ContainerRepositoryImpl
import de.ywegel.svenska.data.impl.FavoritesAndPronunciationsRepositoryImpl
import de.ywegel.svenska.data.impl.FileRepositoryImpl
import de.ywegel.svenska.data.impl.QuizRepositoryImpl
import de.ywegel.svenska.data.impl.SearchRepositoryImpl
import de.ywegel.svenska.data.impl.VocabularyRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Singleton
    @Binds
    abstract fun bindVocabularyRepository(repository: VocabularyRepositoryImpl): VocabularyRepository

    @Singleton
    @Binds
    abstract fun bindContainerRepository(repository: ContainerRepositoryImpl): ContainerRepository

    @Singleton
    @Binds
    abstract fun bindSearchRepository(repository: SearchRepositoryImpl): SearchRepository

    @Singleton
    @Binds
    abstract fun bindFavoritesAndPronunciationsRepository(
        repository: FavoritesAndPronunciationsRepositoryImpl,
    ): FavoritesAndPronunciationsRepository

    @Singleton
    @Binds
    abstract fun bindQuizRepository(repository: QuizRepositoryImpl): QuizRepository

    @Singleton
    @Binds
    abstract fun bindFileRepository(repository: FileRepositoryImpl): FileRepository
}
