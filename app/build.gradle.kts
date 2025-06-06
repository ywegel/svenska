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
    id("kotlin-parcelize")
    id("kotlin-kapt")
}

android {
    namespace = "de.ywegel.svenska"
    compileSdk = 35

    defaultConfig {
        applicationId = "de.ywegel.svenska"
        minSdk = 24
        targetSdk = 35
        versionCode = 3
        versionName = "0.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
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
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.about.libraries)
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

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation)

    // Datastore
    implementation(libs.androidx.datastore)

    // About libraries
    implementation(libs.about.libraries.core)
    implementation(libs.about.libraries.compose.m3)

    // Splash screen
    implementation(libs.splish.splash.screen)

    // testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.kotest.assertions)
    testImplementation(libs.mockk)
    testImplementation(libs.assertk)
    testImplementation(libs.turbine)
    testImplementation(libs.strikt)

    // junit5
    testImplementation(libs.junit5.api)
    testRuntimeOnly(libs.junit5.engine)
    testImplementation(libs.junit5.params)
}

kapt {
    correctErrorTypes = true
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
