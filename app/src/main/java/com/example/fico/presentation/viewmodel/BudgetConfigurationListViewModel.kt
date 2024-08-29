package com.example.fico.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.fico.shared.constants.StringConstants

class BudgetConfigurationListViewModel : ViewModel(){
    val budgetConfigurationList : MutableList<String> = mutableListOf(
        StringConstants.EXPENSE_CONFIGURATION_LIST.BUDGET_LIST.DEFAULT_BUDGET,
        StringConstants.EXPENSE_CONFIGURATION_LIST.BUDGET_LIST.BUDGET_PER_MONTH,
    )
}