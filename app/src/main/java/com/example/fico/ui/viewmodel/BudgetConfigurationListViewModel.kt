package com.example.fico.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.fico.service.constants.AppConstants

class BudgetConfigurationListViewModel : ViewModel(){
    val budgetConfigurationList : MutableList<String> = mutableListOf(
        AppConstants.CONFIGURATION_LIST.BUDGET_LIST.DEFAULT_BUDGET,
        AppConstants.CONFIGURATION_LIST.BUDGET_LIST.BUDGET_PER_MONTH
    )
}