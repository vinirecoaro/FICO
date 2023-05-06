package com.example.fico.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.example.fico.R
import com.example.fico.databinding.ActivityRegisterBinding
import com.example.fico.ui.viewmodel.RegisterViewModel

class RegisterActivity : AppCompatActivity() {

    private val binding by lazy {ActivityRegisterBinding.inflate(layoutInflater)}
    private val viewModel by viewModels<RegisterViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setUpListeners()
    }

    private fun setUpListeners(){
        binding.btRegister.setOnClickListener{
            if(viewModel.checkFields(binding.btRegister, binding.etName, binding.etEmail, binding.etPassword)){
                viewModel.handleRegister(
                    binding.btRegister.text.toString(),
                    binding.etName.text.toString(),
                    binding.etEmail.text.toString())
            }
        }
    }

}