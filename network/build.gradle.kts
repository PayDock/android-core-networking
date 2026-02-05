@file:Suppress("UnusedPrivateProperty")

import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.internal.impldep.org.joda.time.LocalDateTime
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kover) // Test coverage
    // linting
    id("detekt-convention")
    // publishing
    id("github-publish-convention")
    // SPM plugin only when built standalone; when included as composite (e.g. by mobile-sdk-android),
    // convention-plugins are not in scope so we skip it (SDK only needs Android artifact).
    id("maven-central-publish-convention")
}
if (gradle.parent == null) {
    apply(plugin = "spm-publish-convention")
}

val versionName: String by project
val projectDescription: String by project

kotlin {
    applyDefaultHierarchyTemplate()
    androidTarget {
        publishLibraryVariants("release", "debug")
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    // SPM Configuration - XCFramework generation
    targets.withType<KotlinNativeTarget> {
        binaries.framework {
            baseName = "PaydockNetworking"
            isStatic = true
            // Configure for iOS deployment target
            freeCompilerArgs += listOf("-Xbinary=bundleId=com.paydock.core.network")
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Core Ktor - consumers need HttpClient, HttpResponse types
            implementation(libs.ktor.client.core)
            // Serialization - consumers need for their own DTOs
            implementation(libs.ktor.client.serialization.json)
            implementation(libs.ktor.client.content.negotiation)
            // Coroutines - consumers need for async operations
            implementation(libs.kotlinx.coroutines)
            // Testing support - consumers need for mocking
            api(libs.ktor.client.mock)

            // Internal implementation details
            implementation(libs.ktor.client.serialization)
            implementation(libs.ktor.client.logging)
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
        androidMain.dependencies {
            // Testing and debugging support - consumers need these
            api(libs.okhttp3.logging)
            api(libs.okhttp3.mockwebserver)

            // Internal Android implementation details
            implementation(libs.ktor.client.okhttp)
            implementation(libs.ktor.client.logging.jvm)
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
        val simulatorDevice: String = System.getenv("IOS_SIMULATOR_DEVICE")
            ?: System.getenv("SIMULATOR_DEVICE_NAME")
            ?: "iPhone 15 Pro"
        testRuns["test"].deviceId = simulatorDevice
    }
}

// Deploy

android {
    namespace = "com.paydock.core.network"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        // Ensure we're using system Java when toolchain isn't available
        isCoreLibraryDesugaringEnabled = false
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
            all { test ->
                test.testLogging {
                    events("passed", "skipped", "failed", "standardOut", "standardError")
                    showStandardStreams = true
                }
                test.outputs.upToDateWhen { false }
                test.maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
            }
        }
    }
}

// Ensure consistent JVM targets between Java and Kotlin
// Use conditional toolchain configuration to avoid CI failures
if (System.getenv("CI") == null) {
    // Local development: use toolchain for consistency
    kotlin {
        jvmToolchain(17)
    }
} else {
    // CI environment: skip toolchain to avoid Foojay service issues
    logger.info("CI environment detected, skipping Java toolchain configuration")
}

// Kover configuration for test coverage
kover {
    reports {
        total {
            xml {
                onCheck = true
            }
            html {
                onCheck = true
            }
        }
    }
}

// Test configuration
tasks.withType<Test> {
    // Enable parallel execution
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1

    // Configure test output
    testLogging {
        events("passed", "skipped", "failed", "standardOut", "standardError")
        exceptionFormat = TestExceptionFormat.FULL
        showStandardStreams = true
        showCauses = true
        showExceptions = true
        showStackTraces = true
    }

    // Always run tests
    outputs.upToDateWhen { false }

    // JVM test configuration
    jvmArgs("-XX:+EnableDynamicAgentLoading")

    // Generate detailed test reports
    reports {
        junitXml.required.set(true)
        html.required.set(true)
    }

    // Log test progress
    doFirst {
        logger.lifecycle("Starting tests for: $name")
    }

    doLast {
        logger.lifecycle("Completed tests for: $name")
        logger.lifecycle("Test results: ${reports.html.outputLocation.asFile.get().absolutePath}/index.html")
    }
}

// Custom task for comprehensive test reporting
tasks.register("generateTestSummary") {
    group = "verification"
    description = "Generates a comprehensive test summary across all platforms"

    dependsOn("test", "koverXmlReport", "koverHtmlReport")
}

// Task for testing locally (without iOS dependency)
tasks.register("generateTestSummaryLocal") {
    group = "verification"
    description = "Generates a test summary for available platforms (local testing)"

    dependsOn("test", "koverXmlReport", "koverHtmlReport")

    doLast {
        val summaryFile = file("${layout.buildDirectory.get()}/reports/test-summary.html")
        summaryFile.parentFile.mkdirs()

        val testResults = fileTree("${layout.buildDirectory.get()}/test-results").matching {
            include("**/TEST-*.xml")
        }

        val coverageFiles = fileTree("${layout.buildDirectory.get()}/reports/kover").matching {
            include("**/*.xml")
        }

        val iosTestCount = testResults.files.count { it.path.contains("ios") }
        val androidTestCount = testResults.files.count { it.path.contains("android") || it.path.contains("testDebug") }
        val coverageFileCount = coverageFiles.files.size

        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>KMM SDK Test Summary</title>
                <style>
                    body { font-family: Arial, sans-serif; margin: 20px; }
                    .header { background: #f5f5f5; padding: 20px; border-radius: 5px; }
                    .section { margin: 20px 0; padding: 15px; border: 1px solid #ddd; border-radius: 5px; }
                    .success { color: #28a745; }
                    .warning { color: #ffc107; }
                    .error { color: #dc3545; }
                    .platform { background: #e3f2fd; padding: 10px; margin: 10px 0; border-radius: 3px; }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>🧪 KMM SDK Test Summary</h1>
                    <p>Generated: ${LocalDateTime.now()}</p>
                </div>
                
                <div class="section">
                    <h2>📊 Test Results Overview</h2>
                    <div class="platform">
                        <h3>📱 iOS Tests</h3>
                        <p>Test result files found: $iosTestCount</p>
                    </div>
                    <div class="platform">
                        <h3>🤖 Android Tests</h3>
                        <p>Test result files found: $androidTestCount</p>
                    </div>
                </div>
                
                <div class="section">
                    <h2>📈 Coverage Reports</h2>
                    <p>Coverage files generated: $coverageFileCount</p>
                    <p><a href="kover/html/index.html">View HTML Coverage Report</a></p>
                </div>
                
                <div class="section">
                    <h2>📁 Available Reports</h2>
                    <ul>
                        <li><a href="tests/">Unit Test Reports</a></li>
                        <li><a href="kover/">Coverage Reports</a></li>
                    </ul>
                </div>
            </body>
            </html>
        """.trimIndent()

        summaryFile.writeText(htmlContent)
        logger.lifecycle("Test summary generated: ${summaryFile.absolutePath}")
    }
}