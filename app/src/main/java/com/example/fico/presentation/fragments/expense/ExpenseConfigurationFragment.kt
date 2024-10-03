package com.example.fico.presentation.fragments.expense

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fico.R
import com.example.fico.databinding.FragmentConfigurationBinding
import com.example.fico.shared.constants.StringConstants
import com.example.fico.presentation.activities.expense.BudgetConfigurationListActivity
import com.example.fico.presentation.activities.expense.DefaultPaymentDateConfigurationActivity
import com.example.fico.presentation.adapters.ExpenseConfigurationListAdapter
import com.example.fico.presentation.interfaces.OnListItemClick
import com.example.fico.presentation.viewmodel.ExpenseConfigurationViewModel
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject

class ExpenseConfigurationFragment : Fragment(),
    OnListItemClick {

    private var _binding : FragmentConfigurationBinding? = null
    private val binding get() = _binding!!
    private val viewModel : ExpenseConfigurationViewModel by inject()
    private lateinit var configuratonListAdapter: ExpenseConfigurationListAdapter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentConfigurationBinding.inflate(inflater, container, false)
        val rootView = binding.root

        binding.rvConfigurationList.layoutManager = LinearLayoutManager(requireContext())
        configuratonListAdapter = ExpenseConfigurationListAdapter(viewModel.configurationList)
        configuratonListAdapter.setOnItemClickListener(this)
        binding.rvConfigurationList.adapter = configuratonListAdapter

        setUpListeners()

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpListeners(){
        viewModel.updateDatabaseResult.observe(viewLifecycleOwner){ result ->
            if(result){
                Snackbar.make(
                    binding.rvConfigurationList,
                    getString(R.string.update_info_per_month_and_total_expense_success_message),
                    Snackbar.LENGTH_LONG
                ).show()
            }else{
                Snackbar.make(
                    binding.rvConfigurationList,
                    getString(R.string.update_info_per_month_and_total_expense_failure_message),
                    Snackbar.LENGTH_LONG
                ).show()
            }

        }
    }

    override fun onListItemClick(position: Int) {
        val item = viewModel.configurationList[position]
        if(item == getString(R.string.budget_configuration_list)){
            startActivity(Intent(requireContext(), BudgetConfigurationListActivity::class.java))
        }else if(item == getString(R.string.default_payment_date)){
            startActivity(Intent(requireContext(), DefaultPaymentDateConfigurationActivity::class.java))
        }else if (item == getString(R.string.update_database_info_per_month_and_total_expense)){
            viewModel.updateInfoPerMonthAndTotalExpense()
        }
    }

}