package com.shopverse.cmp.screen.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.shopverse.cmp.core.architecture.Route
import com.shopverse.cmp.core.theme.DiscountRed
import com.shopverse.cmp.core.theme.TextGray
import com.shopverse.cmp.screen.component.BackButton
import com.shopverse.cmp.screen.component.ScreenTitle
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

/**
 * Account screen, ported from the Android app's AccountView: a rounded card with the saved
 * name/email over a bottom-anchored red Delete Account button, guarded by the same
 * "This can't be undone" confirmation dialog. Deleting pops back to the tab host, where the
 * Profile list refreshes into its logged-out shape.
 */
@Composable
fun AccountRoute(navController: NavHostController) {
    val viewModel = koinViewModel<AccountViewModel>()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Route(
        viewModel = viewModel,
        topBar = {
            Column {
                BackButton(onClick = { navController.popBackStack() })
                ScreenTitle("Profile")
            }
        },
        onRetry = viewModel::refresh,
        onEffect = { effect ->
            when (effect) {
                AccountEffect.AccountDeleted -> navController.popBackStack()
                is AccountEffect.ShowMessage ->
                    scope.launch { snackbarHostState.showSnackbar(effect.text) }
            }
        },
    ) { model ->
        Column(Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                InfoRow(label = "Name", value = model.name ?: "—")
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                InfoRow(label = "Email", value = model.email ?: "—")
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = { showDeleteConfirm = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DiscountRed,
                    contentColor = Color.White,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .height(48.dp),
            ) {
                Text(
                    text = "Delete Account",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete account?") },
            text = {
                Text(
                    "This permanently deletes your account and order history. " +
                        "This can't be undone.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.deleteAccount()
                    },
                ) {
                    Text("Delete", color = DiscountRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            modifier = Modifier.weight(1f).padding(end = 16.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = TextGray,
            maxLines = 1,
        )
    }
}
