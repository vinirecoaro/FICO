package com.example.fico.ui.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.model.Budget
import com.example.fico.api.FirebaseAPI
import com.example.fico.api.FormatValuesFromDatabase
import com.example.fico.api.FormatValuesToDatabase
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.math.BigDecimal
import java.math.RoundingMode

class BudgetPerMonthViewModel : ViewModel() {
    private val firebaseAPI = FirebaseAPI.instance
    private val _budgetPerMonthList = MutableLiveData<List<Budget>>()
    val budgetPerMonthList : LiveData<List<Budget>> = _budgetPerMonthList

   fun getBudgetPerMonth() = viewModelScope.async {
       val budgetList = firebaseAPI.getBudgetPerMonth()
       val sortedBudgetList = budgetList.sortedByDescending { it.date }
       val budgetListFormatted  = mutableListOf<Budget>()
       for(budget in sortedBudgetList){
           val budgetDate = firebaseAPI.formatDateForFilterOnExpenseList(budget.date)
           val budgetValue = budget.budget
           val budgetItem = Budget(budgetValue,budgetDate)
           budgetListFormatted.add(budgetItem)
        }
           _budgetPerMonthList.value = budgetListFormatted
        }

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun editBudget(newBudget: String, budget: Budget) : Deferred<Boolean> {
        return viewModelScope.async(Dispatchers.IO) {
            val formattedDate = firebaseAPI.formatDateFromFilterToDatabaseForInfoPerMonth(budget.date)
            val bigNumNewBudget = BigDecimal(newBudget)
            val oldBudget = FormatValuesToDatabase().expensePrice(budget.budget,1)
            val bigNumOldBudget = BigDecimal(oldBudget)
            val correction = bigNumNewBudget.subtract(bigNumOldBudget)
            val newBudgetBigNum = BigDecimal(newBudget).setScale(8, RoundingMode.HALF_UP).toString()
            val currentAvailable = firebaseAPI.getAvailableNow(formattedDate)
            val currentAvalableFormatted = BigDecimal(currentAvailable)
            val newAvailable = currentAvalableFormatted.add(correction)
            val newAvailableFormatted = newAvailable.setScale(8, RoundingMode.HALF_UP).toString()

            firebaseAPI.editBudget(formattedDate, newBudgetBigNum, newAvailableFormatted)
        }
    }
}