package com.example.fico.presentation.viewmodel.shared

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.DataStoreManager
import com.example.fico.api.FirebaseAPI
import com.example.fico.api.FormatValuesToDatabase
import com.example.fico.model.Earning
import com.example.fico.model.InformationPerMonthExpense
import com.example.fico.model.ValuePerMonth
import com.example.fico.repositories.CreditCardRepository
import com.example.fico.repositories.TransactionsRepository
import com.example.fico.utils.DateFunctions
import com.example.fico.utils.constants.StringConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode

class RemoteDatabaseViewModel(
    private val firebaseAPI: FirebaseAPI,
    private val dataStore : DataStoreManager,
    private val transactionsRepository: TransactionsRepository,
    private val creditCardRepository: CreditCardRepository
) : ViewModel() {

    suspend fun getDataFromDatabase(){
        mainThread()
        secondaryThread()
    }

    private suspend fun mainThread(){
        getExpenseList()
    }

    private suspend fun secondaryThread(){
        withContext(Dispatchers.IO){
            getCreditCardList()
        }
    }

    private suspend fun getExpenseList(){
        transactionsRepository.getExpenseList().fold(
            onSuccess = { expenseList ->
                dataStore.updateAndResetExpenseList(expenseList)
                getExpenseMonths()
            },
            onFailure = {error ->
                Log.e("LogoViewModel", "Error getting expense list: ${error.message}")
            }
        )
    }

    private suspend fun getExpenseMonths(){
        transactionsRepository.getExpenseMonths().fold(
            onSuccess = { expenseMonths ->
                dataStore.updateAndResetExpenseMonths(expenseMonths)
                getExpenseInfoPerMonth()
            },
            onFailure = {error ->
                Log.e("LogoViewModel", "Error getting expense months: ${error.message}")
            }
        )

    }

    private suspend fun getExpenseInfoPerMonth(){
        transactionsRepository.getExpenseInfoPerMonth().fold(
            onSuccess = { expenseInfoPerMonth ->
                dataStore.updateAndResetInfoPerMonthExpense(expenseInfoPerMonth)
                getTotalExpense()
            },
            onFailure = {error ->
                Log.e("LogoViewModel", "Error getting expense info per month: ${error.message}")
            }
        )
    }

    private suspend fun getTotalExpense(){
        transactionsRepository.getTotalExpense().fold(
            onSuccess = { totalExpense ->
                dataStore.updateTotalExpense(totalExpense)
                getDefaultBudget()
            },
            onFailure = {error ->
                Log.e("LogoViewModel", "Error getting total expense: ${error.message}")
            }
        )
    }

    private suspend fun getDefaultBudget(){
        transactionsRepository.getDefaultBudget().fold(
            onSuccess = { defaultBudget ->
                dataStore.updateDefaultBudget(defaultBudget)
                getEarningList()
            },
            onFailure = {error ->
                Log.e("LogoViewModel", "Error getting default budget: ${error.message}")
            }
        )
    }

    private suspend fun getEarningList(){
        transactionsRepository.getEarningList().fold(
            onSuccess = { earningList ->
                dataStore.updateAndResetEarningList(earningList)
                dataStore.updateAndResetEarningMonthInfoList(earningList)
                getRecurringExpensesList()
            },
            onFailure = {
                Log.e("LogoViewModel", "Error getting earning list: ${it.message}")
            }
        )
    }

    private fun getEarningMonthInfoList(earningList : List<Earning>) : List<ValuePerMonth> {
        val earningMonthsList = mutableListOf<ValuePerMonth>()
        for(earning in earningList){
            val month = DateFunctions().YYYYmmDDtoYYYYmm(earning.date)
            val existMonth = earningMonthsList.find { it.month == month }
            if(existMonth != null){
                existMonth.value = BigDecimal(existMonth.value).add(BigDecimal(earning.value)).toString()
            }else{
                earningMonthsList.add(ValuePerMonth(month, earning.value))
            }
        }
        return earningMonthsList
    }

    private suspend fun getRecurringExpensesList(){
        transactionsRepository.getRecurringExpensesList().fold(
            onSuccess = { recurringExpensesList ->
                dataStore.updateAndResetRecurringExpensesList(recurringExpensesList)
                runFirebaseDatabaseFixesToV1()
            },
            onFailure = {
                Log.e("LogoViewModel", "Error getting recurring expenses list: ${it.message}")
            }
        )
    }

    //V0 to V1 just updates the InfoPerMonth and TotalExpense based on expense list
    // from dataStore after been updated with firebase expense list. It was necessary because an
    // error when editing an expense, the error was that the budget that was been setting on updating information
    // per month was just the default and not the budget set for that month

    private fun runFirebaseDatabaseFixesToV1(){
        updateInfoPerMonthAndTotalExpense()
    }

    //Update Info per Month and Total Expense based on dataStore expense list
    private fun updateInfoPerMonthAndTotalExpense(){
        viewModelScope.async {
            if(dataStore.getFirebaseDatabaseFixingVersion() == StringConstants.VERSION.V0){
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
                            dataStore.setFirebaseDatabaseFixingVersion(StringConstants.VERSION.V1)
                            getExpenseList()
                        },
                        onFailure = {
                            Log.e("LogoViewModel", "Error updating info per month and total expense: ${it.message}")
                        }
                    )
            }else{

            }
        }
    }

    private suspend fun getCreditCardList(){
        creditCardRepository.getCreditCardList().fold(
            onSuccess = { creditCardList ->
                dataStore.updateAndResetCreditCardList(creditCardList)
            },
            onFailure = {
                Log.e("LogoViewModel", "Error updating credit card list: ${it.message}")
            }
        )
    }
}