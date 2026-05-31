plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

kotlin {
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core"))
            implementation(project(":domain"))
            implementation(project(":data"))
            implementation(project(":presentation"))
            implementation(project(":feature-home"))
            implementation(project(":feature-transcription"))
            implementation(project(":feature-history"))
            implementation(project(":feature-models"))
            implementation(project(":feature-settings"))
            implementation(project(":platform-ios"))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
        }
    }
}

android {
    namespace = "com.dhanu.kurio.ios"
    compileSdk = 35
}
