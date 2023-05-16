package com.example.fico.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.example.fico.R
import com.example.fico.databinding.ActivityRegisterBinding
import com.example.fico.ui.viewmodel.RegisterViewModel
import com.google.android.material.snackbar.Snackbar

class RegisterActivity : AppCompatActivity() {

    private val binding by lazy {ActivityRegisterBinding.inflate(layoutInflater)}
    private val viewModel by viewModels<RegisterViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setUpListeners()
    }

    private fun setUpListeners(){
        binding.btRegister.setOnClickListener{
            if(viewModel.checkFields(binding.btRegister, binding.etEmail, binding.etPassword)){
                viewModel.createUser(
                    binding.etEmail.text.toString(),
                    binding.etPassword.text.toString())
            }
        }

        viewModel.onUserCreated = {
            viewModel.sendEmailVerificarion()
            startActivity(Intent(this, VerifyEmailActivity::class.java))
            finish()
        }

        viewModel.onError = { message ->
            Snackbar.make(binding.btRegister, message, Snackbar.LENGTH_LONG).show()
        }

        viewModel.onSendEmailSuccess = {
            Snackbar.make(binding.btRegister, "Email de verificação enviado com sucesso.", Snackbar.LENGTH_LONG).show()
        }

        viewModel.onSendEmailFailure = {
            Snackbar.make(binding.btRegister, "Erro ao enviar email de verificação.", Snackbar.LENGTH_LONG).show()
        }
    }

}