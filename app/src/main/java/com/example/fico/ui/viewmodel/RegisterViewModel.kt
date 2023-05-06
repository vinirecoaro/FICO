package com.example.fico.ui.viewmodel

import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.ViewModel
import com.google.android.material.snackbar.Snackbar

class RegisterViewModel : ViewModel() {
    fun handleRegister(name: String, email: String, password: String){

    }

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

}