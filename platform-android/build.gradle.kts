plugins {
    alias(libs.plugins.android.library)
    kotlin("android")
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.dhanu.kurio.android"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":domain"))
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.napier)
    
    // Compose dependencies for platform permissions/overlay UI
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.runtime:runtime:1.7.3")
    implementation("androidx.compose.ui:ui:1.7.3")
    implementation("androidx.compose.foundation:foundation:1.7.3")
    implementation("androidx.compose.animation:animation:1.7.3")
    implementation("androidx.compose.material3:material3:1.3.0")
}

