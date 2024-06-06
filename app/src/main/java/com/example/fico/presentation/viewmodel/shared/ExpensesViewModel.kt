package com.example.fico.presentation.viewmodel.shared

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.DataStoreManager
import com.example.fico.api.FirebaseAPI
import com.example.fico.model.Expense
import com.example.fico.util.constants.AppConstants
import com.google.gson.Gson
import kotlinx.coroutines.async

class ExpensesViewModel(private val dataStore : DataStoreManager,private val firebaseAPI : FirebaseAPI ) : ViewModel() {

    private val _expenseList = MutableLiveData<List<Expense>>()
    val expenseListLiveData : LiveData<List<Expense>> = _expenseList

    fun getExpenseList(filter : String = "") {
        viewModelScope.async{
            val expenses = firebaseAPI.observeExpenseList(filter).await()
            val sortedExpenses = expenses.sortedByDescending { it.id }
            _expenseList.value = sortedExpenses
            dataStore.updateExpenseList(sortedExpenses)
        }
    }

}