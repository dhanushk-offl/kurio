pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "kurio"

includeBuild("build-logic")

include(":core")
include(":domain")
include(":data")
include(":presentation")
include(":feature-home")
include(":feature-transcription")
include(":feature-history")
include(":feature-models")
include(":feature-settings")
include(":platform-android")
include(":platform-ios")
include(":app-android")
