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
            implementation(libs.koin.core)
        }
    }
}

android {
    namespace = "com.dhanu.kurio.ios"
    compileSdk = 35
}
