package com.example.fico.presentation.fragments.expense

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fico.R
import com.example.fico.databinding.FragmentConfigurationBinding
import com.example.fico.util.constants.AppConstants
import com.example.fico.presentation.activities.expense.BudgetConfigurationListActivity
import com.example.fico.presentation.adapters.ExpenseConfigurationListAdapter
import com.example.fico.presentation.interfaces.OnListItemClick
import com.example.fico.presentation.viewmodel.ExpenseConfigurationViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.koin.android.ext.android.inject

class ConfigurationFragment : Fragment(),
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

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onListItemClick(position: Int) {
        val item = viewModel.configurationList[position]
        if(item == AppConstants.EXPENSE_CONFIGURATION_LIST.BUDGET){
            startActivity(Intent(requireContext(), BudgetConfigurationListActivity::class.java))
        }else if(item == AppConstants.EXPENSE_CONFIGURATION_LIST.DEFAULT_PAYMENT_DATE){
            setDefaultPaymentDateAlertDialog()
        }
    }

    private fun setDefaultPaymentDateAlertDialog(){
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle("Data de Pagamento Padrão")

        val inflater = LayoutInflater.from(requireContext())
        val dialogView = inflater.inflate(R.layout.select_date_for_alert_dialog, null)

        builder.setView(dialogView)

        builder.setPositiveButton("Salvar"){dialog, which ->
            //viewModel.setDefaultPaymentDate()
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }

}