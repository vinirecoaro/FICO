package com.example.fico.presentation.activities

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.example.fico.databinding.ActivityLogoBinding
import com.example.fico.presentation.viewmodel.LogoViewModel
import com.example.fico.utils.BiometricPromptManager
import com.example.fico.utils.BiometricPromptManager.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import com.bumptech.glide.Glide
import com.example.fico.R
import com.example.fico.presentation.viewmodel.shared.RemoteDatabaseViewModel
import com.example.fico.utils.UiFunctions
import com.example.fico.utils.constants.StringConstants
import com.example.fico.utils.internet.ConnectionFunctions

class LogoActivity : AppCompatActivity() {

    private val binding by lazy { ActivityLogoBinding.inflate(layoutInflater) }
    private val viewModel: LogoViewModel by inject()
    private val remoteDatabaseViewModel : RemoteDatabaseViewModel by inject()
    private val promptManager by lazy{ BiometricPromptManager(this) }
    private lateinit var enrollLauncher: ActivityResultLauncher<Intent>

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setUpListeners()

        lifecycleScope.launch(Dispatchers.Main) {
            if (viewModel.isLogged().await()) {
                viewModel.getBlockAppStateFromDataStore()
            }else{
                startActivity(Intent(this@LogoActivity, LoginActivity::class.java))
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun setUpListeners() {
        viewModel.getBlockAppState.observe(this){ result ->
            if(result){
                showBiometricPrompt()
            }else{
                lifecycleScope.launch(Dispatchers.Main) {
                    showLoadingLogo()
                    if(ConnectionFunctions.internetConnectionVerification(this@LogoActivity)){
                        remoteDatabaseViewModel.getDataFromDatabase()
                    }
                    startActivity(Intent(this@LogoActivity, MainTransactionActivity::class.java))
                    finish()
                }
            }
        }

        binding.btUseCellphonePassword.setOnClickListener{
            binding.btUseCellphonePassword.isEnabled = false
            binding.btUseCellphonePassword.visibility = View.GONE
            showBiometricPrompt()
            binding.btUseCellphonePassword.isEnabled = true
        }

        viewModel.onError = { message ->
            when(message) {
                StringConstants.MESSAGES.IS_LOGGED_ERROR -> {
                    Snackbar.make(binding.ivLogo, getString(R.string.is_logged_error_message), Snackbar.LENGTH_LONG).show()
                }
                StringConstants.MESSAGES.NO_INTERNET_CONNECTION -> {
                    Snackbar.make(binding.ivLogo, getString(R.string.no_internet_connection_error_message), Snackbar.LENGTH_LONG).show()
                }
            }
        }

        lifecycleScope.launch {
            promptManager.promptResults.collect{biometricResult ->

                when(biometricResult){
                    is BiometricResult.AuthenticationError -> {
                        //Change button visibility to show biometric prompt again
                        binding.btUseCellphonePassword.visibility = View.VISIBLE
                    }
                    BiometricResult.AuthenticationFailed -> {

                    }
                    BiometricResult.AuthenticationNotSet -> {
                        if(Build.VERSION.SDK_INT >= 30){
                            val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                                putExtra(
                                    Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                    BIOMETRIC_STRONG or DEVICE_CREDENTIAL
                                )
                            }
                            enrollLauncher.launch(enrollIntent)
                        }
                    }
                    BiometricResult.AuthenticationSuccess -> {
                        showLoadingLogo()
                        if(ConnectionFunctions.internetConnectionVerification(this@LogoActivity)){
                            remoteDatabaseViewModel.getDataFromDatabase()
                        }
                        startActivity(Intent(this@LogoActivity, MainTransactionActivity::class.java))
                        finish()

                    }
                    BiometricResult.FeatureUnavailable -> {

                    }
                    BiometricResult.HardwareUnavailable -> {
                    }
                }
            }
        }

        enrollLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                startActivity(Intent(this@LogoActivity, MainTransactionActivity::class.java))
                finish()
            }
        }
    }

    private fun showBiometricPrompt(){
        promptManager.showBiometricPrompt(
            title = getString(R.string.biometric_prompt_title),
            description = getString(R.string.biometric_prompt_description)
        )
    }

    private fun showLoadingLogo(){
        binding.cvLogo.visibility = View.GONE
        Glide.with(this@LogoActivity)
            .asGif()
            .load(
                UiFunctions.setImageBasedOnTheme(
                    this,
                    R.drawable.animated_logo_dark_mode,
                    R.drawable.animated_logo_light_mode
                )
            )
            .into(binding.ivLoadingLogo)
        binding.ivLoadingLogo.visibility = View.VISIBLE
    }

}