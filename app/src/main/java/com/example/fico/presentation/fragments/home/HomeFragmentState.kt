package com.example.fico.presentation.fragments.home

sealed interface HomeFragmentState<out T> {
    data object Loading : HomeFragmentState<Nothing>
    data object Empty : HomeFragmentState<Nothing>
    data class Success<out T>(val info : T) : HomeFragmentState<T>
    data class Error(val message : String) : HomeFragmentState<Nothing>
}