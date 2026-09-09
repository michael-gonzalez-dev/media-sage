import SwiftUI
import ComposeApp
import FirebaseCore

@main
struct iOSApp: App {
    init() {
        FirebaseApp.configure()
        let info = Bundle.main.infoDictionary
        let supabaseUrl = info?["SUPABASE_URL"] as? String ?? ""
        let supabaseAnonKey = info?["SUPABASE_ANON_KEY"] as? String ?? ""
        MainViewControllerKt.doInitKoin(supabaseUrl: supabaseUrl, supabaseAnonKey: supabaseAnonKey)
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
