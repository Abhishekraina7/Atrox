package com.example.atrox.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.atrox.app.ui.theme.AtroxTypography

// ─── Obsidian Focus Dark Color Scheme ─────────────────────────────────────────
private val ObsidianDarkColorScheme = darkColorScheme(
    primary              = md_primary,
    onPrimary            = md_onPrimary,
    primaryContainer     = md_primaryContainer,
    onPrimaryContainer   = md_onPrimaryContainer,
    secondary            = md_secondary,
    onSecondary          = md_onSecondary,
    secondaryContainer   = md_secondaryContainer,
    onSecondaryContainer = md_onSecondaryContainer,
    background           = md_background,
    onBackground         = md_onBackground,
    surface              = md_surface,
    onSurface            = md_onSurface,
    surfaceVariant       = md_surfaceVariant,
    onSurfaceVariant     = md_onSurfaceVariant,
    outline              = md_outline,
    outlineVariant       = md_outlineVariant,
    error                = md_error,
    onError              = md_onError,
    errorContainer       = md_errorContainer,
    onErrorContainer     = md_onErrorContainer,
    inverseSurface       = md_inverseSurface,
    inverseOnSurface     = md_inverseOnSurface,
    inversePrimary       = md_inversePrimary,
    scrim                = md_scrim,
)

// ─── Extended Color Tokens ─────────────────────────────────────────────────────
// Exposes Atrox-specific tokens that don't map to M3 roles
data class AtroxExtendedColors(
    val cardDefault   : Color = CardDefault,
    val cardElevated  : Color = CardElevated,
    val indigoSoft    : Color = IndigoSoft,
    val indigoDim     : Color = IndigoDim,
    val indigoBorder  : Color = IndigoBorder,
    val borderSubtle  : Color = BorderSubtle,
    val borderDefault : Color = BorderDefault,
    val textTertiary  : Color = TextTertiary,
    val successGreen  : Color = SuccessGreen,
    val warningAmber  : Color = WarningAmber,
    val errorRedDim   : Color = ErrorRedDim,
)

val LocalAtroxColors = staticCompositionLocalOf { AtroxExtendedColors() }

// ─── Theme Entry Point ────────────────────────────────────────────────────────
@Composable
fun AtroxTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = ObsidianDarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // True black status bar — blends with the matte background
            window.statusBarColor = Background.toArgb()
            window.navigationBarColor = Surface.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    CompositionLocalProvider(
        LocalAtroxColors provides AtroxExtendedColors()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = AtroxTypography,
            content     = content,
        )
    }
}

// ─── Convenience Accessor ─────────────────────────────────────────────────────
// Usage in any Composable:
//   val ext = MaterialTheme.atroxColors
//   Box(modifier = Modifier.background(ext.cardDefault)) { ... }

val MaterialTheme.atroxColors: AtroxExtendedColors
    @Composable get() = LocalAtroxColors.current