package com.example.fico.presentation.viewmodel

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.DataStoreManager
import com.example.fico.api.FirebaseAPI
import com.example.fico.repositories.AuthRepository
import com.example.fico.repositories.TransactionsRepository
import com.example.fico.utils.constants.StringConstants
import com.example.fico.utils.internet.ConnectionFunctions
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LogoViewModel(
    private val authRepository: AuthRepository,
    private val transactionsRepository: TransactionsRepository,
    private val dataStore : DataStoreManager
) : ViewModel() {

    suspend fun isLogged(context : Context) : Deferred<Boolean> {
        val result = CompletableDeferred<Boolean>()
        if(ConnectionFunctions().internetConnectionVerification(context)){
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
        }else{
            onError(StringConstants.MESSAGES.NO_INTERNET_CONNECTION)
            result.complete(false)
        }
        return result
    }

    suspend fun getDataFromDatabase(){
        getExpenseList()
    }

    suspend fun getExpenseList(){
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

    suspend fun getExpenseMonths(){
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

    suspend fun getExpenseInfoPerMonth(){
        transactionsRepository.getExpenseInfoPerMonth().fold(
            onSuccess = { expenseInfoPerMonth ->
                dataStore.updateAndResetInfoPerMonthExpense(expenseInfoPerMonth)
            },
            onFailure = {error ->
                Log.e("LogoViewModel", "Error getting expense info per month: ${error.message}")
            }
        )
    }

    var onError: (String) -> Unit = {}

}