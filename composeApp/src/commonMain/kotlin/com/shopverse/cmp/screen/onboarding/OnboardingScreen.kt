package com.shopverse.cmp.screen.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.shopverse.cmp.core.architecture.onEffect
import com.shopverse.cmp.core.theme.ShopVerseIndigo
import com.shopverse.cmp.screen.Screen
import org.jetbrains.compose.resources.painterResource
import shopversecmp.composeapp.generated.resources.Res
import shopversecmp.composeapp.generated.resources.app_logo

@Composable
fun OnboardingRoute(
    navController: NavHostController,
    viewModel: OnboardingViewModel,
) {
    viewModel.effectFlow.onEffect { effect ->
        when (effect) {
            OnboardingEffect.Continue -> navController.navigate(Screen.Main) {
                popUpTo(Screen.Onboarding) { inclusive = true }
            }
        }
    }
    OnboardingContent(onContinue = viewModel::onContinueClick)
}

@Composable
private fun OnboardingContent(onContinue: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ShopVerseIndigo)
            .safeDrawingPadding()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Center the icon + copy in the upper two-thirds; the CTA is pinned to the bottom.
        Spacer(Modifier.weight(1f))
        Image(
            painter = painterResource(Res.drawable.app_logo),
            contentDescription = null,
            modifier = Modifier.size(96.dp).clip(RoundedCornerShape(22.dp)),
        )

        Spacer(Modifier.height(16.dp))
        Text(
            text = "ShopVerse",
            color = Color.White,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(12.dp))
        Text(
            text = "Your portal to infinite gaming worlds. Browse, buy, and play.",
            color = Color.White.copy(alpha = 0.9f),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.weight(1f))
        OutlinedButton(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            border = BorderStroke(1.5.dp, Color.White),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
        ) {
            Text(
                text = "Let's Go",
                modifier = Modifier.padding(vertical = 6.dp),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}
