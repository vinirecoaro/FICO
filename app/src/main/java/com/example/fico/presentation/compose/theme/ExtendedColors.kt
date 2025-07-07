package com.example.fico.presentation.compose.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class ExtendedColors(
    val customCardBackgroundColor: Color,
    val customCardBackgroundColorSecondary: Color
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        customCardBackgroundColor = Color.Unspecified,
        customCardBackgroundColorSecondary = Color.Unspecified
    )
}
