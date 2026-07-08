package com.shopverse.cmp.screen.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.shopverse.cmp.core.architecture.onEffect
import com.shopverse.cmp.core.theme.ShopVerseIndigo
import com.shopverse.cmp.screen.Screen
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import kotlinx.coroutines.delay
import shopversecmp.composeapp.generated.resources.Res
import kotlin.time.Duration.Companion.milliseconds

/** Safety net: route forward even if the Lottie asset fails to parse and never reports done. */
private const val SPLASH_TIMEOUT_MS = 5_000L

@Composable
fun SplashRoute(
    navController: NavHostController,
    viewModel: SplashViewModel,
) {
    viewModel.effectFlow.onEffect { effect ->
        when (effect) {
            is SplashEffect.Navigate -> navController.navigate(effect.destination) {
                popUpTo(Screen.Splash) { inclusive = true }
            }
        }
    }
    SplashContent(onFinished = viewModel::onSplashFinished)
}

@Composable
private fun SplashContent(onFinished: () -> Unit) {
    val composition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes("files/splash.json").decodeToString(),
        )
    }
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,
    )

    // Route forward the moment the animation reaches its last frame...
    LaunchedEffect(composition, progress) {
        if (composition != null && progress >= 1f) onFinished()
    }
    // ...or after a timeout, so a bad asset can never trap the user on the splash.
    LaunchedEffect(Unit) {
        delay(SPLASH_TIMEOUT_MS.milliseconds)
        onFinished()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(ShopVerseIndigo),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = rememberLottiePainter(composition = composition, progress = { progress }),
            contentDescription = null,
            modifier = Modifier.fillMaxSize().padding(48.dp),
        )
    }
}
