package com.example.fico.presentation.fragments.expense.expense_home

sealed interface HomeFragmentState {
    object Loading : HomeFragmentState
    object Empty : HomeFragmentState
    data class Success(val expense : List<String>) : HomeFragmentState
    data class Error(val message : String) : HomeFragmentState
}