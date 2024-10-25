package com.example.fico.presentation.fragments.transaction_list

sealed interface TransactionFragmentState<Nothing> {
    object Loading : TransactionFragmentState<Nothing>
    object Empty : TransactionFragmentState<Nothing>
    object Success : TransactionFragmentState<Nothing>
    data class Error(val message : String) : TransactionFragmentState<Nothing>
}