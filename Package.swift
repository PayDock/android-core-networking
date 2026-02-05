// swift-tools-version:6.0
import PackageDescription

let package = Package(
    name: "PaydockNetworking",
    platforms: [
        .iOS(.v18) // Match CocoaPods deployment target of iOS 18.0
    ],
    products: [
        .library(
            name: "PaydockNetworking",
            targets: ["PaydockNetworking"]
        ),
    ],
    dependencies: [
        // No external SPM dependencies needed - KMP handles Ktor internally
    ],
    targets: [
        .binaryTarget(
            name: "PaydockNetworking",
            // Will be updated automatically for each release version
            url: "https://github.com/PayDock/android-core-networking/releases/download/1.4.0/PaydockNetworking.xcframework.zip",
            checksum: "2ae2ce502af474688f7ab0e760b41f17cb7956eed2ebf6f3c327de9dab7442e8"
        )
    ]
)
