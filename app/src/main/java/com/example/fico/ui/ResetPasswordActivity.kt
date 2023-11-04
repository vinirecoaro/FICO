package com.example.fico.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import com.example.fico.R
import com.example.fico.databinding.ActivityResetPasswordBinding
import com.example.fico.ui.viewmodel.ResetPasswordViewModel
import com.google.android.material.snackbar.Snackbar

class ResetPasswordActivity : AppCompatActivity() {

    private val binding by lazy {ActivityResetPasswordBinding.inflate(layoutInflater)}
    private val viewModel by viewModels<ResetPasswordViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle("Redefinir Senha")

        setUpListeners()

    }

    private fun setUpListeners(){
        binding.btSend.setOnClickListener {
            if(verifyFields(binding.etEmail)){
                viewModel.resetPassword(binding.etEmail.text.toString())
            }
        }

        viewModel.onResetPasswordSuccess = {
            Toast.makeText(this,"Email de redefinição de senha enviado com sucesso", Toast.LENGTH_LONG).show()
        }

        viewModel.onResetPasswordFail = {
            Toast.makeText(this,"Falha ao enviar email de redefinição de senha", Toast.LENGTH_LONG).show()
        }
    }

    private fun verifyFields(vararg text: EditText) : Boolean{
        for (i in text){
            if (i.text.toString() == "" || i == null){
                Snackbar.make(binding.btSend,"Preencher o campo ${i.hint}", Snackbar.LENGTH_LONG).show()
                return false
            }
        }
        return true
    }

}