package de.ywegel.svenska

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.serialization.json.Json

@HiltAndroidApp
class Application : Application()

val jsonConfig = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
}
