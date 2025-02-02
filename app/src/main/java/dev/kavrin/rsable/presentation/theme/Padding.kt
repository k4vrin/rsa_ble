package dev.kavrin.rsable.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object Padding {
    /** 4dp */
    val extraSmall: Dp = 4.dp
    /** 8dp */
    val small: Dp = 8.dp
    /**12dp*/
    val smallMedium: Dp = 12.dp
    /** 16dp */
    val medium: Dp = 16.dp
    /**24dp*/
    val extraMedium: Dp = 24.dp
    /**32dp*/
    val smallLarge: Dp = 32.dp
    /** 46dp */
    val large: Dp = 48.dp
    /** 64dp */
    val extraLarge: Dp = 64.dp
}

val LocalPadding = compositionLocalOf { Padding }

val MaterialTheme.padding: Padding
    @Composable
    @ReadOnlyComposable
    get() = LocalPadding.current