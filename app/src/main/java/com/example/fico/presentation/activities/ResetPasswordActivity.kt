package com.example.fico.presentation.activities

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import androidx.activity.viewModels
import com.example.fico.databinding.ActivityResetPasswordBinding
import com.example.fico.presentation.viewmodel.ResetPasswordViewModel
import com.google.android.material.snackbar.Snackbar

class ResetPasswordActivity : AppCompatActivity() {

    private val binding by lazy {ActivityResetPasswordBinding.inflate(layoutInflater)}
    private val viewModel by viewModels<ResetPasswordViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.resetPasswordToolbar.setTitle("Redefinir Senha")
        binding.resetPasswordToolbar.setTitleTextColor(Color.WHITE)

        //Insert a back button on Navigation bar
        setSupportActionBar(binding.resetPasswordToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setUpListeners()

    }

    private fun setUpListeners(){
        binding.btSend.setOnClickListener {
            if(verifyFields(binding.etEmail)){
                viewModel.resetPassword(binding.etEmail.text.toString())
            }
        }

        viewModel.onResetPasswordSuccess = {
            val intent = Intent(this, LoginActivity::class.java)
            intent.putExtra("Email Enviado", true)
            startActivity(intent)
        }

        viewModel.onResetPasswordFail = {
            Snackbar.make(binding.btSend,"Falha ao enviar email de redefinição de senha", Snackbar.LENGTH_LONG).show()
        }

        binding.resetPasswordToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun verifyFields(vararg text: EditText) : Boolean{
        for (i in text){
            if (i.text.toString() == "" || i == null){
                Snackbar.make(binding.btSend,"Preencher o campo ${i.hint}", Snackbar.LENGTH_LONG).show()
                return false
            }
        }
        return true
    }

}