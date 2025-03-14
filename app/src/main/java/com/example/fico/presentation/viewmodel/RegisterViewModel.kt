package com.example.fico.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.fico.model.User
import com.example.fico.repositories.AuthRepository
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.fico.utils.constants.StringConstants

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
            onError(StringConstants.MESSAGES.UNEXPECTED_ERROR)
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
            is FirebaseAuthInvalidCredentialsException -> StringConstants.MESSAGES.INVALID_CREDENTIALS
            is FirebaseAuthUserCollisionException -> StringConstants.MESSAGES.EMAIL_ALREADY_IN_USE
            is FirebaseAuthWeakPasswordException -> StringConstants.MESSAGES.WEAK_PASSWORD_ERROR
            else -> StringConstants.MESSAGES.REGISTER_ERROR
        }
    }

    var onUserCreated: () -> Unit = {}
    var onError: (String) -> Unit = {}

    var onSendEmailSuccess: () -> Unit = {}
    var onSendEmailFailure: () -> Unit = {}

}