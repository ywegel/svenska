package de.ywegel.svenska.di

import javax.inject.Qualifier

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class OverviewDataStore

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class AddEditDataStore
