package com.example.fico.presentation.activities

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fico.R
import com.example.fico.databinding.ActivityBudgetConfigurationListBinding
import com.example.fico.presentation.adapters.BudgetConfigurationListAdapter
import com.example.fico.interfaces.OnListItemClick
import com.example.fico.presentation.viewmodel.BudgetConfigurationListViewModel
import org.koin.android.ext.android.inject

class BudgetConfigurationListActivity : AppCompatActivity(),
    OnListItemClick {

    private val binding by lazy { ActivityBudgetConfigurationListBinding.inflate(layoutInflater) }
    private val viewModel : BudgetConfigurationListViewModel by inject()
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
        if(item == getString(R.string.default_budget_activity_title)){
            startActivity(Intent(this, SetDefaultBudgetActivity::class.java))
        }else if(item == getString(R.string.budget_per_month_item_list)){
            startActivity(Intent(this, BudgetPerMonthActivity::class.java))
        }
    }

    private fun setUpListeners(){
        binding.budgetConfigurationListToolbar.setNavigationOnClickListener {
            finish()
        }
    }
}