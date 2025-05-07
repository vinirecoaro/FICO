package com.example.fico.presentation.fragments.home

sealed interface HomeExpensesFragmentState<out T> {
    object Loading : HomeExpensesFragmentState<Nothing>
    object Empty : HomeExpensesFragmentState<Nothing>
    data class Success<out T>(val infoPerMonthLists : T) : HomeExpensesFragmentState<T>
    data class Error(val message : String) : HomeExpensesFragmentState<Nothing>
}