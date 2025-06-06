package com.example.fico.presentation.viewmodel

import android.content.Context
import android.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.fico.R
import com.example.fico.model.CreditCardColors

class AddCreditCardViewModel(
) : ViewModel() {


    fun getCreditCardColorOptions() : List<CreditCardColors>{
        val colorOptions = listOf(
            CreditCardColors(R.string.dark_red, Color.rgb(100, 0, 0), Color.WHITE),
            CreditCardColors(R.string.dark_green, Color.rgb(0, 100, 0), Color.WHITE),
            CreditCardColors(R.string.dark_blue, Color.rgb(0, 0, 100), Color.WHITE),
            CreditCardColors(R.string.dark_yellow, Color.rgb(255, 193, 7), Color.BLACK),
            CreditCardColors(R.string.magenta, Color.MAGENTA, Color.WHITE),
            CreditCardColors(R.string.purple, Color.rgb(103, 58, 183), Color.WHITE),
            CreditCardColors(R.string.black, Color.BLACK, Color.WHITE),
            CreditCardColors(R.string.white, Color.WHITE, Color.BLACK),
            CreditCardColors(R.string.orange, Color.rgb(255, 152, 0), Color.WHITE),
            CreditCardColors(R.string.light_pink, Color.rgb(255, 167, 197), Color.BLACK),
            CreditCardColors(R.string.light_blue, Color.rgb(123, 194, 250), Color.BLACK)
        )

        return colorOptions
    }
}