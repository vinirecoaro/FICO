package com.example.fico.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.example.fico.databinding.ActivityMainBinding
import com.example.fico.ui.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setUpListeners()

    }

    private fun setUpListeners(){
        binding.btAddExpenses.setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }
        binding.tvTotalExpensesValue.setOnClickListener {
            viewModel.ShowHideValue(binding.tvTotalExpensesValue)
        }
        binding.btConfig.setOnClickListener {
            startActivity(Intent(this, ConfigurationActivity::class.java))
        }
    }

}