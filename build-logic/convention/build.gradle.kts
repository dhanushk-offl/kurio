plugins {
    `kotlin-dsl`
}

dependencies {
    implementation("com.android.tools.build:gradle:8.7.3")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0")
    implementation("org.jetbrains.compose:compose-gradle-plugin:1.7.3")
    implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.1.0-1.0.29")
    implementation("org.jetbrains.kotlin:kotlin-serialization:2.1.0")
}

gradlePlugin {
    plugins {
        register("kurioAndroidLibrary") {
            id = "kurio.android.library"
            implementationClass = "com.dhanu.kurio.buildlogic.AndroidLibraryConventionPlugin"
        }
        register("kurioKmpLibrary") {
            id = "kurio.kmp.library"
            implementationClass = "com.dhanu.kurio.buildlogic.KmpLibraryConventionPlugin"
        }
        register("kurioKmpFeature") {
            id = "kurio.kmp.feature"
            implementationClass = "com.dhanu.kurio.buildlogic.KmpFeatureConventionPlugin"
        }
    }
}
