package com.example.fico.presentation.viewmodel

import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.model.User
import com.example.fico.api.FirebaseAPI
import com.example.fico.repositories.AuthRepository
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class RegisterViewModel(
    private val firebaseAPI : FirebaseAPI,
    private val authRepository: AuthRepository
) : ViewModel() {

    fun checkFields(btn: Button, vararg fields : EditText): Boolean {
        val nFileds = fields.size
        var counter = 0
        for (i in fields){
            if (i.text.isEmpty()){
                emptyField(btn, i)
                return false
            }else{
                counter++
            }
        }
        return counter == nFileds
    }

    fun emptyField(btn: Button ,text: EditText){
        val snackbar = Snackbar.make(btn, "O campo ${text.hint} está vazio, preencha-o", Snackbar.LENGTH_LONG)
        snackbar.show()
    }

    fun setUserName(name : String) =
        viewModelScope.async(Dispatchers.IO){
            firebaseAPI.setUserName(name).await()
    }

    suspend fun createUser(name : String, email: String, password: String) {
        val user = User(name, email)
        try {
            viewModelScope.async(Dispatchers.IO){
                val task = firebaseAPI.createUser(user, password).await()
                if (task.user != null) {
                    firebaseAPI.updateReferences()
                    firebaseAPI.addNewUserOnDatabase()
                    onUserCreated()
                    setUserName(user.name).await()
                } else {
                    val message = "Ocorreu um erro ao criar a conta. Tente novamente mais tarde."
                    onError(message)
                }
            }
        }catch (exception : Exception){
            val message = when (exception) {
                is FirebaseAuthInvalidCredentialsException -> "E-mail ou senha inválidos."
                is FirebaseAuthUserCollisionException -> "Este e-mail já está em uso."
                is FirebaseAuthWeakPasswordException -> "A senha deve ter pelo menos 6 caracteres."
                else -> "Ocorreu um erro ao criar a conta. Tente novamente mais tarde."
            }
            onError(message)
        }
    }

    suspend fun createUserNew(name : String, email: String, password: String) {
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

    suspend fun sendEmailVerificarion(){
        viewModelScope.async(Dispatchers.IO){
            firebaseAPI.sendEmailVerification()
                ?.addOnCompleteListener{
                    onSendEmailSuccess()
                }
                ?.addOnFailureListener{
                    onSendEmailFailure()
                }
        }
    }

    private fun getUserUID() : Deferred<String>{
        val uid = CompletableDeferred<String>()
        viewModelScope.async {
            val currentUser = firebaseAPI.currentUser()
            uid.complete(currentUser!!.uid)
        }
        return uid
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