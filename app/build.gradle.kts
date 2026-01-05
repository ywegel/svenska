import io.gitlab.arturbosch.detekt.Detekt
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

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
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
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
