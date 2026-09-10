# MS-683: Integrate Firebase Analytics and Crashlytics

## What was built

Firebase Analytics + Crashlytics SDK integration across Android and iOS, plus three placeholder
events (figure pin, quote memorize, screen view) and a debug-only test-crash button, proving the
plumbing works ahead of the first TestFlight beta.

- `AnalyticsService` interface (`shared/.../data/analytics/`) with a Firebase-backed Android actual
  and a Firebase-cinterop-backed iOS actual, wired into Koin via `sharedModule` — no per-platform
  Koin module needed, following the same pattern as `ReflectionNoteCipher`.
- `expect fun triggerTestCrash(): Nothing` — throws directly, letting each platform's Crashlytics
  native signal/exception handler capture it automatically. No manual "report" API needed.
- Figure-pin and quote-memorize events log from `FigureDetailViewModel` (business-logic byproducts
  the ViewModel already owns). Screen-view events log from a `TrackedNavEntry` wrapper around every
  `NavEntry` in `MediaSageScaffold.kt`, reading `AnalyticsService` via a new `LocalAnalyticsService`
  CompositionLocal — this hybrid split (ViewModel for business events, CompositionLocal for
  UI-owned events) mirrors Google's Now in Android reference app's `AnalyticsHelper` pattern.
- The debug test-crash button in `SettingsScreen` calls `triggerTestCrash()` directly from
  `onClick`, bypassing `onIntent`/ViewModel entirely — it's a synchronous, non-returning system
  action with no state or side effect to produce, so routing it through MVI would be ceremony with
  no benefit (also matches NiA's and Firebase's own crash-test convention).
