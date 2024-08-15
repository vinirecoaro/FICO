package com.example.fico.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.DataStoreManager
import com.example.fico.model.Budget
import com.example.fico.api.FirebaseAPI
import com.example.fico.api.FormatValuesFromDatabase
import com.example.fico.api.FormatValuesToDatabase
import com.example.fico.model.InformationPerMonthExpense
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.math.BigDecimal
import java.math.RoundingMode

class BudgetPerMonthViewModel(
    private val firebaseAPI : FirebaseAPI,
    private val dataStore : DataStoreManager
) : ViewModel() {

    private val _budgetPerMonthList = MutableLiveData<List<Budget>>()
    val budgetPerMonthList : LiveData<List<Budget>> = _budgetPerMonthList
    private val _editBudgetResult = MutableLiveData<Boolean>()
    val editBudgetResult : LiveData<Boolean> = _editBudgetResult
    
    fun getBudgetPerMonth(){
        viewModelScope.async(Dispatchers.IO){
            val infoPerMonthList = dataStore.getExpenseInfoPerMonth()
            var sortedBudgetList = mutableListOf<Budget>()
            var sortedBudgetListFormatted = mutableListOf<Budget>()
            infoPerMonthList.forEach { infoPerMonth ->
                val budget = Budget(
                    infoPerMonth.budget,
                    infoPerMonth.date
                )
                sortedBudgetList.add(budget)
            }
            sortedBudgetList = sortedBudgetList.sortedByDescending { it.date }.toMutableList()
            sortedBudgetList.forEach { budget ->
                sortedBudgetListFormatted.add(
                    Budget(
                        FormatValuesFromDatabase().price(budget.budget),
                        FormatValuesFromDatabase().formatDateFromFilterToDatabaseForInfoPerMonth(budget.date)
                    )
                )

            }
            _budgetPerMonthList.postValue(sortedBudgetListFormatted)
        }
    }

    suspend fun editBudget(newBudget: String, budget: Budget) {
        //TODO test
        viewModelScope.async(Dispatchers.IO) {
            val formattedDate = firebaseAPI.formatDateFromFilterToDatabaseForInfoPerMonth(budget.date)
            val infoPerMonthDataStore = dataStore.getExpenseInfoPerMonth()
            val infoOfMonth = infoPerMonthDataStore.find { it.date == formattedDate }

            val bigNumNewBudget = BigDecimal(newBudget)
            val oldBudget = FormatValuesToDatabase().expensePrice(budget.budget,1)
            val bigNumOldBudget = BigDecimal(oldBudget)
            val correction = bigNumNewBudget.subtract(bigNumOldBudget)
            val newBudgetBigNum = BigDecimal(newBudget).setScale(8, RoundingMode.HALF_UP).toString()
            val currentAvailable = infoOfMonth!!.availableNow
            val currentAvailableFormatted = BigDecimal(currentAvailable)
            val newAvailable = currentAvailableFormatted.add(correction)
            val newAvailableFormatted = newAvailable.setScale(8, RoundingMode.HALF_UP).toString()

            firebaseAPI.editBudget(formattedDate, newBudgetBigNum, newAvailableFormatted).fold(
                onSuccess = {
                    val updatedInfoPerMonth = infoPerMonthDataStore.toMutableList()
                    updatedInfoPerMonth.removeAll { it.date == formattedDate }
                    updatedInfoPerMonth.add(
                        InformationPerMonthExpense(
                            formattedDate,
                            newAvailableFormatted,
                            newBudgetBigNum,
                            infoOfMonth.monthExpense
                        )
                    )
                    dataStore.updateInfoPerMonthExpense(updatedInfoPerMonth)
                    _editBudgetResult.postValue(true)
                },
                onFailure = {
                    _editBudgetResult.postValue(false)
                }
            )
        }
    }
}