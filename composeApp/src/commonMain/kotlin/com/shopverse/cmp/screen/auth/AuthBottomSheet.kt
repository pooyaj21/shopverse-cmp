package com.shopverse.cmp.screen.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.shopverse.cmp.core.architecture.onEffect
import com.shopverse.cmp.core.theme.AddToCartBlue
import com.shopverse.cmp.core.theme.DiscountRed
import com.shopverse.cmp.core.theme.TextGray
import org.koin.compose.viewmodel.koinViewModel

/**
 * Login / sign-up sheet, ported from the Android app's AuthBottomSheetFragment + View: a single
 * ModalBottomSheet that flips between Login and Register (name field appears in Register),
 * disables the form while submitting, and shows validation/API errors inline.
 *
 * Callers use it like Android's `ensureUserLogin`: show it when a guest taps a login-gated
 * action, run the action in [onAuthenticated], and drop it via [onDismiss] otherwise.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthBottomSheet(
    onAuthenticated: () -> Unit,
    onDismiss: () -> Unit,
) {
    val viewModel = koinViewModel<AuthViewModel>()
    val model by viewModel.dataFlow.collectAsState()
    val mode = model?.mode ?: AuthMode.Login
    val isSubmitting = model?.isSubmitting ?: false

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    // The Koin ViewModel survives a dismissed sheet; start every opening back at Login.
    LaunchedEffect(Unit) { viewModel.reset() }

    viewModel.effectFlow.onEffect { effect ->
        when (effect) {
            AuthEffect.Completed -> onAuthenticated()
            is AuthEffect.ShowError -> errorText = effect.text
        }
    }

    ModalBottomSheet(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(Modifier.padding(start = 20.dp, end = 20.dp, bottom = 24.dp)) {
            Text(
                text = when (mode) {
                    AuthMode.Login -> "Welcome back"
                    AuthMode.Register -> "Create your account"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = when (mode) {
                    AuthMode.Login -> "Log in to continue shopping."
                    AuthMode.Register -> "Sign up to start shopping."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = TextGray,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
            )

            if (mode == AuthMode.Register) {
                AuthField(
                    value = name,
                    onValueChange = { name = it },
                    hint = "Full name",
                    enabled = !isSubmitting,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                )
            }
            AuthField(
                value = email,
                onValueChange = { email = it },
                hint = "Email",
                enabled = !isSubmitting,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            AuthField(
                value = password,
                onValueChange = { password = it },
                hint = "Password",
                enabled = !isSubmitting,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isPassword = true,
            )

            errorText?.let { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = DiscountRed,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            Button(
                onClick = {
                    errorText = null
                    viewModel.submit(name = name.trim(), email = email.trim(), password = password)
                },
                enabled = !isSubmitting,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AddToCartBlue,
                    contentColor = Color.White,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(48.dp),
            ) {
                Text(
                    text = when (mode) {
                        AuthMode.Login -> "Log in"
                        AuthMode.Register -> "Create account"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
            }

            TextButton(
                onClick = {
                    errorText = null
                    viewModel.switchMode()
                },
                enabled = !isSubmitting,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp),
            ) {
                Text(
                    text = when (mode) {
                        AuthMode.Login -> "Don't have an account? Create one"
                        AuthMode.Register -> "Already have an account? Log in"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    enabled: Boolean,
    keyboardOptions: KeyboardOptions,
    isPassword: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(text = hint, color = TextGray) },
        enabled = enabled,
        singleLine = true,
        keyboardOptions = keyboardOptions,
        visualTransformation = if (isPassword) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
    )
}
