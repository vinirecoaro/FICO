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
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Data de Pagamento Padrão")

        val textInputLayout = TextInputLayout(requireContext()).apply {
            id = View.generateViewId()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE)
            hint = getString(R.string.payment_date)
        }

// Criar o TextInputEditText
        val textInputEditText = TextInputEditText(requireContext()).apply {
            id = View.generateViewId()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setHint(getString(R.string.payment_date))
            inputType = InputType.TYPE_CLASS_DATETIME
            isFocusable = false
        }

// Adicionar o TextInputEditText ao TextInputLayout
        textInputLayout.addView(textInputEditText)

// Criar o ImageView
        val imageView = ImageView(requireContext()).apply {
            id = View.generateViewId()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setImageResource(R.drawable.baseline_calendar_month_light)
            isClickable = true
            isFocusable = true
        }

// Criar um LinearLayout para conter o TextInputLayout e o ImageView
        val linearLayout = LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            addView(textInputLayout)
            addView(imageView)
        }

        builder.setView(linearLayout)

        builder.setPositiveButton("Salvar"){dialog, which ->
            //viewModel.setDefaultPaymentDate()
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }

}