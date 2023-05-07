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
            // Inicia a nova activity em caso de sucesso
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        viewModel.onError = { message ->
            // Exibe a mensagem de erro em caso de falha
            Snackbar.make(binding.btRegister, message, Snackbar.LENGTH_LONG).show()
        }
    }

}