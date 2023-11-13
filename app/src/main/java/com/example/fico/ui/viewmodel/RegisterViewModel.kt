package com.example.fico.ui.viewmodel

import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.model.User
import com.example.fico.service.FirebaseAPI
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RegisterViewModel : ViewModel() {

    private val firebaseAPI = FirebaseAPI.instance

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
        if (counter == nFileds){
            return true
        }
        return false
    }

    fun emptyField(btn: Button ,text: EditText){
        val snackbar = Snackbar.make(btn, "O campo ${text.hint} está vazio, preencha-o", Snackbar.LENGTH_LONG)
        snackbar.show()
    }


    private fun setUserName(name : String) = viewModelScope.async{
        firebaseAPI.setUserName(name).await()
    }

    suspend fun createUser(name : String, email: String, password: String) {
        val user = User(name, email, password)
        try {
            viewModelScope.async(Dispatchers.IO){
                val task = firebaseAPI.createUser(user).await()
                if (task.user != null) {
                    firebaseAPI.addNewUserOnDatabase()
                    onUserCreated()
                    setUserName(user.name)
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

    var onUserCreated: () -> Unit = {}
    var onError: (String) -> Unit = {}

    var onSendEmailSuccess: () -> Unit = {}
    var onSendEmailFailure: () -> Unit = {}

}