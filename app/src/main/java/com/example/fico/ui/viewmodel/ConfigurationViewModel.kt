package com.example.fico.ui.viewmodel

import androidx.lifecycle.ViewModel

class ConfigurationViewModel : ViewModel() {
    val configurationList : MutableList<String> = mutableListOf(
        "Dados pessoais",
        "Budget",
    )

}