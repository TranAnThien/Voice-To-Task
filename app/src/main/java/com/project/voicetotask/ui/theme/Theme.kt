package com.project.voicetotask.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.project.voicetotask.ui.theme.Background
import com.project.voicetotask.ui.theme.BackgroundDark
import com.project.voicetotask.ui.theme.ErrorContainer
import com.project.voicetotask.ui.theme.ErrorContainerDark
import com.project.voicetotask.ui.theme.ErrorDark
import com.project.voicetotask.ui.theme.InverseOnSurface
import com.project.voicetotask.ui.theme.InversePrimary
import com.project.voicetotask.ui.theme.InverseSurface
import com.project.voicetotask.ui.theme.OnBackground
import com.project.voicetotask.ui.theme.OnBackgroundDark
import com.project.voicetotask.ui.theme.OnError
import com.project.voicetotask.ui.theme.OnErrorContainer
import com.project.voicetotask.ui.theme.OnErrorContainerDark
import com.project.voicetotask.ui.theme.OnErrorDark
import com.project.voicetotask.ui.theme.OnPrimary
import com.project.voicetotask.ui.theme.OnPrimaryContainer
import com.project.voicetotask.ui.theme.OnPrimaryContainerDark
import com.project.voicetotask.ui.theme.OnPrimaryDark
import com.project.voicetotask.ui.theme.OnSecondary
import com.project.voicetotask.ui.theme.OnSecondaryContainer
import com.project.voicetotask.ui.theme.OnSecondaryContainerDark
import com.project.voicetotask.ui.theme.OnSecondaryDark
import com.project.voicetotask.ui.theme.OnSurface
import com.project.voicetotask.ui.theme.OnSurfaceDark
import com.project.voicetotask.ui.theme.OnSurfaceVariant
import com.project.voicetotask.ui.theme.OnSurfaceVariantDark
import com.project.voicetotask.ui.theme.OnTertiary
import com.project.voicetotask.ui.theme.OnTertiaryContainer
import com.project.voicetotask.ui.theme.OnTertiaryContainerDark
import com.project.voicetotask.ui.theme.OnTertiaryDark
import com.project.voicetotask.ui.theme.OutlineDark
import com.project.voicetotask.ui.theme.OutlineVariant
import com.project.voicetotask.ui.theme.OutlineVariantDark
import com.project.voicetotask.ui.theme.Primary
import com.project.voicetotask.ui.theme.PrimaryContainer
import com.project.voicetotask.ui.theme.PrimaryContainerDark
import com.project.voicetotask.ui.theme.PrimaryDark
import com.project.voicetotask.ui.theme.Secondary
import com.project.voicetotask.ui.theme.SecondaryContainer
import com.project.voicetotask.ui.theme.SecondaryContainerDark
import com.project.voicetotask.ui.theme.SecondaryDark
import com.project.voicetotask.ui.theme.SurfaceContainer
import com.project.voicetotask.ui.theme.SurfaceContainerDark
import com.project.voicetotask.ui.theme.SurfaceContainerHigh
import com.project.voicetotask.ui.theme.SurfaceContainerHighDark
import com.project.voicetotask.ui.theme.SurfaceContainerHighest
import com.project.voicetotask.ui.theme.SurfaceContainerHighestDark
import com.project.voicetotask.ui.theme.SurfaceContainerLow
import com.project.voicetotask.ui.theme.SurfaceContainerLowDark
import com.project.voicetotask.ui.theme.SurfaceContainerLowest
import com.project.voicetotask.ui.theme.SurfaceContainerLowestDark
import com.project.voicetotask.ui.theme.SurfaceDark
import com.project.voicetotask.ui.theme.SurfaceVariantDark
import com.project.voicetotask.ui.theme.Tertiary
import com.project.voicetotask.ui.theme.TertiaryContainer
import com.project.voicetotask.ui.theme.TertiaryContainerDark
import com.project.voicetotask.ui.theme.TertiaryDark

private val DarkColorScheme =
  darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    inversePrimary = InversePrimary,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    inverseSurface = InverseSurface,
    inverseOnSurface = InverseOnSurface,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    inversePrimary = InversePrimary,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceContainerHighest,
    onSurfaceVariant = OnSurfaceVariant,
    inverseSurface = InverseSurface,
    inverseOnSurface = InverseOnSurface,
    outline = Outline,
    outlineVariant = OutlineVariant,
    surfaceContainerLowest = SurfaceContainerLowest,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,
  )

@Composable
fun VoiceToTaskTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is turned off by default as per custom instruction
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, shapes = Shapes, content = content)
}
