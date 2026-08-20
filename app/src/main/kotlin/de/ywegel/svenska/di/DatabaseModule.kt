package de.ywegel.svenska.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.ywegel.svenska.data.db.ContainerDao
import de.ywegel.svenska.data.db.MIGRATION_1_2
import de.ywegel.svenska.data.db.MIGRATION_2_3
import de.ywegel.svenska.data.db.QuizDao
import de.ywegel.svenska.data.db.SearchDao
import de.ywegel.svenska.data.db.VocabularyDao
import de.ywegel.svenska.data.db.VocabularyDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Singleton
    @Provides
    fun provideVocabularyDatabase(app: Application, dbCallback: VocabularyDatabase.Callback) =
        Room.databaseBuilder(app, VocabularyDatabase::class.java, "vocabulary")
            .addMigrations(MIGRATION_1_2)
            .addMigrations(MIGRATION_2_3)
            .addCallback(dbCallback)
            .build()

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
