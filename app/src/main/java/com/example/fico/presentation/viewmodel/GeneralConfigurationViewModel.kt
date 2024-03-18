package com.example.fico.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.api.FirebaseAPI
import com.example.fico.util.constants.AppConstants
import kotlinx.coroutines.async

class GeneralConfigurationViewModel(
    private val firebaseAPI : FirebaseAPI
) : ViewModel() {

    val configurationList : MutableList<String> = mutableListOf(
        AppConstants.GENERAL_CONFIGURATION_LIST.PERSONAL_DATA,
        AppConstants.GENERAL_CONFIGURATION_LIST.LOGOUT
    )

    fun logoff() = viewModelScope.async{
        firebaseAPI.logoff()
    }

}