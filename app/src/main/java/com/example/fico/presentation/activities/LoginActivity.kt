package com.example.fico.presentation.activities

import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.fico.R
import com.example.fico.databinding.ActivityLoginBinding
import com.example.fico.presentation.activities.expense.MainActivity
import com.example.fico.presentation.viewmodel.LoginViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class LoginActivity : AppCompatActivity() {

    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    private val viewModel : LoginViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        resetPasswordSucess()
        setColorBasedOnTheme()
        setUpListeners()

    }

    private fun setUpListeners(){
        binding.btLogin.setOnClickListener {
            binding.btLogin.isEnabled = false
            lifecycleScope.launch (Dispatchers.Main){
                viewModel.login(
                    binding.etEmail.text.toString(),
                    binding.etPassword.text.toString())
            }
            binding.btLogin.isEnabled = true
        }
        binding.tvRedifinePassword.setOnClickListener {
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }
        binding.tvRegister.setOnClickListener{
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        viewModel.onUserLogged = {
            startActivity(Intent(this, MainActivity::class.java))
        }
        viewModel.onUserNotVerified = {
            startActivity(Intent(this, VerifyEmailActivity::class.java))
        }
        viewModel.onError = { message ->
            Snackbar.make(binding.btLogin, message, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun resetPasswordSucess(){
        val result = intent.getBooleanExtra("Email Enviado",false)
        if(result){
            Toast.makeText(this,"Email de redefinição de senha enviado com sucesso", Toast.LENGTH_LONG).show()
        }
    }

    private fun setColorBasedOnTheme(){
        when (this.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.ivLogo.setImageResource(R.drawable.login_logo_light)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.ivLogo.setImageResource(R.drawable.login_logo_black)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }
    }


}