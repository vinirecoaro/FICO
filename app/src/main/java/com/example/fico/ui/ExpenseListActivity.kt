package com.example.fico.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fico.R
import com.example.fico.databinding.ActivityExpenseListBinding
import com.example.fico.ui.adapters.ExpenseListAdapter
import com.example.fico.ui.viewmodel.ExpenseListViewModel

class ExpenseListActivity : AppCompatActivity() {

    private val binding by lazy { ActivityExpenseListBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<ExpenseListViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.rvExpenseList.layoutManager = LinearLayoutManager(this)
        val adapter = ExpenseListAdapter(emptyList())
        binding.rvExpenseList.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        viewModel.getExpenseList(binding.rvExpenseList)
    }

}