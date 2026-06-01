plugins {
    kotlin("multiplatform")
}

kotlin {
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        iosMain.dependencies {
            implementation(project(":core"))
            implementation(project(":domain"))
            implementation(libs.koin.core)
        }
    }
}
