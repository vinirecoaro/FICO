package com.example.fico.presentation.viewmodel

import NetworkConnectionLiveData
import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.DataStoreManager
import com.example.fico.model.User
import com.example.fico.api.FirebaseAPI
import com.example.fico.repositories.AuthRepository
import com.example.fico.repositories.UserDataRepository
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import kotlinx.coroutines.*

class LoginViewModel(
    application: Application,
    private val dataStore : DataStoreManager,
    private val authRepository: AuthRepository,
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    val internetConnection = NetworkConnectionLiveData(application)
    private val _enabledLoginButton = MutableLiveData<Boolean>()
    val enabledLoginButton : LiveData<Boolean> = _enabledLoginButton

    suspend fun login(email: String, password: String){
        val user = User("", email)
        try {
            withContext(Dispatchers.IO){
                authRepository.login(user, password).fold(
                    onSuccess = {result ->
                        if(result){
                            getUserInfo().await()
                            onUserLogged()
                        }else{
                            onUserNotVerified()
                        }
                    },
                    onFailure = { error ->
                        onError(error.message.toString())
                        _enabledLoginButton.postValue(true)
                    }
                )
            }
        }catch (e : Exception){
            onError(e.message.toString())
        }
    }

    var onUserLogged: () -> Unit = {}
    var onUserNotVerified : () -> Unit = {}
    var onError: (String) -> Unit = {}

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
        userDataRepository.getUserEmail().fold(
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
        userDataRepository.getUserName().fold(
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