- iOS pulls the Firebase SDK via CocoaPods (Kotlin's official `native.cocoapods` Gradle plugin) —
  chosen over Swift Package Manager because it auto-generates cinterop bindings with zero manual
  Xcode steps.

## MS-162 closed as wont-do

An older, overlapping ticket (`MS-162`, "Firebase Analytics + Crashlytics — crash reporting and
event tracking") predated this one and MS-696 with a smaller, less-specified event list. Closed as
`wont-do` in favor of MS-683/MS-696's more detailed acceptance criteria.

## Key decisions

**No runtime no-op/Koin-swap, but a compile-time one was still needed**
The initial design assumed gating the `google-services`/Crashlytics Gradle plugins and the
CocoaPods block on config-file existence would be enough to keep CI green without secrets — since
CI never launches the actual app, a missing config file was assumed to only be a *runtime* concern.
That held for Android (the Firebase library deps are normal, unconditional Maven dependencies —
only the *plugin*, which processes `google-services.json` into resources, is gated). It did **not**
hold for iOS: the Kotlin code directly references cinterop-only symbols
(`cocoapods.FirebaseAnalytics.FIRAnalytics`), and those bindings only exist when the CocoaPods block
actually runs. Since `ci.yml`'s `:shared:build` compiles iOS targets too (no
`-Pmediasage.worker=true` flag), this would have broken CI on every PR. Fixed with a genuine
compile-time fallback: `shared/src/iosFirebaseMain/` (the real Firebase-cinterop actuals) and
`shared/src/iosNoFirebaseMain/` (a no-op fallback reusing the same `NoOpAnalyticsService` built for
`LocalAnalyticsService`'s CompositionLocal default) are two mutually-exclusive extra source
directories added to the `iosMain` source set, chosen by the same file-existence check used for the
Gradle plugin gating.

**`-ktx` Firebase artifacts no longer exist in the BOM**
`firebase-analytics-ktx`/`firebase-crashlytics-ktx` (added without an explicit version, relying on
the BOM) failed to resolve with an empty version — Firebase deprecated and removed the separate
`-ktx` artifacts once their Kotlin extensions merged into the base SDKs. Switched to
`firebase-analytics`/`firebase-crashlytics` directly; the same package names
(`com.google.firebase.ktx.Firebase`, `com.google.firebase.analytics.ktx.analytics`) no longer exist
either — replaced with the classic `FirebaseAnalytics.getInstance(FirebaseApp.getInstance().applicationContext)`
static-accessor form, which needs no `Context` parameter threaded through Koin.

**BOM must be `api`, and declared in both `:shared` and `:composeApp`**
A `platform(libs.firebase.bom)` added as `implementation` in `:shared`'s `androidMain` only
constrains `:shared`'s own classpath resolution — the constraint doesn't propagate downstream to
`:composeApp`, which resolves `firebase-analytics`/`firebase-crashlytics` transitively with no
version at all. Fixed by declaring the BOM as `api` in `:shared` **and** redeclaring it directly in
`:composeApp`'s own `dependencies {}` block.

**Local repro caught all three of the above before any CI/live run**
Per the project's "local repro before live run" rule, the whole CocoaPods+cinterop+BOM chain was
verified locally end-to-end (`./gradlew :shared:podspec :composeApp:podspec`, `pod install`,
`:shared:compileKotlinIosSimulatorArm64`, `:composeApp:linkDebugFrameworkIosSimulatorArm64`,
`:composeApp:compileDebugKotlinAndroid`, `:composeApp:assembleDebug`) using a placeholder
`GoogleService-Info.plist` (fake content, real file-existence gate) before ever touching CI config
or spending a real Cloud Run/TestFlight run. All three build failures above (KotlinDependencyHandler's
removed `platform()` overload, the CI-breaking iOS compile-time gap, the missing BOM propagation)
were caught this way, not discovered live.

**CocoaPods vs SPM, revisited mid-implementation**
`pod install` surfaced a real warning: Firebase deprecated CocoaPods distribution for new SDK
versions after October 2026 (this ticket shipped September 2026 — about a month before that
cutoff). Flagged to the user rather than proceeding silently, since it directly affected a decision
they'd already approved. Decision: ship on CocoaPods now — the currently-resolved pods keep working
indefinitely once installed (only *new* Firebase releases stop publishing to CocoaPods), and the
`AnalyticsService` interface already isolates every Firebase-specific line to two files
(`FirebaseAnalyticsService.kt`, `TestCrash.ios.kt` under `iosFirebaseMain/`) — a future CocoaPods→SPM
migration is a contained, low-risk follow-up, not a rewrite.

## Files changed

- `gradle/libs.versions.toml` — Firebase BOM/analytics/crashlytics libraries, `google-services`,
  `firebaseCrashlytics`, `kotlinCocoapods` plugin aliases
- `build.gradle.kts` — new plugin aliases registered `apply false`
- `composeApp/build.gradle.kts` — conditional `google-services`/Crashlytics plugin application,
  minimal `cocoapods {}` block (required by every module in the iOS framework chain), Firebase BOM
- `shared/build.gradle.kts` — conditional `cocoapods {}` block declaring the Firebase pods,
  conditional `iosMain` source directory (real vs. no-op fallback), Firebase BOM (`api`) + libraries
- `shared/src/commonMain/.../data/analytics/{AnalyticsService,TestCrash}.kt` — interface, `expect
  fun`s, `NoOpAnalyticsService`
- `shared/src/androidMain/.../data/analytics/` — Android actuals
- `shared/src/iosFirebaseMain/.../data/analytics/` — real Firebase-cinterop iOS actuals
- `shared/src/iosNoFirebaseMain/.../data/analytics/` — no-op iOS actuals (CI/config-absent fallback)
- `shared/src/commonMain/.../di/SharedModule.kt` — `AnalyticsService` Koin binding
- `composeApp/src/commonMain/kotlin/com/mediasage/LocalAnalyticsService.kt` — new CompositionLocal
- `composeApp/.../App.kt` — provides `LocalAnalyticsService`
- `composeApp/.../navigation/MediaSageScaffold.kt` — `TrackedNavEntry` wrapper for screen-view logging
- `composeApp/.../feature/figures/FigureDetailViewModel.kt` — figure-pin/quote-memorize event logging
- `composeApp/.../feature/settings/SettingsScreen.kt` — debug-only test-crash button
- `composeApp/.../di/AppModule.kt` — `AnalyticsService` injected into `FigureDetailViewModel`
- `composeApp/src/commonTest/.../FigureDetailViewModelTest.kt` — `FakeAnalyticsServiceForFigureDetail`
  + event-logging assertions
- `composeApp/src/commonMain/composeResources/values/strings.xml` — debug section + crash-button strings
- `iosApp/Podfile` — new (this project previously had no CocoaPods integration)
- `iosApp/iosApp.xcodeproj/project.pbxproj` — CocoaPods integration build phases (generated by
  running the real `pod install`, not hand-edited)
- `iosApp/iosApp/iOSApp.swift` — `FirebaseApp.configure()`
- `.gitignore` — Firebase config files, CocoaPods-generated artifacts
- `.github/workflows/testflight.yml` — writes Firebase config from new secrets, generates podspecs,
  runs `pod install` before the build
- `fastlane/Fastfile` — `build_app` now references `iosApp.xcworkspace` instead of `.xcodeproj`

## Manual setup still required (not automated)

1. Create a new Firebase project (e.g. `media-sage-app`) via the Firebase console — kept separate
   from `media-sage-agent` (the agentic pipeline's GCP project).
2. Register an Android app (`com.mediasage`) and an iOS app (`com.thecouragepost.app`); download
   `google-services.json` → `composeApp/google-services.json` and `GoogleService-Info.plist` →
   `iosApp/GoogleService-Info.plist` (both gitignored).
3. Add `GOOGLE_SERVICES_JSON` and `GOOGLE_SERVICE_INFO_PLIST` GitHub Actions secrets (their raw file
   contents) for `testflight.yml` to write before each release build.
4. Locally: `./gradlew :shared:podspec :composeApp:podspec :shared:generateDummyFramework
   :composeApp:generateDummyFramework && cd iosApp && pod install` once the real plist is in place.
