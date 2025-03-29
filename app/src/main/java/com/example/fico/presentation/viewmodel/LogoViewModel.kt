package com.example.fico.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.fico.DataStoreManager
import com.example.fico.model.Earning
import com.example.fico.repositories.AuthRepository
import com.example.fico.repositories.TransactionsRepository
import com.example.fico.utils.DateFunctions
import com.example.fico.utils.constants.StringConstants
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LogoViewModel(
    private val authRepository: AuthRepository,
    private val transactionsRepository: TransactionsRepository,
    private val dataStore : DataStoreManager
) : ViewModel() {

    suspend fun isLogged() : Deferred<Boolean> {
        val result = CompletableDeferred<Boolean>()
            withContext(Dispatchers.IO){
                try {
                    authRepository.isLogged().fold(
                        onSuccess = { isLogged ->
                            if(isLogged){
                                result.complete(true)
                            }else{
                                result.complete(false)
                            }
                        },
                        onFailure = {
                            onError(StringConstants.MESSAGES.IS_LOGGED_ERROR)
                            result.complete(false)
                        }
                    )
                }catch (e : Exception){
                    onError(StringConstants.MESSAGES.IS_LOGGED_ERROR)
                    result.complete(false)
                }
            }
        return result
    }

    suspend fun getDataFromDatabase(){
        getExpenseList()
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
                getDefaultPaymentDay()
            },
            onFailure = {error ->
                Log.e("LogoViewModel", "Error getting default budget: ${error.message}")
            }
        )
    }

    private suspend fun getDefaultPaymentDay(){
        transactionsRepository.getDefaultPaymentDay().fold(
            onSuccess = { defaultPaymentDay ->
                dataStore.setDefaultPaymentDay(defaultPaymentDay)
                getDaysForClosingBill()
            },
            onFailure = { error ->
                Log.e("LogoViewModel", "Error getting default payment day: ${error.message}")
            }
        )
    }

    private suspend fun getDaysForClosingBill(){
        transactionsRepository.getDaysForClosingBill().fold(
            onSuccess = { daysForClosingBill ->
                dataStore.setDaysForClosingBill(daysForClosingBill)
                getEarningList()
            },
            onFailure = {
                Log.e("LogoViewModel", "Error getting days for closing bill: ${it.message}")
            }
        )
    }

    private suspend fun getEarningList(){
        transactionsRepository.getEarningList().fold(
            onSuccess = { earningList ->
                dataStore.updateAndResetEarningList(earningList)
                val earningMonthsList = getEarningMonthsList(earningList)
                dataStore.updateAndResetEarningMonths(earningMonthsList.toList())
                getRecurringExpensesList()
            },
            onFailure = {
                Log.e("LogoViewModel", "Error getting earning list: ${it.message}")
            }
        )
    }

    private fun getEarningMonthsList(earningList : List<Earning>) : Set<String> {
        val earningMonthsList = mutableSetOf<String>()
        for(earning in earningList){
            earningMonthsList.add(DateFunctions().YYYYmmDDtoYYYYmm(earning.date))
        }
        return earningMonthsList
    }

    private suspend fun getRecurringExpensesList(){
        transactionsRepository.getRecurringExpensesList().fold(
            onSuccess = { recurringExpensesList ->
                dataStore.updateAndResetRecurringExpensesList(recurringExpensesList)
            },
            onFailure = {
                Log.e("LogoViewModel", "Error getting recurring expenses list: ${it.message}")
            }
        )
    }

    var onError: (String) -> Unit = {}

}