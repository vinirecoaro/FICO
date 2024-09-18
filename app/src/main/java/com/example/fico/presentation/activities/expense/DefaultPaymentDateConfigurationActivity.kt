package com.example.fico.presentation.activities.expense

import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fico.R
import com.example.fico.databinding.ActivityDefaultPaymentDateConfigurationBinding
import com.example.fico.presentation.adapters.BudgetConfigurationListAdapter
import com.example.fico.presentation.adapters.DefaultPaymentDateConfigurationListAdapter
import com.example.fico.presentation.viewmodel.BudgetConfigurationListViewModel
import com.example.fico.presentation.viewmodel.DefaultPaymentDateConfigurationViewModel

class DefaultPaymentDateConfigurationActivity : AppCompatActivity() {

    private val binding by lazy{ActivityDefaultPaymentDateConfigurationBinding.inflate(layoutInflater)}
    private val viewModel by viewModels<DefaultPaymentDateConfigurationViewModel>()
    private lateinit var defaultPaymentDateConfigurationListAdapter: DefaultPaymentDateConfigurationListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        /*binding.rvDefaultPaymentDateConfiguration.layoutManager = LinearLayoutManager(this)
        defaultPaymentDateConfigurationListAdapter = DefaultPaymentDateConfigurationListAdapter(viewModel.budgetConfigurationList)
        defaultPaymentDateConfigurationListAdapter.setOnItemClickListener(this)
        binding.rvDefaultPaymentDateConfiguration.adapter = defaultPaymentDateConfigurationListAdapter*/

        binding.defaultPaymentDateConfigurationToolbar.setTitle(getString(R.string.default_payment_day))
        //binding.defaultPaymentDateConfigurationToolbar.setTitleTextColor(Color.WHITE)

        //Insert a back button on Navigation bar
        setSupportActionBar(binding.defaultPaymentDateConfigurationToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
}