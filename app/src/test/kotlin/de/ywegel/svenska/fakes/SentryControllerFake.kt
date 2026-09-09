package de.ywegel.svenska.fakes

import de.ywegel.svenska.diagnostics.SentryController

class SentryControllerFake : SentryController {
    var isInitialized: Boolean = false
        private set

    /** Id of the most recent [initialize] call, or null if it was never initialized. */
    var initializedWithUserId: String? = null
        private set

    /** Number of [shutdown] calls, so tests can tell a surviving observer from a dead one. */
    var shutdownCount: Int = 0
        private set

    override fun initialize(userId: String) {
        isInitialized = true
        initializedWithUserId = userId
    }

    override fun shutdown() {
        isInitialized = false
        shutdownCount++
    }
}
