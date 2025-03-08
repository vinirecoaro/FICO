package com.example.fico.presentation.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.example.fico.R
import com.example.fico.databinding.ActivityLoginBinding
import com.example.fico.presentation.viewmodel.LoginViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class LoginActivity : AppCompatActivity() {

    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    private val viewModel : LoginViewModel by inject()
    private var networkConnectionSnackBar: Snackbar? = null

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        resetPasswordSucess()
        setUpListeners()

    }

    @RequiresApi(Build.VERSION_CODES.N)
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
            startActivity(Intent(this@LoginActivity, MainTransactionActivity::class.java))
        }
        viewModel.onUserNotVerified = {
            startActivity(Intent(this, VerifyEmailActivity::class.java))
        }
        viewModel.onError = { message ->
            Snackbar.make(binding.btLogin, message, Snackbar.LENGTH_LONG).show()
        }
        lifecycleScope.launch(Dispatchers.Main){
            viewModel.internetConnection.isConnected.collectLatest{ isConnected ->
                if (!isConnected) {
                    if (networkConnectionSnackBar == null) { //create just if it not exists
                        networkConnectionSnackBar = Snackbar.make(
                            binding.btLogin,
                            getString(R.string.without_network_connection),
                            Snackbar.LENGTH_INDEFINITE
                        )
                            .setBackgroundTint(resources.getColor(android.R.color.holo_red_dark, theme))
                            .setActionTextColor(resources.getColor(android.R.color.white, theme))
                        networkConnectionSnackBar?.show()
                    }
                } else {
                    networkConnectionSnackBar?.dismiss()
                    networkConnectionSnackBar = null // Clear the instance to allow recreate after
                }
            }
        }
    }

    private fun resetPasswordSucess(){
        val result = intent.getBooleanExtra("Email Enviado",false)
        if(result){
            Toast.makeText(this,"Email de redefinição de senha enviado com sucesso", Toast.LENGTH_LONG).show()
        }
    }

}