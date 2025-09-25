enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    // Auto-download Java toolchains when required version is missing
    // Prevents CI failures when Java 17 isn't pre-installed
    // Uses Foojay DiscoAPI to resolve and download JDK distributions
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
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
