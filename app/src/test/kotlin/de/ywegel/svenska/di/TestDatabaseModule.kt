package de.ywegel.svenska.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import de.ywegel.svenska.data.db.ContainerDao
import de.ywegel.svenska.data.db.QuizDao
import de.ywegel.svenska.data.db.SearchDao
import de.ywegel.svenska.data.db.VocabularyDao
import de.ywegel.svenska.data.db.VocabularyDatabase
import javax.inject.Singleton

// In-memory and without the demo data callback, so every test starts with an empty, isolated database
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [DatabaseModule::class])
object TestDatabaseModule {

    @Singleton
    @Provides
    fun provideVocabularyDatabase(app: Application): VocabularyDatabase =
        Room.inMemoryDatabaseBuilder(app, VocabularyDatabase::class.java).build()

    @Singleton
    @Provides
    fun provideVocabularyDao(db: VocabularyDatabase): VocabularyDao = db.vocabulary()

    @Singleton
    @Provides
    fun provideContainerDao(db: VocabularyDatabase): ContainerDao = db.container()

    @Singleton
    @Provides
    fun provideSearchDao(db: VocabularyDatabase): SearchDao = db.search()

    @Singleton
    @Provides
    fun provideQuizDao(db: VocabularyDatabase): QuizDao = db.quiz()
}
