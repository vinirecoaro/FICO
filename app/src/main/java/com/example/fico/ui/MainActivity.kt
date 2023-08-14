package com.example.fico.ui

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import com.example.fico.R
import com.example.fico.databinding.ActivityMainBinding
import com.example.fico.service.FirebaseAPI
import com.example.fico.ui.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<MainViewModel>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setUpListeners()
        viewModel.returnTotalExpense(binding.tvTotalExpensesValue)
        viewModel.returnAvailableNow(binding.tvAvailableThisMonthValue, viewModel.getCurrentYearMonth().toString())
        viewModel.returnMonthExpense(binding.tvTotalExpensesThisMonthValue, viewModel.getCurrentYearMonth().toString())

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
        binding.btList.setOnClickListener {
            startActivity(Intent(this, ExpenseListActivity::class.java))
        }
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId){
                R.id.navigation_home -> {
                    startActivity(Intent(this,MainActivity::class.java))
                    true
                }
                R.id.navigation_add_expense -> {
                    binding.bottomNavigation.selectedItemId = R.id.navigation_home
                    startActivity(Intent(this,AddExpenseActivity::class.java))
                    true
                }
                R.id.navigation_expense_list -> {
                    binding.bottomNavigation.selectedItemId = R.id.navigation_home
                    startActivity(Intent(this,ExpenseListActivity::class.java))
                    true
                }
                R.id.navigation_config -> {
                    binding.bottomNavigation.selectedItemId = R.id.navigation_home
                    startActivity(Intent(this,ConfigurationActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

}