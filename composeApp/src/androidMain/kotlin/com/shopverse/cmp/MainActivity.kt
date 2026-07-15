package com.shopverse.cmp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.shopverse.cmp.core.deeplink.DeepLinkLauncher

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent { App() }
        // Cold start via a shopverse:// link — queued until the nav graph is established.
        intent?.dataString?.let(DeepLinkLauncher::enqueue)
    }

    // launchMode="singleTask": a deeplink while the app runs lands here instead of onCreate.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.dataString?.let(DeepLinkLauncher::enqueue)
    }
}
