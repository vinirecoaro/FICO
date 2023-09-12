package com.example.fico.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fico.R
import com.example.fico.databinding.ActivityBudgetConfigurationListBinding
import com.example.fico.databinding.ActivityMainBinding
import com.example.fico.databinding.FragmentConfigurationBinding
import com.example.fico.ui.adapters.BudgetConfigurationListAdapter
import com.example.fico.ui.adapters.ConfigurationListAdapter
import com.example.fico.ui.viewmodel.BudgetConfigurationListViewModel
import com.example.fico.ui.viewmodel.ConfigurationViewModel

class BudgetConfigurationListActivity : AppCompatActivity() {

    private val binding by lazy { ActivityBudgetConfigurationListBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<BudgetConfigurationListViewModel>()
    private lateinit var budgetConfiguratonListAdapter: BudgetConfigurationListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.rvBudgetConfigurationList.layoutManager = LinearLayoutManager(this)
        budgetConfiguratonListAdapter = BudgetConfigurationListAdapter(viewModel.budgetConfigurationList)
        binding.rvBudgetConfigurationList.adapter = budgetConfiguratonListAdapter

    }
}