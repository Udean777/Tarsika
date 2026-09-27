package com.ssajudn.tarsika.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme =
    darkColorScheme(
        primary = TarsikaNightPrimary,
        onPrimary = Color(0xFF442C00),
        primaryContainer = Color(0xFFE5B263),
        onPrimaryContainer = Color(0xFF664400),
        secondary = TarsikaNightSage,
        onSecondary = Color(0xFF243520),
        secondaryContainer = Color(0xFF3A4B34),
        onSecondaryContainer = Color(0xFFA7BB9E),
        tertiary = Color(0xFFCBDACE),
        onTertiary = Color(0xFF26332B),
        tertiaryContainer = Color(0xFFAFBEB3),
        onTertiaryContainer = Color(0xFF404D44),
        background = TarsikaNight,
        onBackground = Color(0xFFE0E3DF),
        surface = TarsikaNightSurface,
        onSurface = Color(0xFFE0E3DF),
        surfaceVariant = TarsikaNightElevated,
        onSurfaceVariant = TarsikaNightMuted,
        surfaceContainerLow = TarsikaNightSurface,
        surfaceContainer = Color(0xFF1C201E),
        surfaceContainerHigh = TarsikaNightElevated,
        surfaceContainerHighest = TarsikaNightHighest,
        outline = Color(0xFF9C8F7F),
        outlineVariant = TarsikaNightLine,
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
    )

private val LightColorScheme =
    lightColorScheme(
        primary = TarsikaForest,
        onPrimary = Color.White,
        primaryContainer = TarsikaForestSoft,
        onPrimaryContainer = Color(0xFF94BAA7),
        secondary = TarsikaSageLeaf,
        onSecondary = Color.White,
        secondaryContainer = TarsikaSage,
        onSecondaryContainer = Color(0xFF596750),
        tertiary = Color(0xFF402A00),
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFF5D3E00),
        onTertiaryContainer = Color(0xFFD9A95D),
        background = TarsikaPaper,
        onBackground = TarsikaInk,
        surface = TarsikaSurface,
        onSurface = TarsikaInk,
        surfaceVariant = Color(0xFFE6E2D9),
        onSurfaceVariant = TarsikaMutedInk,
        surfaceContainerLow = Color(0xFFF7F3EA),
        surfaceContainer = TarsikaSurfaceElevated,
        surfaceContainerHigh = Color(0xFFECE8DF),
        surfaceContainerHighest = Color(0xFFE6E2D9),
        outline = Color(0xFF717974),
        outlineVariant = TarsikaLine,
        error = TarsikaTerracotta,
        onError = Color.White,
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF93000A),
    )

@Composable
fun TarsikaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = TarsikaShapes,
        content = content,
    )
}
