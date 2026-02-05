import org.gradle.process.ExecOperations
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.GradleException
import java.security.MessageDigest
import javax.inject.Inject

// SPM Publishing Convention - Provides XCFramework generation tasks for Kotlin Multiplatform projects

// Get project properties
val libraryName: String by project

// SPM XCFramework Generation Tasks
abstract class CreateXCFrameworkTask @Inject constructor(
    private val execOperations: ExecOperations
) : DefaultTask() {
    
    @TaskAction
    fun createXCFramework() {
        val buildDir = project.layout.buildDirectory.get().toString()
        val xcframeworkPath = "$buildDir/XCFrameworks/release/PaydockNetworking.xcframework"
        val iosDeviceFrameworkPath = "$buildDir/bin/iosArm64/releaseFramework/PaydockNetworking.framework"
        val iosX64FrameworkPath = "$buildDir/bin/iosX64/releaseFramework/PaydockNetworking.framework"
        val iosSimulatorArm64FrameworkPath = "$buildDir/bin/iosSimulatorArm64/releaseFramework/PaydockNetworking.framework"
        val combinedSimulatorFrameworkPath = "$buildDir/bin/combinedSimulator/PaydockNetworking.framework"
        
        // Remove existing XCFramework if it exists
        project.delete(xcframeworkPath)
        project.delete(combinedSimulatorFrameworkPath)
        
        // Create directory for combined simulator framework
        val combinedSimulatorDir = project.file("$buildDir/bin/combinedSimulator")
        combinedSimulatorDir.mkdirs()
        
        logger.lifecycle("🔨 Creating combined simulator framework...")
        
        // Copy the ARM64 simulator framework as base structure
        project.copy {
            from(iosSimulatorArm64FrameworkPath)
            into(combinedSimulatorDir)
        }
        
        // Ensure the combined framework directory structure exists
        val combinedFrameworkDir = project.file(combinedSimulatorFrameworkPath)
        combinedFrameworkDir.mkdirs()
        
        // Use lipo to create fat binary combining both simulator architectures
        val combinedBinaryPath = "$combinedSimulatorFrameworkPath/PaydockNetworking"
        val iosX64BinaryPath = "$iosX64FrameworkPath/PaydockNetworking"
        val iosSimulatorArm64BinaryPath = "$iosSimulatorArm64FrameworkPath/PaydockNetworking"
        
        // Verify source binaries exist before combining
        if (!project.file(iosX64BinaryPath).exists()) {
            throw GradleException("iOS x64 binary not found at: $iosX64BinaryPath")
        }
        if (!project.file(iosSimulatorArm64BinaryPath).exists()) {
            throw GradleException("iOS Simulator ARM64 binary not found at: $iosSimulatorArm64BinaryPath")
        }
        
        logger.lifecycle("📦 Combining simulator binaries with lipo...")
        execOperations.exec {
            commandLine("lipo", "-create", 
                iosX64BinaryPath,
                iosSimulatorArm64BinaryPath,
                "-output", combinedBinaryPath)
        }
        
        logger.lifecycle("🔨 Creating XCFramework...")
        
        // Create XCFramework with device framework and combined simulator framework
        execOperations.exec {
            commandLine("xcodebuild", "-create-xcframework",
                "-framework", iosDeviceFrameworkPath,
                "-framework", combinedSimulatorFrameworkPath,
                "-output", xcframeworkPath)
        }
        
        logger.lifecycle("✅ XCFramework created successfully at: $xcframeworkPath")
        
        // Clean up temporary combined simulator framework
        project.delete(combinedSimulatorFrameworkPath)
    }
}

tasks.register<CreateXCFrameworkTask>("createXCFramework") {
    group = "spm"
    description = "Creates XCFramework for Swift Package Manager distribution"
    dependsOn("linkReleaseFrameworkIosArm64", "linkReleaseFrameworkIosX64", "linkReleaseFrameworkIosSimulatorArm64")
}

tasks.register<Zip>("packageXCFramework") {
    group = "spm"
    description = "Packages XCFramework as zip for SPM distribution"
    dependsOn("createXCFramework")
    
    from("${layout.buildDirectory.get()}/XCFrameworks/release/")
    include("PaydockNetworking.xcframework/**")
    archiveFileName.set("PaydockNetworking.xcframework.zip")
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))
    
    doLast {
        val zipPath = "${layout.buildDirectory.get()}/distributions/PaydockNetworking.xcframework.zip"
        logger.lifecycle("✅ XCFramework packaged at: $zipPath")
        
        // Calculate checksum for Package.swift
        val zipFile = file(zipPath)
        val checksum = zipFile.readBytes().let { bytes ->
            MessageDigest.getInstance("SHA-256")
                .digest(bytes)
                .joinToString("") { byte: Byte -> "%02x".format(byte) }
        }
        logger.lifecycle("📝 SHA-256 checksum for Package.swift: $checksum")
    }
}
