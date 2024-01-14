package com.example.fico.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.fico.api.FirebaseAPI
import com.example.fico.util.constants.AppConstants

class ExpenseConfigurationViewModel : ViewModel() {

    private val firebaseAPI = FirebaseAPI.instance

    val configurationList : MutableList<String> = mutableListOf(
        AppConstants.EXPENSE_CONFIGURATION_LIST.BUDGET,
        AppConstants.EXPENSE_CONFIGURATION_LIST.CATEGORIES
    )

}