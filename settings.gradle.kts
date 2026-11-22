pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // Fork of FsrsKt, published to GitHub Packages until upstream publishes to Maven Central.
        // Needs a token with read:packages: locally gpr.user/gpr.key in ~/.gradle/gradle.properties, in CI GITHUB_TOKEN.
        maven("https://maven.pkg.github.com/ywegel/FsrsKt") {
            credentials {
                username = providers.gradleProperty("gpr.user").orNull ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("gpr.key").orNull ?: System.getenv("GITHUB_TOKEN")
            }
            content { includeGroup("one.felsen.fsrskt") }
        }
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}

rootProject.name = "Svenska"
include(":app")
