package com.example.fico.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.model.User
import com.example.fico.service.FirebaseAPI
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LoginViewModel : ViewModel() {

    private val firebaseAPI = FirebaseAPI.instance

    suspend fun login(email: String, password: String)=
        viewModelScope.async(Dispatchers.IO){
            val user = User(email, password)
            val currentUser = firebaseAPI.currentUser()
            firebaseAPI.login(user)
                .addOnCompleteListener{ task ->
                    if (task.isSuccessful) {
                        if(currentUser?.isEmailVerified == true){
                            onUserLogged()
                        }else{
                            onUserNotVerified()
                        }

                    } else {
                        val message = when (task.exception) {
                            is FirebaseAuthInvalidCredentialsException -> "E-mail ou senha inválidos."
                            else -> "Ocorreu um erro ao realizar o login. Tente novamente mais tarde."
                        }
                        onError(message)
                    }
                }


    }

    suspend fun isLogged() {
        viewModelScope.async(Dispatchers.IO) {
            val currentUser = firebaseAPI.currentUser()
            if (currentUser != null) {
                try {
                    val providers = firebaseAPI.verifyIfUserExists().await()
                    if (providers.signInMethods?.isNotEmpty() == true) {
                        onUserLogged()
                    } else {
                        firebaseAPI.logoff()
                        onError("Erro ao verificar o usuário 1")
                    }
                } catch (e: Exception) {
                    firebaseAPI.logoff()
                    onError("Erro ao verificar o usuário 2")
                }
            }
        }
    }


    var onUserLogged: () -> Unit = {}
    var onUserNotVerified : () -> Unit = {}
    var onError: (String) -> Unit = {}

}