plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

// The Cloud Run worker only builds the Android target (to render Compose UI headlessly
// via Robolectric — see docs/MS-581-headless-ui-render-loop.md). Registering the iOS
// targets forces the Kotlin/Native toolchain (~3 GB extracted) to download during
// configuration even though it is never used on Linux, so the worker skips them by
// passing -Pmediasage.worker=true. Local and CI builds leave the property unset and
// build all targets normally.
val buildIosTargets = providers.gradleProperty("mediasage.worker").orNull != "true"

// Firebase iOS SDK is pulled in via CocoaPods (MS-683) — gated on the config file so a
// contributor without GoogleService-Info.plist (and without CocoaPods installed) still gets a
// working iOS build.
val googleServiceInfoPlistExists = file("../iosApp/GoogleService-Info.plist").exists()

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }

    if (buildIosTargets) {
        listOf(
            iosArm64(),
            iosSimulatorArm64()
        ).forEach { iosTarget ->
            iosTarget.binaries.framework {
                baseName = "Shared"
                isStatic = true
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
                summary = "Media Sage shared KMP module"
                homepage = "https://thecouragepost.app"
                ios.deploymentTarget = "15.0"
                pod("FirebaseAnalytics")
                pod("FirebaseCrashlytics")
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)

            // cryptography-kotlin — portable AES-GCM for the shared reflection-note-key (MS-740)
            implementation(libs.cryptography.core)
            implementation(libs.cryptography.provider.optimal)

            // Ktor Client
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.json)

            // Room
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)

            // Koin
            implementation(libs.koin.core)

            // Supabase
            implementation(libs.supabase.auth)
            implementation(libs.supabase.postgrest)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
            implementation(libs.koin.test)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.okhttp.logging.interceptor)
            // BOM must be `api`, not `implementation` — its version constraints need to be
            // visible when :composeApp resolves its own classpath, since :shared's transitive
            // firebase-analytics/firebase-crashlytics deps carry no version themselves.
            api(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.analytics)
            implementation(libs.firebase.crashlytics)
        }

        if (buildIosTargets) {
            iosMain.dependencies {
                implementation(libs.ktor.client.darwin)
            }
            // Firebase-cinterop iOS actuals only compile against the real pods (podspec/pod
            // install are themselves gated on the plist above) — CI's `:shared:build` has no
            // Firebase secrets, so it needs the no-op fallback actuals instead (MS-683).
            getByName("iosMain") {
                kotlin.srcDir(
                    if (googleServiceInfoPlistExists) "src/iosFirebaseMain/kotlin" else "src/iosNoFirebaseMain/kotlin"
                )
            }
        }
    }
}

android {
    namespace = "com.mediasage.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    // Room KSP processor — target-specific configurations
    add("kspAndroid", libs.androidx.room.compiler)
    if (buildIosTargets) {
        add("kspIosArm64", libs.androidx.room.compiler)
        add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    }
}
