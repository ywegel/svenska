package de.ywegel.svenska.diagnostics

import io.sentry.Breadcrumb
import io.sentry.SentryEvent
import io.sentry.protocol.Device
import io.sentry.protocol.SentryException
import io.sentry.protocol.User
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

class SentryScrubbingTest {

    @Test
    fun `overwrites the sdk installation id on the user`() {
        val event = SentryEvent().apply {
            user = User().apply { id = "sdk-installation-id" }
        }

        event.applyStableUserId("app-generated-id")

        expectThat(event.user?.id).isEqualTo("app-generated-id")
    }

    @Test
    fun `sets a user id even when the event carries none`() {
        val event = SentryEvent()

        event.applyStableUserId("app-generated-id")

        expectThat(event.user?.id).isEqualTo("app-generated-id")
    }

    @Test
    fun `clears the device id, so only the app-generated id identifies the report`() {
        val event = SentryEvent().apply {
            contexts.setDevice(Device().apply { id = "sdk-installation-id" })
        }

        event.applyStableUserId("app-generated-id")

        expectThat(event.contexts.device?.id).isNull()
    }

    @Test
    fun `redacts exception messages on tagged events`() {
        val event = eventWithMessage("failed to parse: hund, hunden, hundar")
        event.setTag(SCRUB_EXCEPTION_MESSAGE_TAG, "true")

        event.redactTaggedExceptionMessages()

        expectThat(event.exceptions?.single()?.value).isEqualTo(REDACTED_MESSAGE)
    }

    @Test
    fun `keeps exception messages on untagged events`() {
        val message = "database is locked"
        val event = eventWithMessage(message)

        event.redactTaggedExceptionMessages()

        expectThat(event.exceptions?.single()?.value).isEqualTo(message)
    }

    @Test
    fun `drops the exception message the logcat instrumentation attaches to breadcrumbs`() {
        val breadcrumb = logcatBreadcrumb().apply {
            setData("throwable", "failed to parse: hund, hunden, hundar")
        }

        breadcrumb.redactLogcatThrowable()

        expectThat(breadcrumb.data["throwable"]).isNull()
    }

    @Test
    fun `keeps the developer-authored breadcrumb message and tag`() {
        val breadcrumb = logcatBreadcrumb().apply {
            setData("throwable", "failed to parse: hund, hunden, hundar")
        }

        breadcrumb.redactLogcatThrowable()

        expectThat(breadcrumb.message).isEqualTo("parseFile: failed (SerializationException)")
        expectThat(breadcrumb.data["tag"]).isEqualTo("FileRepository")
    }

    @Test
    fun `leaves breadcrumbs from other categories untouched`() {
        val breadcrumb = Breadcrumb().apply {
            category = "navigation"
            setData("throwable", "not a logcat breadcrumb")
        }

        breadcrumb.redactLogcatThrowable()

        expectThat(breadcrumb.data["throwable"]).isEqualTo("not a logcat breadcrumb")
    }

    @Test
    fun `drops navigation arguments that are not allowlisted`() {
        val breadcrumb = navigationBreadcrumb(
            "to_arguments" to mutableMapOf<String, Any?>(
                "containerId" to 12,
                "initialVocabulary" to "Vocabulary(word=hund, translation=dog)",
            ),
        )

        breadcrumb.redactNavigationArguments()

        expectThat(breadcrumb.toArguments()).isEqualTo(mapOf("containerId" to 12))
    }

    @Test
    fun `drops disallowed arguments of the previous destination as well`() {
        val breadcrumb = navigationBreadcrumb(
            "from_arguments" to mutableMapOf<String, Any?>(
                "quizMode" to "TranslateWithEndings",
                "searchQuery" to "hund",
            ),
        )

        breadcrumb.redactNavigationArguments()

        expectThat(breadcrumb.fromArguments()).isEqualTo(mapOf("quizMode" to "TranslateWithEndings"))
    }

