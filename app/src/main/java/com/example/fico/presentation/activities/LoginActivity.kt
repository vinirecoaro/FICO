package com.example.fico.presentation.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.example.fico.R
import com.example.fico.databinding.ActivityLoginBinding
import com.example.fico.presentation.viewmodel.LoginViewModel
import com.example.fico.presentation.viewmodel.shared.RemoteDatabaseViewModel
import com.example.fico.utils.UiFunctions
import com.example.fico.utils.constants.StringConstants
import com.example.fico.presentation.components.inputs.InputFieldFunctions
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class LoginActivity : AppCompatActivity() {

    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    private val viewModel : LoginViewModel by inject()
    private var networkConnectionSnackBar: Snackbar? = null
    private val remoteDatabaseViewModel : RemoteDatabaseViewModel by inject()


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
            UiFunctions.hideKeyboard(this, binding.btLogin)
            binding.btLogin.isEnabled = false
            binding.btLogin.text = StringConstants.MESSAGES.EMPTY_STRING
            binding.pbLogin.visibility = View.VISIBLE
            if(InputFieldFunctions.isFilled(
                    this,
                    binding.btLogin,
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
            lifecycleScope.launch (Dispatchers.Main){
                remoteDatabaseViewModel.getDataFromDatabase()
                startActivity(Intent(this@LoginActivity, MainTransactionActivity::class.java))
            }
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

        //Personalized box Stroke and Hint color
        InputFieldFunctions.defineStrokeColorOnFocused(
            this,
            this,
            Pair(binding.tlEmail, binding.etEmail),
            Pair(binding.tlPassword, binding.etPassword)
        )
    }

    private fun resetPasswordSucess(){
        val result = intent.getBooleanExtra(StringConstants.RESET_PASSWORD.EMAIL_SENT,false)
        if(result){
            Toast.makeText(this,getString(R.string.redefine_password_email_sent_success_message), Toast.LENGTH_LONG).show()
        }
    }
}