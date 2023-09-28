package com.example.fico.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.example.fico.R
import com.example.fico.databinding.ActivityEditExpenseBinding
import com.example.fico.ui.viewmodel.EditExpenseViewModel

class EditExpenseActivity : AppCompatActivity() {

    val binding by lazy { ActivityEditExpenseBinding.inflate(layoutInflater) }
    val viewModel by viewModels<EditExpenseViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}