    @Test
    fun `keeps every allowlisted argument`() {
        val allowed = mutableMapOf<String, Any?>(
            "containerId" to 12,
            "quizMode" to "OnlyEndings",
            "screenType" to "Favorites",
        )
        val breadcrumb = navigationBreadcrumb("to_arguments" to allowed)

        breadcrumb.redactNavigationArguments()

        expectThat(breadcrumb.toArguments()).isEqualTo(allowed)
    }

    @Test
    fun `drops arguments with a null value when they are not allowlisted`() {
        val breadcrumb = navigationBreadcrumb(
            "to_arguments" to mutableMapOf("initialVocabulary" to null),
        )

        breadcrumb.redactNavigationArguments()

        expectThat(breadcrumb.toArguments()).isEqualTo(emptyMap())
    }

    @Test
    fun `scrubs the argument map in place, so the transaction sharing the instance is scrubbed too`() {
        val arguments = mutableMapOf<String, Any?>("containerId" to 12, "initialVocabulary" to "hund")
        val breadcrumb = navigationBreadcrumb("to_arguments" to arguments)

        breadcrumb.redactNavigationArguments()

        expectThat(arguments).isEqualTo(mutableMapOf("containerId" to 12))
    }

    @Test
    fun `keeps the route names and other breadcrumb data`() {
        val breadcrumb = navigationBreadcrumb(
            "to_arguments" to mutableMapOf<String, Any?>("initialVocabulary" to "hund"),
        ).apply {
            setData("from", "/overview_screen")
            setData("to", "/edit_vocabulary_screen")
            setData("state", "navigated")
        }

        breadcrumb.redactNavigationArguments()

        expectThat(breadcrumb.data["from"]).isEqualTo("/overview_screen")
        expectThat(breadcrumb.data["to"]).isEqualTo("/edit_vocabulary_screen")
        expectThat(breadcrumb.data["state"]).isEqualTo("navigated")
    }

    @Test
    fun `leaves argument-shaped data on breadcrumbs of other categories untouched`() {
        val breadcrumb = logcatBreadcrumb().apply {
            setData("to_arguments", mutableMapOf<String, Any?>("initialVocabulary" to "hund"))
        }

        breadcrumb.redactNavigationArguments()

        expectThat(breadcrumb.data["to_arguments"]).isEqualTo(mapOf("initialVocabulary" to "hund"))
    }

    @Test
    fun `does nothing when the navigation breadcrumb carries no arguments`() {
        val breadcrumb = navigationBreadcrumb().apply { setData("to", "/container_screen") }

        breadcrumb.redactNavigationArguments()

        expectThat(breadcrumb.data.toMap()).isEqualTo(mapOf<String, Any>("to" to "/container_screen"))
    }

    @Test
    fun `ignores argument data that is not a map`() {
        val breadcrumb = navigationBreadcrumb("to_arguments" to "containerId=12")

        breadcrumb.redactNavigationArguments()

        expectThat(breadcrumb.data["to_arguments"]).isEqualTo("containerId=12")
    }

    private fun navigationBreadcrumb(vararg data: Pair<String, Any?>) = Breadcrumb().apply {
        type = "navigation"
        category = "navigation"
        data.forEach { (key, value) -> setData(key, value) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun Breadcrumb.toArguments() = data["to_arguments"] as Map<String, Any?>

    @Suppress("UNCHECKED_CAST")
    private fun Breadcrumb.fromArguments() = data["from_arguments"] as Map<String, Any?>

    private fun logcatBreadcrumb() = Breadcrumb().apply {
        category = "Logcat"
        message = "parseFile: failed (SerializationException)"
        setData("tag", "FileRepository")
    }

    private fun eventWithMessage(message: String) = SentryEvent().apply {
        exceptions = listOf(
            SentryException().apply {
                type = "IllegalStateException"
                value = message
            },
        )
    }
}
