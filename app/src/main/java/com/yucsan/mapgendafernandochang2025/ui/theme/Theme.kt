package com.yucsan.mapgendafernandochang2025.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import android.view.Window
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat


private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,

    // Text colors in dark mode
    onBackground = Color.White,
    onSurface = Color.White

)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF7100D4),
    secondary = Color(0xFF327F16),
    tertiary = Color(0xFFBDBDBE),

    // Text colors in light mode
    onBackground = Color(0xFF212121), // gris oscuro
    onSurface = Color(0xFF212121)    // gris oscuro
)

@Composable
fun MapGendaFernandoChang2025Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Establecer color del sistema (barra de estado)
    if (context is Activity) {
        val window: Window = context.window
        window.statusBarColor = colorScheme.background.toArgb()
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = !darkTheme // 👈 ¡clave para los íconos!
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
