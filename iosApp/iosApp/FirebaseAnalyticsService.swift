import ComposeApp
import FirebaseAnalytics

/// iOS implementation of the shared Kotlin `AnalyticsService`. The Firebase iOS SDK comes in via
/// Swift Package Manager, so Kotlin never links it directly — shared code's events are forwarded here.
final class FirebaseAnalyticsService: NSObject, SharedAnalyticsService {

    func logEvent(name: String, params: [String: String]) {
        Analytics.logEvent(name, parameters: params)
    }

    func logScreenView(screenName: String) {
        Analytics.logEvent(AnalyticsEventScreenView, parameters: [AnalyticsParameterScreenName: screenName])
    }
}
