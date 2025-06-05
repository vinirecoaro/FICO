package com.example.fico.presentation.viewmodel

import android.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.fico.model.CreditCardColors

class AddCreditCardViewModel(
) : ViewModel() {

    val colorOptions = listOf(
        CreditCardColors("Vermelho Escuro",  Color.rgb(100, 0, 0), Color.WHITE),
        CreditCardColors("Verde Escuro", Color.rgb(0, 100, 0), Color.WHITE),
        CreditCardColors("Azul Escuro", Color.rgb(0, 0, 100), Color.WHITE),
        CreditCardColors("Amarelo Escuro", Color.rgb(255, 193, 7), Color.BLACK),
        CreditCardColors("Magenta", Color.MAGENTA, Color.WHITE),
        CreditCardColors("Roxo", Color.rgb(103, 58, 183), Color.WHITE)
    )


}