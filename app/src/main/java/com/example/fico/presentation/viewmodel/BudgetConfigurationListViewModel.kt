package com.example.fico.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.fico.util.constants.AppConstants

class BudgetConfigurationListViewModel : ViewModel(){
    val budgetConfigurationList : MutableList<String> = mutableListOf(
        AppConstants.EXPENSE_CONFIGURATION_LIST.BUDGET_LIST.DEFAULT_BUDGET,
        AppConstants.EXPENSE_CONFIGURATION_LIST.BUDGET_LIST.BUDGET_PER_MONTH,
    )
}