package com.example.fico.presentation.viewmodel

import NetworkConnectionLiveData
import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.DataStoreManager
import com.example.fico.model.User
import com.example.fico.api.FirebaseAPI
import com.example.fico.repositories.AuthRepository
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import kotlinx.coroutines.*

class LoginViewModel(
    private val firebaseAPI : FirebaseAPI,
    application: Application,
    private val dataStore : DataStoreManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    val internetConnection = NetworkConnectionLiveData(application)

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun login(email: String, password: String)=
        viewModelScope.launch{
            val user = User("", email)
            var successLogin = CompletableDeferred<Boolean>()
            firebaseAPI.login2(user, password)
                .addOnCompleteListener{ task ->
                    if (task.isSuccessful) {
                        firebaseAPI.updateReferences()
                        successLogin.complete(true)
                    } else {
                        successLogin.complete(false)
                        val message = when (task.exception) {
                            is FirebaseAuthInvalidCredentialsException -> "E-mail ou senha inválidos."
                            else -> "Ocorreu um erro ao realizar o login. Tente novamente mais tarde."
                        }
                        onError(message)
                    }
                }

            if(successLogin.await()){
                if(!verifyExistsExpensesPath().await()){
                    updateExpensesDatabasePath().await()
                }
                getUserInfo().await()
                val currentUser = firebaseAPI.currentUser()
                if(currentUser?.isEmailVerified == true){
                    onUserLogged()
                }else{
                    onUserNotVerified()
                }
            }
    }

    suspend fun login2(email: String, password: String){
        val user = User("", email)
        try {
            withContext(Dispatchers.IO){
                authRepository.login(user, password).fold(
                    onSuccess = {result ->
                        if(result){
                            onUserLogged()
                        }else{
                            onUserNotVerified()
                        }
                    },
                    onFailure = { error ->
                        //TODO tratar messagem de erro conforme retorno
                        onError(error.message.toString())
                    }
                )
            }
        }catch (e : Exception){

        }
    }

    var onUserLogged: () -> Unit = {}
    var onUserNotVerified : () -> Unit = {}
    var onError: (String) -> Unit = {}

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun verifyExistsExpensesPath() : Deferred<Boolean> {
        return viewModelScope.async(Dispatchers.IO){
            firebaseAPI.verifyExistsExpensesPath()
        }
    }

    fun updateExpensesDatabasePath(): Deferred<Unit> = viewModelScope.async{
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

    private suspend fun getUserInfo(): Deferred<Unit> = withContext(Dispatchers.IO){
        val result = CompletableDeferred<Unit>()
        viewModelScope.launch {
            getUserName().await()
            getUserEmail().await()
            result.complete(Unit)
        }
        return@withContext result
    }

    private suspend fun getUserEmail() : Deferred<Unit> = withContext(Dispatchers.IO){
        val result = CompletableDeferred<Unit>()
            firebaseAPI.getUserEmail().fold(
                onSuccess = { email ->
                    dataStore.updateUserEmail(email)
                    result.complete(Unit)
                },
                onFailure = { error ->
                    Log.e("getUserEmail", error.message.toString())
                    result.complete(Unit)
                }
            )
        return@withContext result
    }

    private suspend fun getUserName() : Deferred<Unit> = withContext(Dispatchers.IO){
        val result = CompletableDeferred<Unit>()
            firebaseAPI.getUserName().fold(
                onSuccess = {name ->
                    dataStore.updateUserName(name)
                    result.complete(Unit)
                },
                onFailure = { error ->
                    Log.e("getUserName", error.message.toString())
                    result.complete(Unit)
                }
            )
        return@withContext result
    }

}