package com.example.fico.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.fico.R
import com.example.fico.databinding.ActivityResetPasswordBinding

class ResetPasswordActivity : AppCompatActivity() {

    private val binding by lazy {ActivityResetPasswordBinding.inflate(layoutInflater)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle("Redefinir Senha")
    }
}