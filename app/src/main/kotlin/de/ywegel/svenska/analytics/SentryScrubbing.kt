package de.ywegel.svenska.analytics

import io.sentry.Breadcrumb
import io.sentry.Sentry
import io.sentry.SentryBaseEvent
import io.sentry.SentryEvent
import io.sentry.protocol.User

/**
 * Marks an event whose exception messages must not leave the device. Set at the capture site by
 * [captureExceptionWithoutMessage] and evaluated in the `beforeSend` hook of [SentryControllerImpl].
 */
const val SCRUB_EXCEPTION_MESSAGE_TAG = "svenska.scrubbed_message"

internal const val REDACTED_MESSAGE = "<redacted: may contain user vocabulary>"

/**
 * Reports [throwable] without its message. Use, when exception messages can contain user data.
 */
fun captureExceptionWithoutMessage(throwable: Throwable) {
    Sentry.captureException(throwable) { scope -> scope.setTag(SCRUB_EXCEPTION_MESSAGE_TAG, "true") }
}

/**
 * Replaces the identifiers the Sentry SDK assigns for the event with [userId]. This skips possible race conditions of
 * [Sentry.setUser].
 */
internal fun SentryBaseEvent.applyStableUserId(userId: String) {
    user = (user ?: User()).apply { id = userId }
    contexts.device?.id = null
}

/**
 * Blanks exception messages on events marked by [captureExceptionWithoutMessage]. Untagged events
 * keep their messages.
 */
internal fun SentryEvent.redactTaggedExceptionMessages() {
    if (getTag(SCRUB_EXCEPTION_MESSAGE_TAG) == null) return
    exceptions?.forEach { exception -> exception.value = REDACTED_MESSAGE }
}

/** Category the Sentry Gradle plugin's logcat instrumentation uses, see `SentryLogcatAdapter`. */
private const val LOGCAT_BREADCRUMB_CATEGORY = "Logcat"

/** Key under which `SentryLogcatAdapter` stores `Throwable.getMessage()`. */
private const val LOGCAT_THROWABLE_KEY = "throwable"

/**
 * **This method was AI generated.**
 *
 * Drops the exception message that the logcat instrumentation attaches to breadcrumbs.
 *
 * `tracingInstrumentation.logcat` rewrites every `android.util.Log` call at INFO or above into
 * `SentryLogcatAdapter`, which copies `Throwable.getMessage()` into `breadcrumb.data["throwable"]`.
 * That path bypasses [redactTaggedExceptionMessages], so without this a scrubbed message still
 * leaves the device as a breadcrumb.
 *
 * Only the message is dropped, because it is all the adapter records of the throwable. Captured
 * exceptions keep type and stack trace on the event; log-only call sites name the class in the log
 * message instead. Breadcrumb messages are kept, which makes "never interpolate user data into a
 * Log message" an invariant this codebase must hold — mask it, as `HighlightUtils` does.
 */
internal fun Breadcrumb.redactLogcatThrowable() {
    if (category != LOGCAT_BREADCRUMB_CATEGORY) return
    data.remove(LOGCAT_THROWABLE_KEY)
}

private const val NAVIGATION_BREADCRUMB_CATEGORY = "navigation"

private val NAVIGATION_ARGUMENT_KEYS = listOf("to_arguments", "from_arguments")

/** Only arguments that don't contain personal data are allowed here. */
private val ALLOWED_NAV_ARGS = setOf("containerId", "quizMode", "screenType")

/**
 * Drops every navigation argument that is not on the [ALLOWED_NAV_ARGS] allowlist.
 * Removal needs to be done in place.
 */
internal fun Breadcrumb.redactNavigationArguments() {
    if (category != NAVIGATION_BREADCRUMB_CATEGORY) return
    NAVIGATION_ARGUMENT_KEYS.forEach { key ->
        (data[key] as? MutableMap<*, *>)
            ?.entries?.removeAll { (k, _) -> k !in ALLOWED_NAV_ARGS }
    }
}
