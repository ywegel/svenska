package de.ywegel.svenska.analytics

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sentry.Sentry
import io.sentry.android.core.SentryAndroid
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wrap Sentry lifecycle. Sentry is disabled on app start and needs to be initialized manually (see
 * `io.sentry.auto-init` in manifest)
 */
interface SentryController {
    fun initialize()
    fun shutdown()
}

@Singleton
class SentryControllerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : SentryController {

    override fun initialize() {
        SentryAndroid.init(context)
    }

    override fun shutdown() {
        Sentry.close()
    }
}
