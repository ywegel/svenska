package de.ywegel.svenska.fakes

import de.ywegel.svenska.analytics.SentryController

class SentryControllerFake : SentryController {
    var isInitialized: Boolean = false
        private set

    override fun initialize() {
        isInitialized = true
    }

    override fun shutdown() {
        isInitialized = false
    }
}
