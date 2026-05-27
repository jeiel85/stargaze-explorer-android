package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val CosmicColorScheme = darkColorScheme(
  primary = CosmicCyan,
  secondary = StarlightGold,
  tertiary = NebulaPink,
  background = DeepSpaceBlack,
  surface = CosmicDarkBlue,
  onPrimary = DeepSpaceBlack,
  onSecondary = DeepSpaceBlack,
  onBackground = StarWhite,
  onSurface = StarWhite,
  surfaceVariant = CosmicSlate,
  onSurfaceVariant = TextPrimary
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme for night stargazing
  dynamicColor: Boolean = false, // Disable dynamic colors to preserve our unique custom cosmic look
  content: @Composable () -> Unit,
) {
  val colorScheme = CosmicColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
