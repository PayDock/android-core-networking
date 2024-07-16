Pod::Spec.new do |spec|
    spec.name                     = 'network'
    spec.version                  = '1.0.0'
    spec.homepage                 = 'https://github.com/PayDock/ios-mobile-sdk'
    spec.source                   = { :http=> ''}
    spec.authors                  = ''
    spec.license                  = ''
    spec.summary                  = 'A versatile and robust networking module for Kotlin Multiplatform (KMP) projects, utilizing Ktor with platform-specific engines. Supports dynamic HTTP client engine creation, including success and failure mock engines, interceptor-based OkHttp for Android, and SSL pinning configurations. This library ensures seamless integration and consistent networking functionality across Android and iOS, making it ideal for both production and testing environments.'
    spec.vendored_frameworks      = 'build/cocoapods/framework/network.framework'
    spec.libraries                = 'c++'
    spec.ios.deployment_target    = '16.0'
                
                
    if !Dir.exist?('build/cocoapods/framework/network.framework') || Dir.empty?('build/cocoapods/framework/network.framework')
        raise "

        Kotlin framework 'network' doesn't exist yet, so a proper Xcode project can't be generated.
        'pod install' should be executed after running ':generateDummyFramework' Gradle task:

            ./gradlew :network:generateDummyFramework

        Alternatively, proper pod installation is performed during Gradle sync in the IDE (if Podfile location is set)"
    end
                
    spec.xcconfig = {
        'ENABLE_USER_SCRIPT_SANDBOXING' => 'NO',
    }
                
    spec.pod_target_xcconfig = {
        'KOTLIN_PROJECT_PATH' => ':network',
        'PRODUCT_MODULE_NAME' => 'network',
    }
                
    spec.script_phases = [
        {
            :name => 'Build network',
            :execution_position => :before_compile,
            :shell_path => '/bin/sh',
            :script => <<-SCRIPT
                if [ "YES" = "$OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED" ]; then
                  echo "Skipping Gradle build task invocation due to OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED environment variable set to \"YES\""
                  exit 0
                fi
                set -ev
                REPO_ROOT="$PODS_TARGET_SRCROOT"
                "$REPO_ROOT/../gradlew" -p "$REPO_ROOT" $KOTLIN_PROJECT_PATH:syncFramework \
                    -Pkotlin.native.cocoapods.platform=$PLATFORM_NAME \
                    -Pkotlin.native.cocoapods.archs="$ARCHS" \
                    -Pkotlin.native.cocoapods.configuration="$CONFIGURATION"
            SCRIPT
        }
    ]
                
end