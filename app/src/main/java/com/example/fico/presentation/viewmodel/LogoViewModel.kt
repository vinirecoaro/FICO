package com.example.fico.presentation.viewmodel

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.api.FirebaseAPI
import com.example.fico.utils.internet.ConnectionFunctions
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LogoViewModel(
    private val firebaseAPI : FirebaseAPI
) : ViewModel() {

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun isLogged(context : Context) : Deferred<Boolean> {
        val result = CompletableDeferred<Boolean>()
        viewModelScope.async(Dispatchers.IO) {
            val currentUser = firebaseAPI.currentUser()
            if (currentUser != null) {
                if(ConnectionFunctions().internetConnectionVerification(context)){
                    firebaseAPI.updateReferences()
                    try {
                        val providers = firebaseAPI.verifyIfUserExists().await()
                        if (providers.signInMethods?.isNotEmpty() == true) {
                            if(!verifyExistsExpensesPath().await()){
                                updateExpensesDatabasePath().await()
                            }
                        } else {
                            onError("Erro ao verificar o usuário")
                            result.complete(false)
                        }
                    } catch (e: Exception) {
                        onError("Erro ao verificar o usuário")
                        result.complete(false)
                    }
                }
                result.complete(true)
            }
            result.complete(false)
        }
        return result
    }

    var onError: (String) -> Unit = {}

    @RequiresApi(Build.VERSION_CODES.N)
    private suspend fun verifyExistsExpensesPath() : Deferred<Boolean> {
        return viewModelScope.async(Dispatchers.IO){
            firebaseAPI.verifyExistsExpensesPath()
        }
    }

    private fun updateExpensesDatabasePath(): Deferred<Unit> = viewModelScope.async{
        try{
            viewModelScope.launch {
                firebaseAPI.updateExpensePerListInformationPath()
                firebaseAPI.updateDefaultValuesPath()
                firebaseAPI.updateInformationPerMonthPath()
                firebaseAPI.updateTotalExpensePath()
            }
        }catch (e: Exception){

        }
    }

}