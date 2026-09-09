package de.ywegel.svenska.diagnostics

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sentry.Sentry
import io.sentry.SentryOptions.BeforeBreadcrumbCallback
import io.sentry.SentryOptions.BeforeSendCallback
import io.sentry.SentryOptions.BeforeSendTransactionCallback
import io.sentry.android.core.SentryAndroid
import io.sentry.protocol.User
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wrap Sentry lifecycle. Sentry is disabled on app start and needs to be initialized manually (see
 * `io.sentry.auto-init` in manifest)
 */
interface SentryController {
    /**
     * @param userId pseudonymous id every report is tagged with, see
     * [de.ywegel.svenska.domain.GetOrCreateCrashReportingIdUseCase].
     */
    fun initialize(userId: String)
    fun shutdown()
}

@Singleton
class SentryControllerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : SentryController {

    private val cacheDir = File(context.cacheDir, "svenska-sentry")

    override fun initialize(userId: String) {
        SentryAndroid.init(context) { options ->
            // Set cache path manually. We can then delete the cached reports, if a user opts out of crash reporting.
            // Otherwise, old reports are cached and sent, once the user turns reporting back on.
            options.cacheDirPath = cacheDir.absolutePath

            // Session envelopes are sent directly and never reach beforeSend, so their `did` has
            // to be replaced on the options instead. This lambda runs after the SDK has already
            // defaulted it to the installation id, so assigning here wins.
            options.distinctId = userId

            // Skips the flush wait in `Sentry.close()`, so withdrawal doesn't block on pending
            // uploads. It does not cancel them: the transport still drains whatever is already
            // queued, bounded by `flushTimeoutMillis` (4s, fixed by AndroidOptionsInitializer).
            // Finishing an upload that started while consent was still valid is deliberate; only
            // new reports have to stop, which `Sentry.close()` guarantees.
            options.shutdownTimeoutMillis = 0

            options.beforeSend = BeforeSendCallback { event, _ ->
                event.applyStableUserId(userId)
                event.redactTaggedExceptionMessages()
                event
            }
            // Transactions are sent through beforeSendTransaction and never reach beforeSend, so
            // the sampled performance events need the same identifier treatment.
            options.beforeSendTransaction = BeforeSendTransactionCallback { transaction, _ ->
                transaction.applyStableUserId(userId)
                transaction
            }
            // Logcat exception messages have to be stripped at the point the breadcrumb is recorded.
            options.beforeBreadcrumb = BeforeBreadcrumbCallback { breadcrumb, _ ->
                breadcrumb.redactLogcatThrowable()
                breadcrumb.redactNavigationArguments()
                breadcrumb
            }
        }

        // Set id, even though we overwrite it in beforeSend. This is a fallback for calls that don't invoke beforeSend,
        // to still use our own id.
        Sentry.setUser(User().apply { id = userId })
    }

    override fun shutdown() {
        Sentry.close()

        if (cacheDir.exists() && !cacheDir.deleteRecursively()) {
            Log.w(TAG, "shutdown: Could not delete all cached crash reports")
        }
    }
}

private const val TAG = "SentryController"
