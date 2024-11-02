package com.example.fico.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.api.FirebaseAPI
import com.example.fico.utils.constants.StringConstants
import kotlinx.coroutines.async

class GeneralConfigurationViewModel(
    private val firebaseAPI : FirebaseAPI
) : ViewModel() {

    val configurationList : MutableList<String> = mutableListOf(
        StringConstants.GENERAL_CONFIGURATION_LIST.PERSONAL_DATA,
        StringConstants.GENERAL_CONFIGURATION_LIST.LOGOUT
    )

    fun logoff() = viewModelScope.async{
        firebaseAPI.logoff()
    }

}