package de.ywegel.svenska.ui.common

import kotlinx.coroutines.flow.SharingStarted

/**
 * For ViewModel StateFlows: keeps the upstream flows running while the UI collects them, and for 5 seconds after
 * that, so that a configuration change like a rotation doesn't restart them. Once the screen is in the background
 * longer, the database flows stop.
 */
val WhileUiSubscribed: SharingStarted = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000)
