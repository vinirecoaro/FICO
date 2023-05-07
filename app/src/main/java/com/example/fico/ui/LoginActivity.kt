package com.example.fico.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.example.fico.R
import com.example.fico.databinding.ActivityLoginBinding
import com.example.fico.ui.viewmodel.LoginViewModel
import com.google.android.material.snackbar.Snackbar

class LoginActivity : AppCompatActivity() {

    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<LoginViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        setUpListeners()
        viewModel.isLogged()
    }

    private fun setUpListeners(){
        binding.btLogin.setOnClickListener {
            viewModel.login(
                binding.etEmail.text.toString(),
                binding.etPassword.text.toString())
        }
        binding.tvRegister.setOnClickListener{
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        viewModel.onUserLogged = {
            startActivity(Intent(this, MainActivity::class.java))
        }
        viewModel.onError = { message ->
            // Exibe a mensagem de erro em caso de falha
            Snackbar.make(binding.btLogin, message, Snackbar.LENGTH_LONG).show()
        }
    }

}