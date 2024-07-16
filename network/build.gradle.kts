@file:Suppress("UnusedPrivateProperty")
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.cocoapods)
    // linting
    id("detekt-convention")
    // publishing
    id("github-publish-convention")
}

val projectDescription: String by project

kotlin {
    applyDefaultHierarchyTemplate()
    androidTarget {
        publishLibraryVariants("release")
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        summary = projectDescription
        homepage = "https://github.com/PayDock/ios-mobile-sdk"
        version = "1.0.0"
        ios.deploymentTarget = "16.0"
        framework {
            baseName = "network"
            binaryOption("bundleId", "com.paydock.core.$baseName")
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.serialization)
            implementation(libs.ktor.client.serialization.json)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.logging)
            implementation(libs.kotlinx.coroutines)
            // Used to share testing logic
            implementation(libs.ktor.client.mock)
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.ktor.client.logging.jvm)
            implementation(libs.okhttp3.logging)
            implementation(libs.okhttp3.mockwebserver)
            implementation(libs.slf4j.jdk14)
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(libs.mockk)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.kotlin.test.junit)
            }
        }
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by getting {
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val iosTest by getting
    }

    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithSimulatorTests> {
        testRuns["test"].deviceId = "iPhone 14 Pro"
    }
}

android {
    namespace = "com.paydock.core.network"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}