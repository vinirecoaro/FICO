package com.example.fico.model

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import com.example.fico.R

enum class CreditCardColors(
    @StringRes val labelRes: Int,
    @ColorInt val background: Int,
    @ColorInt val text: Int
) {
    DARK_RED(R.string.dark_red, Color.rgb(100, 0,0), Color.WHITE),
    DARK_GREEN(R.string.dark_green, Color.rgb(0,100, 0), Color.WHITE),
    DARK_BLUE(R.string.dark_blue, Color.rgb(0,0,100), Color.WHITE),
    DARK_YELLOW(R.string.dark_yellow, Color.rgb(255, 193, 7), Color.BLACK),
    MAGENTA(R.string.magenta, Color.MAGENTA, Color.WHITE),
    PURPLE(R.string.purple, Color.rgb(103, 58, 183), Color.WHITE),
    BLACK(R.string.black, Color.BLACK, Color.WHITE),
    WHITE(R.string.white, Color.WHITE, Color.BLACK),
    ORANGE(R.string.orange, Color.rgb(255, 152, 0), Color.WHITE),
    LIGHT_PINK (R.string.light_pink, Color.rgb(255, 167,197), Color.BLACK),
    LIGHT_BLUE (R.string.light_blue, Color.rgb(123, 194,250), Color.BLACK);

    override fun toString(): String = name
}