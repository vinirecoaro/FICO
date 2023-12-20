package com.example.fico.ui

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fico.databinding.ActivityBudgetConfigurationListBinding
import com.example.fico.service.constants.AppConstants
import com.example.fico.ui.adapters.BudgetConfigurationListAdapter
import com.example.fico.ui.interfaces.OnListItemClick
import com.example.fico.ui.viewmodel.BudgetConfigurationListViewModel

class BudgetConfigurationListActivity : AppCompatActivity(),
    OnListItemClick {

    private val binding by lazy { ActivityBudgetConfigurationListBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<BudgetConfigurationListViewModel>()
    private lateinit var budgetConfiguratonListAdapter: BudgetConfigurationListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.rvBudgetConfigurationList.layoutManager = LinearLayoutManager(this)
        budgetConfiguratonListAdapter = BudgetConfigurationListAdapter(viewModel.budgetConfigurationList)
        budgetConfiguratonListAdapter.setOnItemClickListener(this)
        binding.rvBudgetConfigurationList.adapter = budgetConfiguratonListAdapter

        binding.budgetConfigurationListToolbar.setTitle("Orçamento")
        binding.budgetConfigurationListToolbar.setTitleTextColor(Color.WHITE)

        //Insert a back button on Navigation bar
        setSupportActionBar(binding.budgetConfigurationListToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setUpListeners()

    }

    override fun onListItemClick(position: Int) {
        val item = viewModel.budgetConfigurationList[position]
        if(item == AppConstants.CONFIGURATION_LIST.BUDGET_LIST.DEFAULT_BUDGET){
            startActivity(Intent(this, SetDefaultBudgetActivity::class.java))
        }else if(item == AppConstants.CONFIGURATION_LIST.BUDGET_LIST.BUDGET_PER_MONTH){
            startActivity(Intent(this,BudgetPerMonthActivity::class.java))
        }
    }

    private fun setUpListeners(){
        binding.budgetConfigurationListToolbar.setNavigationOnClickListener {
            finish()
        }
    }
}