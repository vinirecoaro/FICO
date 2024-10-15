package com.example.fico.presentation.fragments.expense.transaction_list

sealed interface TransactionFragmentState<out T> {
    object Loading : TransactionFragmentState<Nothing>
    object Empty : TransactionFragmentState<Nothing>
    data class Success<out T>(val infoPerMonthLists : T) : TransactionFragmentState<T>
    data class Error(val message : String) : TransactionFragmentState<Nothing>
}