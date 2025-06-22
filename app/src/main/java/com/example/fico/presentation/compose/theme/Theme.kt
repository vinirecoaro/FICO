package com.example.fico.presentation.compose.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import com.example.fico.R

@Composable
    fun Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        darkColorScheme(
            primary = colorResource(id = R.color.blue_200),
            onPrimary = colorResource(id = R.color.white),
            secondary = colorResource(id = R.color.teal_200),
            surface = colorResource(id = R.color.black_100),
            onSurface = colorResource(id = R.color.white),
            background = colorResource(id = R.color.black_500),
        )
    } else {
        lightColorScheme(
            primary = colorResource(id = R.color.blue_400),
            onPrimary = colorResource(id = R.color.white),
            secondary = colorResource(id = R.color.teal_200),
            surface = colorResource(id = R.color.pearl),
            onSurface = colorResource(id = R.color.black),
            background = colorResource(id = R.color.blue_10),
        )
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        shapes = Shapes(),
        content = content
    )
}
