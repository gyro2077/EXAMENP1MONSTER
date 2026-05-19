package ec.edu.espe.ticketpremium.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val PremiumDarkColorScheme = darkColorScheme(
    primary = Color(0xFFD4AF37),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFFB8860B),
    onPrimaryContainer = Color(0xFF000000),
    secondary = Color(0xFFE8C547),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF3D3520),
    onSecondaryContainer = Color(0xFFFFE066),
    tertiary = Color(0xFFFFD700),
    onTertiary = Color(0xFF000000),
    background = Color(0xFF121212),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFCCCCCC),
    error = Color(0xFFCF6679),
    onError = Color(0xFF000000),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

@Composable
fun TicketPremiumTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.parseColor("#121212")
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = PremiumDarkColorScheme,
        typography = androidx.compose.material3.Typography(),
        content = content
    )
}
