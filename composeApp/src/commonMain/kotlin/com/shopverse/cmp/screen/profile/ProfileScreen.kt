package com.shopverse.cmp.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shopverse.cmp.core.architecture.Route
import com.shopverse.cmp.core.theme.DiscountRed
import com.shopverse.cmp.core.theme.TextGray
import com.shopverse.cmp.model.ThemeMode
import com.shopverse.cmp.model.label
import com.shopverse.cmp.screen.component.ScreenTitle
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import shopversecmp.composeapp.generated.resources.Res
import shopversecmp.composeapp.generated.resources.ic_check
import shopversecmp.composeapp.generated.resources.ic_chevron_end

/**
 * Profile, ported from the Android app's ProfileView: a sectioned settings list (Account,
 * App with the theme picker + version info, and Log out when signed in). The theme picker
 * is the same three-option bottom sheet as Android's ThemeBottomSheetDialog.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileRoute() {
    val viewModel = koinViewModel<ProfileViewModel>()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var themePickerFor by remember { mutableStateOf<ThemeMode?>(null) }

    Route(
        viewModel = viewModel,
        topBar = { ScreenTitle("Profile") },
        onEffect = { effect ->
            when (effect) {
                is ProfileEffect.ShowMessage ->
                    scope.launch { snackbarHostState.showSnackbar(effect.text) }
            }
        },
    ) { model ->
        Column(Modifier.verticalScroll(rememberScrollState())) {
            model.items.forEach { item ->
                when (item) {
                    is ProfileItem.Title -> TitleRow(item.title)
                    is ProfileItem.Navigatable -> ClickableRow(
                        title = item.title,
                        showChevron = true,
                        onClick = { viewModel.onItemClick(item) },
                    )
                    is ProfileItem.Simple -> ClickableRow(
                        title = item.title,
                        titleColor = if (item is ProfileItem.Simple.Logout) {
                            DiscountRed
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        onClick = { viewModel.onItemClick(item) },
                    )
                    is ProfileItem.Theme -> ClickableRow(
                        title = item.title,
                        value = item.value,
                        showChevron = true,
                        onClick = { themePickerFor = item.mode },
                    )
                    is ProfileItem.Info -> InfoRow(title = item.title, value = item.value)
                    is ProfileItem.Separator -> HorizontalDivider(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(start = if (item.isLast) 0.dp else 16.dp),
                    )
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    themePickerFor?.let { current ->
        ModalBottomSheet(
            onDismissRequest = { themePickerFor = null },
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            ThemePickerContent(
                current = current,
                onSelect = { mode ->
                    themePickerFor = null
                    viewModel.setTheme(mode)
                },
            )
        }
    }
}

@Composable
private fun TitleRow(title: String) {
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 36.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = TextGray,
            maxLines = 1,
        )
    }
}

@Composable
private fun ClickableRow(
    title: String,
    titleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onBackground,
    value: String? = null,
    showChevron: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = titleColor,
            maxLines = 1,
            modifier = Modifier.weight(1f).padding(end = 16.dp),
        )
        if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = TextGray,
                maxLines = 1,
                modifier = Modifier.padding(end = 8.dp),
            )
        }
        if (showChevron) {
            Icon(
                painter = painterResource(Res.drawable.ic_chevron_end),
                contentDescription = null,
                tint = TextGray,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun InfoRow(title: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
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

@Composable
private fun ThemePickerContent(
    current: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
) {
    Column(Modifier.padding(bottom = 8.dp)) {
        Text(
            text = "Theme",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
        listOf(ThemeMode.LIGHT, ThemeMode.DARK, ThemeMode.SYSTEM).forEach { mode ->
            val isSelected = mode == current
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 56.dp)
                    .clickable(onClick = { onSelect(mode) })
                    .padding(horizontal = 16.dp),
            ) {
                Text(
                    text = mode.label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 1,
                    modifier = Modifier.weight(1f).padding(end = 16.dp),
                )
                if (isSelected) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_check),
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}
