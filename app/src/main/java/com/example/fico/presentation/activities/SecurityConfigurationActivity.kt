package com.example.fico.presentation.activities

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.lifecycle.lifecycleScope
import com.example.fico.R
import com.example.fico.databinding.ActivitySecurityConfigurationBinding
import com.example.fico.presentation.viewmodel.SecurityConfigurationViewModel
import com.example.fico.utils.BiometricPromptManager
import com.example.fico.utils.BiometricPromptManager.BiometricResult
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class SecurityConfigurationActivity : AppCompatActivity() {

    private val binding by lazy{ ActivitySecurityConfigurationBinding.inflate(layoutInflater)}
    private val viewModel : SecurityConfigurationViewModel by inject()
    private val promptManager by lazy{ BiometricPromptManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.securityConfigurationToolbar.setTitle(getString(R.string.security))
        binding.securityConfigurationToolbar.setTitleTextColor(Color.WHITE)

        //Insert a back button on Navigation bar
        setSupportActionBar(binding.securityConfigurationToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setUpListeners()

    }

    private fun setUpListeners(){
        binding.securityConfigurationToolbar.setNavigationOnClickListener {
            finish()
        }

        binding.swtBlockApp.setOnCheckedChangeListener{ _ , state ->
            viewModel.setBlockAppState(state)
        }

        viewModel.changeBlockAppState.observe(this){ state ->
            if(state){
                viewModel.setBlockAppStateOnDataStore(state)
            }else{
                showBiometricPrompt()
            }
        }

        lifecycleScope.launch {
            promptManager.promptResults.collect{biometricResult ->

                when(biometricResult){
                    is BiometricResult.AuthenticationError -> {
                        binding.swtBlockApp.isChecked = true
                    }
                    BiometricResult.AuthenticationFailed -> {

                    }
                    BiometricResult.AuthenticationNotSet -> {

                    }
                    BiometricResult.AuthenticationSuccess -> {
                        viewModel.setBlockAppStateOnDataStore(false)
                    }
                    BiometricResult.FeatureUnavailable -> {

                    }
                    BiometricResult.HardwareUnavailable -> {
                    }
                }
            }
        }

        viewModel.getBlockAppState.observe(this){ state ->
            binding.swtBlockApp.isChecked = state
        }

    }

    private fun showBiometricPrompt(){
        promptManager.showBiometricPrompt(
            title = getString(R.string.biometric_prompt_title),
            description = getString(R.string.authenticate)
        )
    }
}