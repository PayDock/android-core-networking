enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    // Disable automatic Java toolchain resolution to avoid Foojay service failures
    // CI environments should have Java pre-installed
    // id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "NetworkApplication"
include(":network")
includeBuild("convention-plugins")
