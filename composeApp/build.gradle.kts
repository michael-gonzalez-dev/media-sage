import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.roborazzi)
}

// Firebase's Android Gradle plugins (google-services, Crashlytics) hard-fail the build if
// google-services.json is absent — gated so CI (ci.yml only assembles a debug APK, never
// launches it) and contributors without the file still get a green build (MS-683).
val googleServicesJsonExists = file("google-services.json").exists()
if (googleServicesJsonExists) {
    apply(plugin = "com.google.gms.google-services")
    apply(plugin = "com.google.firebase.crashlytics")
}

// The Cloud Run worker only builds the Android target (to render Compose UI headlessly
// via Robolectric — see docs/MS-581-headless-ui-render-loop.md). Registering the iOS
// targets forces the Kotlin/Native toolchain (~3 GB extracted) to download during
// configuration even though it is never used on Linux, so the worker skips them by
// passing -Pmediasage.worker=true. Local and CI builds leave the property unset and
// build all targets normally.
val buildIosTargets = providers.gradleProperty("mediasage.worker").orNull != "true"

// Kotlin's CocoaPods plugin requires every KMP module in the iOS framework chain to apply it
// when any one of them (here, :shared) declares pods — this module has no pods of its own.
// Gated the same way as :shared's cocoapods block (MS-683).
val googleServiceInfoPlistExists = file("../iosApp/GoogleService-Info.plist").exists()

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    if (buildIosTargets) {
        listOf(
            iosArm64(),
            iosSimulatorArm64()
        ).forEach { iosTarget ->
            iosTarget.binaries.framework {
                baseName = "ComposeApp"
                isStatic = true
                binaryOption("bundleId", "com.thecouragepost.app")
            }
            iosTarget.compilations.all {
                compileTaskProvider.configure {
                    compilerOptions {
                        freeCompilerArgs.add("-Xexpect-actual-classes")
                    }
                }
            }
        }

        if (googleServiceInfoPlistExists) {
            apply(plugin = "org.jetbrains.kotlin.native.cocoapods")
            extensions.configure<org.jetbrains.kotlin.gradle.plugin.cocoapods.CocoapodsExtension> {
                version = "1.0"
                summary = "Media Sage Compose Multiplatform UI"
                homepage = "https://thecouragepost.app"
                ios.deploymentTarget = "15.0"
            }
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.core.ktx)
        }
        commonMain.dependencies {
            implementation(projects.shared)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.navigation3.ui)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.compottie)
            implementation(libs.compottie.dot)
            implementation(libs.kotlinx.datetime)
            implementation(libs.datastore.preferences)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        androidUnitTest.dependencies {
            // Headless Compose render loop (MS-581): render a composable to a PNG on the
            // JVM via Robolectric — no emulator or device required.
            implementation(libs.roborazzi)
            implementation(libs.roborazzi.compose)
            implementation(libs.roborazzi.junit.rule)
            implementation(libs.robolectric)
            implementation(libs.junit)
            implementation(libs.androidx.testExt.junit)
        }
    }
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}
val serverBaseUrl: String = localProperties.getProperty(
    "server.base.url",
    "http://10.0.2.2:8080"
)
val supabaseUrl: String = localProperties.getProperty("supabase.url", "")
    .ifEmpty { System.getenv("SUPABASE_URL") ?: "" }
val supabaseAnonKey: String = localProperties.getProperty("supabase.anon.key", "")
    .ifEmpty { System.getenv("SUPABASE_ANON_KEY") ?: "" }

android {
    namespace = "com.mediasage"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.mediasage"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "SERVER_BASE_URL", "\"$serverBaseUrl\"")
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseAnonKey\"")
        buildConfigField("boolean", "USE_MOCK_DATA", localProperties.getProperty("use.mock.data", "false"))
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildFeatures {
        buildConfig = true
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
    testOptions {
        unitTests {
            // Robolectric needs merged Android resources to render Compose on the JVM.
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
    coreLibraryDesugaring(libs.android.desugar.jdk)
    // The Firebase BOM's version constraints from :shared's `api platform(...)` weren't
    // propagating into this application module's own classpath resolution — declaring it
    // again here directly resolves the versionless firebase-analytics/firebase-crashlytics
    // transitive dependencies (MS-683).
    implementation(platform(libs.firebase.bom))
}

