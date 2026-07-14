package com.shopverse.cmp.screen.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import shopversecmp.composeapp.generated.resources.Res
import shopversecmp.composeapp.generated.resources.ic_back

/** Top-start back arrow for pushed screens (the tab screens don't need one). */
@Composable
fun BackButton(onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.padding(start = 4.dp)) {
        Icon(
            painter = painterResource(Res.drawable.ic_back),
            contentDescription = "Back",
            tint = MaterialTheme.colorScheme.onBackground,
        )
    }
}
