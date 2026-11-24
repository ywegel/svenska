package de.ywegel.svenska.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.ywegel.svenska.domain.quiz.DAY_CUTOFF_HOUR
import kotlinx.datetime.TimeZone
import one.felsen.fsrskt.fsrs6.FsrsDayCounter
import one.felsen.fsrskt.fsrs6.FsrsScheduler
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object QuizModule {
    @Singleton
    @Provides
    fun provideTimeZone(): TimeZone = TimeZone.currentSystemDefault()

    @Singleton
    @Provides
    fun provideFsrsScheduler(timeZone: TimeZone) =
        FsrsScheduler(dayCounter = FsrsDayCounter.calendarDays(DAY_CUTOFF_HOUR, timeZone))
}
