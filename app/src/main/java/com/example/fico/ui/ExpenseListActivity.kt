package com.example.fico.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fico.R
import com.example.fico.databinding.ActivityExpenseListBinding
import com.example.fico.ui.viewmodel.ExpenseListViewModel

class ExpenseListActivity : AppCompatActivity() {

    private val binding by lazy { ActivityExpenseListBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<ExpenseListViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_list)

        binding.rvExpenseList.layoutManager = LinearLayoutManager(this)
    }

    override fun onResume() {
        super.onResume()
        viewModel.getExpenseList(binding.rvExpenseList)
    }

}