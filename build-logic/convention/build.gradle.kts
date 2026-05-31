plugins {
    `kotlin-dsl`
    alias(libs.plugins.android.library)
}

dependencies {
    implementation(libs.agp)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.compose.multiplatform.gradle.plugin)
    implementation(libs.compose.compiler.gradle.plugin)
    implementation(libs.ksp.gradle.plugin)
    implementation(libs.kotlin.serialization.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("kurioAndroidLibrary") {
            id = "kurio.android.library"
            implementationClass = "com.dhanu.kurio.buildlogic.AndroidLibraryConventionPlugin"
        }
        register("kurioAndroidFeature") {
            id = "kurio.android.feature"
            implementationClass = "com.dhanu.kurio.buildlogic.AndroidFeatureConventionPlugin"
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
