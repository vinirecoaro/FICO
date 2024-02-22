package com.example.fico.ui.fragments.expense.add_expense

import com.example.fico.domain.model.ExpenseDomain

sealed interface AddExpenseState {
    object Loading : AddExpenseState
    object Empty : AddExpenseState
    data class Success(val expense : List<ExpenseDomain>) : AddExpenseState
    data class Error(val message : String) : AddExpenseState

}