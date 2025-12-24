package de.ywegel.svenska.di

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.ywegel.svenska.data.ContainerRepository
import de.ywegel.svenska.data.FavoritesAndPronunciationsRepository
import de.ywegel.svenska.data.FileRepository
import de.ywegel.svenska.data.QuizRepository
import de.ywegel.svenska.data.SearchRepository
import de.ywegel.svenska.data.VocabularyRepository
import de.ywegel.svenska.data.db.ContainerDao
import de.ywegel.svenska.data.db.MIGRATION_1_2
import de.ywegel.svenska.data.db.MIGRATION_2_3
import de.ywegel.svenska.data.db.QuizDao
import de.ywegel.svenska.data.db.SearchDao
import de.ywegel.svenska.data.db.VocabularyDao
import de.ywegel.svenska.data.db.VocabularyDatabase
import de.ywegel.svenska.data.impl.ContainerRepositoryImpl
import de.ywegel.svenska.data.impl.FavoritesAndPronunciationsRepositoryImpl
import de.ywegel.svenska.data.impl.FileRepositoryImpl
import de.ywegel.svenska.data.impl.QuizRepositoryImpl
import de.ywegel.svenska.data.impl.SearchRepositoryImpl
import de.ywegel.svenska.data.impl.VocabularyRepositoryImpl
import de.ywegel.svenska.data.preferences.OVERVIEW_PREFERENCES_NAME
import de.ywegel.svenska.domain.wordImporter.WordParser
import de.ywegel.svenska.domain.wordImporter.WordParserImpl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

// TODO: Split into multiple modules
@Suppress("detekt:TooManyFunctions")
@Module
@InstallIn(SingletonComponent::class)
class SvenskaModule {

    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())

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

    @Singleton
    @Provides
    fun provideVocabularyRepository(dao: VocabularyDao): VocabularyRepository = VocabularyRepositoryImpl(dao)

    @Singleton
    @Provides
    fun provideContainerRepository(dao: ContainerDao): ContainerRepository = ContainerRepositoryImpl(dao)

    @Singleton
    @Provides
    fun provideSearchRepository(dao: SearchDao): SearchRepository = SearchRepositoryImpl(dao)

    @Singleton
    @Provides
    fun provideFavoritesAndPronunciationsRepository(dao: VocabularyDao): FavoritesAndPronunciationsRepository =
        FavoritesAndPronunciationsRepositoryImpl(dao)

    @Singleton
    @Provides
    fun provideQuizRepository(dao: QuizDao): QuizRepository = QuizRepositoryImpl(dao)

    @Singleton
    @Provides
    fun provideUserPreferencesDatastore(@ApplicationContext appContext: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler(
                // TODO maybe warn user about corrupted data? Possibly by setting a corruption flag
                produceNewData = { emptyPreferences() },
            ),
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { appContext.preferencesDataStoreFile(OVERVIEW_PREFERENCES_NAME) },
        )
    }

    @Provides
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver {
        return context.contentResolver
    }

    @Provides
    @Singleton
    fun provideWordParser(): WordParser {
        return WordParserImpl()
    }

    @Provides
    @Singleton
    fun provideFileRepository(
        contentResolver: ContentResolver,
        vocabularyRepository: VocabularyRepository,
        containerRepository: ContainerRepository,
        wordParser: WordParser,
    ): FileRepository {
        return FileRepositoryImpl(
            contentResolver = contentResolver,
            vocabularyRepository = vocabularyRepository,
            containerRepository = containerRepository,
            wordParser = wordParser,
        )
    }

    @DefaultDispatcher
    @Provides
    fun providesDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @IoDispatcher
    @Provides
    fun providesIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @MainDispatcher
    @Provides
    fun providesMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    @ImmediateDispatcher
    @Provides
    fun providesImmediateDispatcher(): CoroutineDispatcher = Dispatchers.Main.immediate
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope
