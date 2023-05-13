package com.example.fico.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.fico.model.User
import com.example.fico.service.FirebaseAPI
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

class LoginViewModel : ViewModel() {

    private val auth = FirebaseAPI.getInstance()

    fun login(email: String, password: String){
        val user = User(email, password)

        auth.login(user)
            .addOnCompleteListener{ task ->
                if (task.isSuccessful) {
                    if(auth.currentUser()?.isEmailVerified == true){
                        onUserLogged()
                    }else{
                        onUserNotVerified()
                    }

                } else {
                    val message = when (task.exception) {
                        is FirebaseAuthInvalidCredentialsException -> "E-mail ou senha inválidos."
                        else -> "Ocorreu um erro ao realizar o login. Tente novamente mais tarde."
                    }
                    // Chama a função que exibe a mensagem de erro
                    onError(message)
                }
            }
    }

    fun isLogged(){
        val curretUser = auth.currentUser()
        if(curretUser != null){
            onUserLogged()
        }
    }

    var onUserLogged: () -> Unit = {}
    var onUserNotVerified : () -> Unit = {}
    var onError: (String) -> Unit = {}

}