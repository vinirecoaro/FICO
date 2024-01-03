package com.example.fico.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.service.FirebaseAPI
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await

class LogoViewModel : ViewModel() {

    private val firebaseAPI = FirebaseAPI.instance

    suspend fun isLogged() : Deferred<Boolean> {
        val result = CompletableDeferred<Boolean>()
        viewModelScope.async(Dispatchers.IO) {
            val currentUser = firebaseAPI.currentUser()
            if (currentUser != null) {
                try {
                    val providers = firebaseAPI.verifyIfUserExists().await()
                    if (providers.signInMethods?.isNotEmpty() == true) {
                        onUserLogged()
                        result.complete(true)
                    } else {
                        firebaseAPI.logoff()
                        onError("Erro ao verificar o usuário 1")
                        result.complete(false)
                    }
                } catch (e: Exception) {
                    firebaseAPI.logoff()
                    onError("Erro ao verificar o usuário 2")
                    result.complete(false)
                }
            }
            result.complete(false)
        }
        return result
    }

    var onUserLogged: () -> Unit = {}
    var onError: (String) -> Unit = {}
}