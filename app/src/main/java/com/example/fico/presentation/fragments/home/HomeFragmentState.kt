package com.example.fico.presentation.fragments.home

sealed interface HomeFragmentState<out T> {
    object Loading : HomeFragmentState<Nothing>
    object Empty : HomeFragmentState<Nothing>
    data class Success<out T>(val infoPerMonthLists : T) : HomeFragmentState<T>
    data class Error(val message : String) : HomeFragmentState<Nothing>
}