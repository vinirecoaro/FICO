package com.example.fico.presentation.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.fico.R
import com.example.fico.databinding.ActivityVerifyEmailBinding
import com.example.fico.presentation.viewmodel.VerifyEmailViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject


class VerifyEmailActivity : AppCompatActivity() {

    private val binding by lazy {ActivityVerifyEmailBinding.inflate(layoutInflater)}
    private val viewModel : VerifyEmailViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setUpListeners()

    }

    private fun setUpListeners(){
        binding.btResentEmail.setOnClickListener {
            binding.btResentEmail.isEnabled = false
            lifecycleScope.launch {
                viewModel.sendVerificationEmail()
                binding.btResentEmail.isEnabled = true
            }
        }

        binding.btLogin.setOnClickListener {
            binding.btLogin.isEnabled = false
            lifecycleScope.launch {
                viewModel.logoff()
            }
            startActivity(Intent(this, LoginActivity::class.java))
            binding.btLogin.isEnabled = true
        }

        viewModel.isVerified.observe(this, Observer {isVerified ->
            if(isVerified){
                startActivity(Intent(this, MainTransactionActivity::class.java))
            }
        })

        viewModel.onSendEmailSuccess = {
            Snackbar.make(
                binding.btResentEmail,
                getString(R.string.verification_email_success_message),
                Snackbar.LENGTH_LONG
            ).show()
        }

        viewModel.onSendEmailFailure = {
            Snackbar.make(
                binding.btResentEmail,
                getString(R.string.verification_email_fail_message),
                Snackbar.LENGTH_LONG
            ).show()
        }

    }

}