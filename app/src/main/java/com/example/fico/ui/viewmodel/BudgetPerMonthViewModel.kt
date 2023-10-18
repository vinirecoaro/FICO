package com.example.fico.ui.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.model.Budget
import com.example.fico.service.FirebaseAPI
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.text.DecimalFormat

class BudgetPerMonthViewModel : ViewModel() {
    private val firebaseAPI = FirebaseAPI.instance
    private val _budgetPerMonthList = MutableLiveData<List<Budget>>()
    val budgetPerMonthList : LiveData<List<Budget>> = _budgetPerMonthList

   fun getBudgetPerMonth() = viewModelScope.async {
            val budgetList = firebaseAPI.getBudgetPerMonth()
            _budgetPerMonthList.value = budgetList
        }

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun editBudget(newBudget: String, budget: Budget) : Deferred<Boolean> {
        return viewModelScope.async(Dispatchers.IO) {
            val formatNum = DecimalFormat("#.##")
            val formattedBudget = formatNum.format(newBudget.toFloat()).toString().replace(",",".")
            firebaseAPI.editBudget(formattedBudget, budget)
        }
    }
}