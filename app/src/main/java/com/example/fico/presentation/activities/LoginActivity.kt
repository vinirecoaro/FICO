package com.example.fico.presentation.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.example.fico.R
import com.example.fico.databinding.ActivityLoginBinding
import com.example.fico.presentation.viewmodel.LoginViewModel
import com.example.fico.utils.constants.StringConstants
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
            binding.btLogin.text = StringConstants.MESSAGES.EMPTY_STRING
            binding.pbLogin.visibility = View.VISIBLE
            if(verifyFields(
                binding.etEmail,
                binding.etPassword
            )){
                lifecycleScope.launch (Dispatchers.Main){
                    viewModel.login(
                        binding.etEmail.text.toString(),
                        binding.etPassword.text.toString())
                }
            }else{
                binding.btLogin.isEnabled = true
                binding.btLogin.text = getString(R.string.login)
                binding.pbLogin.visibility = View.GONE
            }
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
            when (message) {
                StringConstants.MESSAGES.INVALID_CREDENTIALS -> {
                    Snackbar.make(binding.btLogin, getString(R.string.invalid_credentials), Snackbar.LENGTH_LONG).show()
                }
                StringConstants.MESSAGES.USER_NOT_FOUND -> {
                    Snackbar.make(binding.btLogin, getString(R.string.user_not_found), Snackbar.LENGTH_LONG).show()
                }
                StringConstants.MESSAGES.LOGIN_ERROR -> {
                    Snackbar.make(binding.btLogin, getString(R.string.login_error), Snackbar.LENGTH_LONG).show()
                }
            }
        }

        viewModel.enabledLoginButton.observe(this){ enabled ->
            if(enabled){
                binding.btLogin.isEnabled = true
                binding.btLogin.text = getString(R.string.login)
                binding.pbLogin.visibility = View.GONE
            }
        }

        lifecycleScope.launch(Dispatchers.Main){
            viewModel.internetConnection.isConnected.collectLatest{ isConnected ->
                if (!isConnected) {
                    if (networkConnectionSnackBar == null) { //create just if it not exists

                        binding.btLogin.isEnabled = false

                        networkConnectionSnackBar = Snackbar.make(
                            binding.btLogin,
                            getString(R.string.without_network_connection),
                            Snackbar.LENGTH_INDEFINITE
                        )
                            .setBackgroundTint(resources.getColor(android.R.color.holo_red_dark, theme))
                            .setTextColor(resources.getColor(android.R.color.white, theme))
                        networkConnectionSnackBar?.show()
                    }
                } else {

                    binding.btLogin.isEnabled = true

                    networkConnectionSnackBar?.dismiss()
                    networkConnectionSnackBar = null // Clear the instance to allow recreate after
                }
            }
        }
    }

    private fun resetPasswordSucess(){
        val result = intent.getBooleanExtra(StringConstants.RESET_PASSWORD.EMAIL_SENT,false)
        if(result){
            Toast.makeText(this,getString(R.string.redefine_password_email_sent_success_message), Toast.LENGTH_LONG).show()
        }
    }

    private fun verifyFields(vararg text: EditText): Boolean {
        for (i in text) {
            if (i.text.toString() == "" || i == null) {
                Snackbar.make(
                    binding.btLogin, "${getString(R.string.fill_field)} ${i.hint}", Snackbar.LENGTH_LONG
                ).show()
                return false
            }
        }
        return true
    }

}