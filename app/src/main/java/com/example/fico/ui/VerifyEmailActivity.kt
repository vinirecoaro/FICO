package com.example.fico.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.example.fico.R
import com.example.fico.databinding.ActivityVerifyEmailBinding
import com.example.fico.service.FirebaseAPI
import com.example.fico.ui.viewmodel.VerifyEmailViewModel
import com.google.android.material.snackbar.Snackbar

class VerifyEmailActivity : AppCompatActivity() {

    private val binding by lazy {ActivityVerifyEmailBinding.inflate(layoutInflater)}
    private val viewModel by viewModels<VerifyEmailViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_email)
        setUpListeners()

    }

    private fun setUpListeners(){
        binding.btResentEmail.setOnClickListener {
            viewModel.sendEmailVerificarion()
        }

        viewModel.isVerified.observe(this, Observer {isVerified ->
            if(isVerified){
                startActivity(Intent(this, MainActivity::class.java))
            }
        })

        viewModel.onSendEmailSuccess = {
            Snackbar.make(binding.btResentEmail, "Email de verificação enviado com sucesso.", Snackbar.LENGTH_LONG).show()
        }

        viewModel.onSendEmailFailure = {
            Snackbar.make(binding.btResentEmail, "Erro ao enviar email de verificação.", Snackbar.LENGTH_LONG).show()
        }

    }

}