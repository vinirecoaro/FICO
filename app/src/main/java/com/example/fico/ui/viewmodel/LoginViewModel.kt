package com.example.fico.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.domain.model.User
import com.example.fico.api.FirebaseAPI
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import kotlinx.coroutines.*

class LoginViewModel : ViewModel() {

    private val firebaseAPI = FirebaseAPI.instance

    suspend fun login(email: String, password: String)=
        viewModelScope.async(Dispatchers.IO){
            val user = User("",email, password)
            var successLogin = CompletableDeferred<Boolean>()
            firebaseAPI.login(user)
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
                val currentUser = firebaseAPI.currentUser()
                if(currentUser?.isEmailVerified == true){
                    onUserLogged()
                }else{
                    onUserNotVerified()
                }
            }
    }

    var onUserLogged: () -> Unit = {}
    var onUserNotVerified : () -> Unit = {}
    var onError: (String) -> Unit = {}

}