package de.ywegel.svenska

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import de.ywegel.svenska.analytics.SentryConsentObserver
import de.ywegel.svenska.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltAndroidApp
class Application : Application() {

    @Inject
    lateinit var sentryConsentObserver: SentryConsentObserver

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        sentryConsentObserver.start(applicationScope)
    }
}

val jsonConfig = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
}
