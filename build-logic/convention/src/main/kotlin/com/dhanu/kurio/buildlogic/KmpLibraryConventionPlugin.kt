package com.dhanu.kurio.buildlogic

import com.android.build.gradle.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.compose.ComposeExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.multiplatform")
            pluginManager.apply("com.android.library")
            pluginManager.apply("org.jetbrains.compose")
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
            pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")

            val composeDeps = extensions.getByType<ComposeExtension>().dependencies

            extensions.configure<LibraryExtension> {
                compileSdk = 35
                defaultConfig.minSdk = 26
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }
            }

            extensions.configure<KotlinMultiplatformExtension> {
                androidTarget {
                    compilations.all {
                        compileTaskProvider.configure {
                            compilerOptions {
                                jvmTarget.set(JvmTarget.JVM_17)
                            }
                        }
                    }
                }

                listOf(
                    iosX64(),
                    iosArm64(),
                    iosSimulatorArm64()
                ).forEach {
                    it.binaries.framework {
                        baseName = target.name.replaceFirstChar { it.uppercase() }
                        isStatic = true
                    }
                }

                sourceSets.apply {
                    getByName("commonMain") {
                        dependencies {
                            implementation(composeDeps.runtime)
                            implementation(composeDeps.foundation)
                            implementation(composeDeps.material3)
                            implementation(composeDeps.ui)
                            implementation(composeDeps.components.resources)
                        }
                    }

                    getByName("androidMain") {
                        dependencies {
                            implementation(composeDeps.preview)
                        }
                    }

                    getByName("commonTest") {
                        dependencies {
                            implementation("org.jetbrains.kotlin:kotlin-test:2.1.0")
                            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
                        }
                    }
                }
            }
        }
    }
}
