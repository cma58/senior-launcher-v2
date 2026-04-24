package com.inclusion.seniorlauncher.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Baseline light color scheme — AA compliant.
 */
private val LightScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = PrimaryBlueOn,
    primaryContainer = PrimaryBlue,
    onPrimaryContainer = PrimaryBlueOn,
    secondary = SuccessGreen,
    onSecondary = PrimaryBlueOn,
    tertiary = WarningAmber,
    onTertiary = PrimaryBlueOn,
    error = SosRed,
    onError = SosRedOn,
    background = SurfaceLight,
    onBackground = PrimaryTextLight,
    surface = SurfaceLight,
    onSurface = PrimaryTextLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = PrimaryTextLight,
    outline = SecondaryTextLight
)

/**
 * Dark color scheme — AAA compliant.
 */
private val DarkScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = PrimaryBlueOn,
    primaryContainer = PrimaryBlue,
    onPrimaryContainer = PrimaryBlueOn,
    secondary = SuccessGreen,
    onSecondary = PrimaryBlueOn,
    tertiary = WarningAmber,
    onTertiary = PrimaryBlueOn,
    error = SosRed,
    onError = SosRedOn,
    background = SurfaceDark,
    onBackground = PrimaryTextDark,
    surface = SurfaceDark,
    onSurface = PrimaryTextDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = PrimaryTextDark,
    outline = SecondaryTextDark
)

/**
 * Pure black/white — for seniors with macular degeneration or very low vision.
 * Activated via settings flag (not auto-detected).
 */
private val HighContrastScheme = darkColorScheme(
    primary = HcPrimary,
    onPrimary = HcSurface,
    secondary = HcPrimary,
    onSecondary = HcSurface,
    error = HcSos,
    onError = HcOnSurface,
    background = HcSurface,
    onBackground = HcOnSurface,
    surface = HcSurface,
    onSurface = HcOnSurface,
    surfaceVariant = HcSurface,
    onSurfaceVariant = HcOnSurface,
    outline = HcOnSurface
)

@Composable
fun SeniorLauncherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    highContrast: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        highContrast -> HighContrastScheme
        darkTheme    -> DarkScheme
        else         -> LightScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Edge-to-edge: system bars become transparent, content draws behind them.
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT

            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme && !highContrast
            insetsController.isAppearanceLightNavigationBars = !darkTheme && !highContrast
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SeniorTypography,
        content = content
    )
}
