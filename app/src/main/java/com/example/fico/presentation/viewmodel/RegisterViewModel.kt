package com.example.fico.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.model.User
import com.example.fico.api.FirebaseAPI
import com.example.fico.repositories.AuthRepository
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class RegisterViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    suspend fun register(name : String, email: String, password: String) {
        val user = User(name, email)
        try {
            withContext(Dispatchers.IO){
                authRepository.register(user, password).fold(
                    onSuccess = { onUserCreated() },
                    onFailure = { exception ->
                        val message = errorMessage(exception)
                        onError(message)
                    }
                )
            }
        }catch (exception : Exception){
            onError("Ocorreu um erro inesperado. Tente novamente mais tarde.")
        }
    }

    suspend fun sendVerificationEmail(){
        withContext(Dispatchers.IO){
            authRepository.sendVerificationEmail().fold(
                onSuccess = { onSendEmailSuccess() },
                onFailure = { onSendEmailFailure() }
            )
        }
    }

    private fun errorMessage(exception: Throwable): String {
        return when(exception){
            is FirebaseAuthInvalidCredentialsException ->"E-mail ou senha inválidos."
            is FirebaseAuthUserCollisionException -> "Este e-mail já está em uso."
            is FirebaseAuthWeakPasswordException -> "A senha deve ter pelo menos 6 caracteres."
            else -> "Ocorreu um erro ao criar a conta. Tente novamente mais tarde."
        }
    }

    var onUserCreated: () -> Unit = {}
    var onError: (String) -> Unit = {}

    var onSendEmailSuccess: () -> Unit = {}
    var onSendEmailFailure: () -> Unit = {}

}