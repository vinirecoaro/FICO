package com.example.fico.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.model.User
import com.example.fico.api.FirebaseAPI
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import kotlinx.coroutines.*

class LoginViewModel(
    private val firebaseAPI : FirebaseAPI
) : ViewModel() {

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

}