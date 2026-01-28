package com.example.weibo.core.ui.components

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

data class SystemBarsConfig(
    val immersive: Boolean = true,
    val statusBarColor: Color = Color.Transparent,
    val navigationBarColor: Color = Color.Transparent,
    val statusBarDarkIcons: Boolean? = null,
    val navigationBarDarkIcons: Boolean? = null,
    val autoStatusBarIcons: Boolean = true,
    val autoStatusBarColor: Boolean = true,
    val topBarBackground: TopBarBackground? = null,
    val statusBarIconsFallbackColor: Color = Color.White,
    val statusBarColorFallbackColor: Color = Color.Transparent
)

fun systemBarsConfigForTopBar(
    topBarBackground: TopBarBackground,
    immersive: Boolean = true,
    navigationBarColor: Color = Color.Transparent,
    statusBarIconsFallbackColor: Color = Color.White,
    statusBarColorFallbackColor: Color = Color.Transparent
): SystemBarsConfig {
    return SystemBarsConfig(
        immersive = immersive,
        navigationBarColor = navigationBarColor,
        topBarBackground = topBarBackground,
        statusBarIconsFallbackColor = statusBarIconsFallbackColor,
        statusBarColorFallbackColor = statusBarColorFallbackColor
    )
}

@Composable
fun SetupSystemBars(
    config: SystemBarsConfig,
    onFinalColorCalculated: (Color) -> Unit = {}
) {
    val view = LocalView.current
    val activityFromView = view.context as? Activity
    val activityFromLocalContext = LocalContext.current as? Activity
    val activity = activityFromView ?: activityFromLocalContext

    val computedStatusBarDarkIcons = androidx.compose.runtime.remember(config) {
        androidx.compose.runtime.mutableStateOf<Boolean?>(null)
    }

    val computedStatusBarColor = androidx.compose.runtime.remember(config) {
        androidx.compose.runtime.mutableStateOf<Color?>(null)
    }

    androidx.compose.runtime.LaunchedEffect(
        config.autoStatusBarIcons,
        config.autoStatusBarColor,
        config.topBarBackground,
        config.statusBarIconsFallbackColor,
        config.statusBarColorFallbackColor
    ) {
        computedStatusBarDarkIcons.value = null
        computedStatusBarColor.value = null

        val shouldAutoIcons = config.autoStatusBarIcons && config.statusBarDarkIcons == null
        val shouldAutoColor = config.autoStatusBarColor && config.statusBarColor == Color.Transparent

        if (!shouldAutoIcons && !shouldAutoColor) return@LaunchedEffect

        when (val bg = config.topBarBackground) {
            is TopBarBackground.Solid -> {
                if (shouldAutoIcons) {
                    computedStatusBarDarkIcons.value = shouldUseDarkStatusBarIcons(bg.color)
                }
                if (shouldAutoColor) {
                    computedStatusBarColor.value = bg.color
                }
                return@LaunchedEffect
            }
            is TopBarBackground.Image -> {
                if (activity != null) {
                    val result = computeStatusBarFromPalette(
                        context = activity,
                        imageModel = bg.model,
                        fallbackColor = bg.fallbackColor
                    )
                    if (shouldAutoIcons) {
                        computedStatusBarDarkIcons.value = result.darkIcons
                    }
                    if (shouldAutoColor) {
                        computedStatusBarColor.value = result.backgroundColor
                    }
                    return@LaunchedEffect
                }
            }
            null -> Unit
        }

        if (shouldAutoIcons) {
            computedStatusBarDarkIcons.value = shouldUseDarkStatusBarIcons(config.statusBarIconsFallbackColor)
        }
        if (shouldAutoColor) {
            computedStatusBarColor.value = config.statusBarColorFallbackColor
        }
    }

    val finalStatusBarColor = computedStatusBarColor.value ?: config.statusBarColor
    onFinalColorCalculated(finalStatusBarColor)

    SideEffect {
        val window = activity?.window ?: return@SideEffect

        WindowCompat.setDecorFitsSystemWindows(window, !config.immersive)

        window.statusBarColor = finalStatusBarColor.toArgb()
        window.navigationBarColor = config.navigationBarColor.toArgb()

        val controller = WindowCompat.getInsetsController(window, window.decorView)

        val statusBarIcons = config.statusBarDarkIcons ?: computedStatusBarDarkIcons.value
        statusBarIcons?.let { controller.isAppearanceLightStatusBars = it }

        config.navigationBarDarkIcons?.let { controller.isAppearanceLightNavigationBars = it }
    }
}
