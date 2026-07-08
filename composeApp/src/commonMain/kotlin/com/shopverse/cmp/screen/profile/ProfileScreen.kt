package com.shopverse.cmp.screen.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.shopverse.cmp.core.architecture.Route
import com.shopverse.cmp.screen.component.ScreenTitle

@Composable
fun ProfileRoute() {
    Route(topBar = { ScreenTitle("Profile") }) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Account coming soon", color = MaterialTheme.colorScheme.onBackground)
        }
    }
}
