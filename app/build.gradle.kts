import io.gitlab.arturbosch.detekt.Detekt
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.dagger.hilt.android)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.about.libraries)
    alias(libs.plugins.junit5)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
    alias(libs.plugins.room)
    alias(libs.plugins.kotlinx.kover)
    alias(libs.plugins.sentry.io)
    id("kotlin-parcelize")
}

object LocalPropertiesManager {
    private fun getKey(project: Project, keyName: String): String? {
        try {
            val props = Properties().apply {
                load(FileInputStream(project.rootProject.file("local.properties")))
            }
            return props.getProperty(keyName, null)
        } catch (e: Exception) {
            return null
        }
    }

    fun getSentryDsn(project: Project): String? {
        return getKey(project, "sentryDsn")
    }
}

// Resolved once at configuration time so the manifest placeholder (below) and the
// release guard (bottom of this file) agree on the same value.
val releaseSentryDsn: String? = LocalPropertiesManager.getSentryDsn(rootProject) ?: System.getenv("SENTRY_DSN")

android {
    namespace = "de.ywegel.svenska"
    compileSdk = 36

    defaultConfig {
        applicationId = "de.ywegel.svenska"
        minSdk = 24
        targetSdk = 36
        versionCode = libs.versions.app.version.code.get().toInt()
        versionName = libs.versions.app.version.name.get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("KEYSTORE_PATH") ?: "keystore.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            manifestPlaceholders["sentryDsn"] = ""
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // Empty is a safe default here: this only feeds the manifest placeholder, evaluated for every
            // build type on every sync. A missing DSN is caught for real by the guard at the bottom of this
            // file, which only runs when a release artifact is actually assembled.
            manifestPlaceholders["sentryDsn"] = releaseSentryDsn ?: ""
        }
        create("beta") {
            applicationIdSuffix = ".beta"
            versionNameSuffix = "-beat"

            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("debug")
            manifestPlaceholders["sentryDsn"] = ""
        }
    }
    sourceSets {
        getByName("androidTest").assets.srcDir("$projectDir/schemas")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    lint {
        baseline = file("lint-baseline.xml")
        abortOnError = true
        checkAllWarnings = true
        warningsAsErrors = true
        xmlReport = true
        htmlReport = true
        checkDependencies = true
        checkGeneratedSources = true
        enable += listOf(
            "UnusedIds",
            "UnusedResources",
            "UnusedQuantity",
        )
        disable += listOf(
            "AndroidGradlePluginVersion",
            "ComposableLambdaParameterNaming",
            "DuplicateStrings",
            "GradleDependency",
            "NewerVersionAvailable",
        )
        checkGeneratedSources = false
    }
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.kotlinx.serialization)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material3.icons)
    implementation(libs.compose.activity)
    implementation(libs.compose.viewmodel)
    implementation(libs.compose.lifecycle.runtime)

    // Compose navigation
    implementation(libs.compose.navigation.destinations.core)
    ksp(libs.compose.navigation.destinations.ksp)

    // compose tests
    debugImplementation(libs.compose.ui.test.manifest)
    androidTestImplementation(libs.compose.ui.test.junit4)

    // Room
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    testImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.room.testing)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation)

    // Datastore
    implementation(libs.androidx.datastore)

    // About libraries
    implementation(libs.about.libraries.core)
    implementation(libs.about.libraries.compose.m3)

    // Splash screen
    implementation(libs.splish.splash.screen)

    // testing
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.mockk.android)
    testImplementation(libs.assertk)
    testImplementation(libs.turbine)
    testImplementation(libs.robolectric)
    testImplementation(libs.strikt)
    androidTestImplementation(libs.strikt)
    testImplementation(libs.konsist)

    // junit5
    testImplementation(libs.junit5.api)
    androidTestImplementation(libs.junit5.api)
    testRuntimeOnly(libs.junit5.engine)
    testImplementation(libs.junit5.params)

    // Enable junit4 tests
    testImplementation(libs.junit)
    testRuntimeOnly(libs.junit.vintage.engine)
    androidTestImplementation(libs.androidx.test.ext.junit)

    testImplementation(libs.lifecycle.viewmodel.testing)
}

ksp {
    arg("room.generateKotlin", "true")
}

room {
    schemaDirectory("$projectDir/schemas")
}

hilt {
    enableAggregatingTask = true
}

ktlint {
    android = true
    ignoreFailures = false
    version = "1.0.1"
    reporters {
        reporter(ReporterType.CHECKSTYLE)
        reporter(ReporterType.PLAIN)
        reporter(ReporterType.HTML)
    }
}

tasks.withType<Detekt>().configureEach {
    reports {
        xml.required.set(true)
        md.required.set(true)
    }
}

detekt {
    toolVersion = "1.23.8"
    config.setFrom(files("${rootProject.projectDir}/config/detekt/detekt.yml"))
    baseline = file("${rootProject.projectDir}/config/detekt/detekt-baseline.xml")
    buildUponDefaultConfig = true
}

kover {
    reports {
        filters {
            excludes {
                classes(
                    "_generated._ramcosta._composedestinations._moduleregistry.*",
                    "com.ramcosta.composedestinations.generated.*",
                    "dagger.hilt.internal.aggregatedroot.codegen.*",
                    "hilt_aggregated_deps.*",
                    "*_Factory*",
                    "*_Impl*",
                    "*_HiltModules*",
                )
            }
        }
    }
}

sentry {
    org.set("ywegel")
    projectName.set("svenska")
    ignoredBuildTypes.set(listOf("debug"))

    includeSourceContext = true
    includeNativeSources = true
    includeProguardMapping = true
    uploadNativeSymbols = true
    autoUploadProguardMapping = true
    autoUploadNativeSymbols = true
    autoUploadSourceContext = true
}

// Fail loudly if a release artifact gets built without a Sentry DSN, e.g. because a CI secret was
// renamed or dropped. Hooked into manifest processing specifically (not assembleRelease/bundleRelease)
// so it fails before compilation/signing/packaging do any work, and never affects sync or the
// debug/beta variants.
tasks.matching { it.name == "processReleaseManifest" }.configureEach {
    doFirst {
        check(!releaseSentryDsn.isNullOrBlank()) {
            "SENTRY_DSN is missing for the release build. Set 'sentryDsn' in local.properties or the " +
                "SENTRY_DSN environment variable before building a release artifact."
        }
    }
}
