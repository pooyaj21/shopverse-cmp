import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
                // shopverse:// links — Info.plist registers the scheme; the shared
                // DeepLinkLauncher dequeues once the app is established.
                .onOpenURL { url in
                    DeepLinkLauncher.shared.enqueue(uri: url.absoluteString)
                }
        }
    }
}
