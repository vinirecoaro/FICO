package com.example.fico.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.fico.service.constants.AppConstants

class ConfigurationViewModel : ViewModel() {
    val configurationList : MutableList<String> = mutableListOf(
        AppConstants.CONFIGURATION_LIST.DADOS_PESSOAIS,
        AppConstants.CONFIGURATION_LIST.BUDGET,
    )

}