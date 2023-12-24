package com.example.fico.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.service.FirebaseAPI
import com.example.fico.service.constants.AppConstants
import kotlinx.coroutines.async

class ConfigurationViewModel : ViewModel() {

    private val firebaseAPI = FirebaseAPI.instance

    val configurationList : MutableList<String> = mutableListOf(
        AppConstants.EXPENSE_CONFIGURATION_LIST.BUDGET,
        AppConstants.EXPENSE_CONFIGURATION_LIST.CATEGORIES
    )

    fun logoff() = viewModelScope.async{
        firebaseAPI.logoff()
    }

}