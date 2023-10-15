package com.example.fico.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fico.R
import com.example.fico.databinding.ActivityBudgetPerMonthBinding
import com.example.fico.ui.adapters.BudgetPerMonthAdapter
import com.example.fico.ui.adapters.ExpenseListAdapter
import com.example.fico.ui.viewmodel.BudgetPerMonthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class BudgetPerMonthActivity : AppCompatActivity() {

    private val bindind by lazy { ActivityBudgetPerMonthBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<BudgetPerMonthViewModel>()
    private val budgetPerMonthListAdapter = BudgetPerMonthAdapter(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(bindind.root)
        setTitle("Budget por mês")

        bindind.rvBudgetPerMonth.layoutManager = LinearLayoutManager(this)
        bindind.rvBudgetPerMonth.adapter = budgetPerMonthListAdapter

        setUpListeners()
    }

    private fun setUpListeners(){
        lifecycleScope.async(Dispatchers.Main){

            viewModel.budgetPerMonthList.observe(this@BudgetPerMonthActivity, Observer {budgetList ->
                budgetPerMonthListAdapter.updateList(budgetList)
            })

            viewModel.getBudgetPerMonth()
        }
    }
}