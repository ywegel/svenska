package de.ywegel.svenska.di

import android.content.ContentResolver
import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.ywegel.svenska.domain.wordImporter.WordParser
import de.ywegel.svenska.domain.wordImporter.WordParserImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WordImportModule {

    @Singleton
    @Binds
    abstract fun bindWordParser(impl: WordParserImpl): WordParser

    companion object {
        @Provides
        fun provideContentResolver(@ApplicationContext context: Context): ContentResolver {
            return context.contentResolver
        }
    }
}
