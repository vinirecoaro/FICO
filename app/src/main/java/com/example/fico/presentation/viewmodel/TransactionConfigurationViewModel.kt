package com.example.fico.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.DataStoreManager
import com.example.fico.R
import com.example.fico.api.FirebaseAPI
import com.example.fico.api.FormatValuesToDatabase
import com.example.fico.model.InformationPerMonthExpense
import com.example.fico.model.RecurringTransaction
import com.example.fico.utils.DateFunctions
import kotlinx.coroutines.async
import java.math.BigDecimal
import java.math.RoundingMode

class TransactionConfigurationViewModel(
    context : Context,
    private val firebaseAPI: FirebaseAPI,
    private val dataStore: DataStoreManager
) : ViewModel() {

    private val _updateDatabaseResult = MutableLiveData<Boolean>()
    val updateDatabaseResult : LiveData<Boolean> = _updateDatabaseResult
    private val _recurringTransactionsList = MutableLiveData<Pair<List<RecurringTransaction>, String>>()
    val recurringTransactionsList: LiveData<Pair<List<RecurringTransaction>, String>> = _recurringTransactionsList

    val configurationList: MutableList<String> = mutableListOf(
        context.getString(R.string.credit_card),
        context.getString(R.string.budget_configuration_list),
        context.getString(R.string.recurring_transactions_configuration_list),
        //context.getString(R.string.update_database_info_per_month_and_total_expense),
        context.getString(R.string.security)
    )

    //Update Info per Month and Total Expense based on dataStore expense list
    fun updateInfoPerMonthAndTotalExpense(){
        viewModelScope.async {
            val expenseList = dataStore.getExpenseList()
            var totalExpense = BigDecimal(0)
            var infoPerMonth = mutableListOf<InformationPerMonthExpense>()
            expenseList.forEach { expense ->
                //Total Expense
                val price = BigDecimal(expense.price)
                totalExpense = totalExpense.add(price)

                //InfoPerMonth
                val expenseDateYYYYmm = DateFunctions().YYYYmmDDtoYYYYmm(FormatValuesToDatabase().expenseDate(expense.paymentDate))
                val infoOfMonth = infoPerMonth.find { it.date == expenseDateYYYYmm }
                if(infoOfMonth != null){
                    val updatedMonthExpense = BigDecimal(infoOfMonth.monthExpense).add(BigDecimal(expense.price)).setScale(8, RoundingMode.HALF_UP)
                    val updatedInfoOfMonth = InformationPerMonthExpense(
                        infoOfMonth.date,
                        "0",
                        "0",
                        updatedMonthExpense.toString()
                    )
                    infoPerMonth.removeAll{it.date == updatedInfoOfMonth.date}
                    infoPerMonth.add(updatedInfoOfMonth)
                }else{
                    val updatedInfoOfMonth = InformationPerMonthExpense(
                        DateFunctions().YYYYmmDDtoYYYYmm(FormatValuesToDatabase().expenseDate(expense.paymentDate)),
                        "0",
                        "0",
                        expense.price
                    )
                    infoPerMonth.add(updatedInfoOfMonth)
                }
            }
            val infoPerMonthUpdated = mutableListOf<InformationPerMonthExpense>()
            val infoPerMonthDataStore = dataStore.getExpenseInfoPerMonth()
            val defaultBudget = dataStore.getDefaultBudget()
            infoPerMonth.forEach { monthInfo ->
                val month = infoPerMonthDataStore.find { it.date == monthInfo.date }
                if(month != null){
                    val budget = BigDecimal(month.budget).setScale(8, RoundingMode.HALF_UP)
                    val availableNow = budget.subtract(BigDecimal(monthInfo.monthExpense)).setScale(8, RoundingMode.HALF_UP)
                    infoPerMonthUpdated.add(
                        InformationPerMonthExpense(
                            monthInfo.date,
                            availableNow.toString(),
                            budget.toString(),
                            monthInfo.monthExpense
                        )
                    )
                }else{
                    val budget = BigDecimal(defaultBudget).setScale(8, RoundingMode.HALF_UP)
                    val availableNow = budget.subtract(BigDecimal(monthInfo.monthExpense)).setScale(8, RoundingMode.HALF_UP)
                    infoPerMonthUpdated.add(
                        InformationPerMonthExpense(
                            monthInfo.date,
                            availableNow.toString(),
                            budget.toString(),
                            monthInfo.monthExpense
                        )
                    )
                }
            }
            firebaseAPI.updateInfoPerMonthAndTotalExpense(totalExpense.toString(), infoPerMonthUpdated)
                .fold(
                    onSuccess = {
                        _updateDatabaseResult.postValue(true)
                    },
                    onFailure = {
                        _updateDatabaseResult.postValue(false)
                    }
                )
        }
    }

    fun getRecurringTransactionList(type : String){
        viewModelScope.async {
            try {
                val recurringTransactionsList = dataStore.getRecurringTransactionsList()
                val recurringTransactionTypeFilteredList = recurringTransactionsList.filter { it.type == type }
                val sortedExpenses = recurringTransactionTypeFilteredList.sortedByDescending { it.description }
                val result = Pair(sortedExpenses, type)
                _recurringTransactionsList.value = result

            }catch (error: Exception){
                Log.e("recurring expenses","Fail in get recurring expenses list: ${error.message}")
            }
        }
    }

    /*fun getUploadsFromFileList(){
        viewModelScope.async{
            val test = dataStore.getUploadsFromFileList()
            Log.e("e", test.toString())
        }
    }*/

}