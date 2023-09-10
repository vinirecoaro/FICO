package com.example.fico.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import com.example.fico.R
import com.example.fico.databinding.ActivityBudgetConfigurationListBinding
import com.example.fico.databinding.FragmentConfigurationBinding
import com.example.fico.ui.adapters.BudgetConfigurationListAdapter
import com.example.fico.ui.adapters.ConfigurationListAdapter
import com.example.fico.ui.viewmodel.BudgetConfigurationListViewModel
import com.example.fico.ui.viewmodel.ConfigurationViewModel

class BudgetConfigurationListActivity : AppCompatActivity() {

    private var _binding : ActivityBudgetConfigurationListBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<BudgetConfigurationListViewModel>()
    private lateinit var budgetConfiguratonListAdapter: BudgetConfigurationListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_budget_configuration_list)

    }
}