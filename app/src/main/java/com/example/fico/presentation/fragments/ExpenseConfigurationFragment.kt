package com.example.fico.presentation.fragments

import android.app.Dialog
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fico.R
import com.example.fico.databinding.FragmentConfigurationBinding
import com.example.fico.presentation.activities.BudgetConfigurationListActivity
import com.example.fico.presentation.activities.DefaultPaymentDateConfigurationActivity
import com.example.fico.presentation.adapters.ExpenseConfigurationListAdapter
import com.example.fico.presentation.interfaces.OnListItemClick
import com.example.fico.presentation.viewmodel.ExpenseConfigurationViewModel
import com.example.fico.utils.constants.StringConstants
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
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
        var item = viewModel.configurationList[position]
        if(item == getString(R.string.budget_configuration_list)){
            startActivity(Intent(requireContext(), BudgetConfigurationListActivity::class.java))
        }else if(item == getString(R.string.default_payment_date)){
            startActivity(Intent(requireContext(), DefaultPaymentDateConfigurationActivity::class.java))
        }else if (item == getString(R.string.update_database_info_per_month_and_total_expense)){
            viewModel.updateInfoPerMonthAndTotalExpense()
        }else if(item == getString(R.string.recurring_transactions_configuration_list)){
            selectTransactionTypeDialog()
        }
    }

    private fun selectTransactionTypeDialog(){
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle(getString(R.string.edit_recurring_transaction))
        builder.setMessage(getString(R.string.edit_recurring_transaction_message))

        builder.setPositiveButton(getString(R.string.expenses_2)){dialog, which ->
            recurringExpenseDialog()
        }

        builder.setNegativeButton(getString(R.string.earnings_2)){dialog, which ->
            recurringEarningDialog()
        }

        val dialog = builder.create()

        dialog.setOnShowListener {
            dialog.getButton(Dialog.BUTTON_POSITIVE).setTextColor(getAlertDialogTextButtonColor())
            dialog.getButton(Dialog.BUTTON_NEGATIVE).setTextColor(getAlertDialogTextButtonColor())
        }

        dialog.show()
    }

    private fun recurringExpenseDialog(){
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle(getString(R.string.expenses_2))
        builder.setMessage(getString(R.string.edit_recurring_transaction_message_step_2))

        builder.setPositiveButton(getString(R.string.list)){dialog, which ->

        }

        builder.setNegativeButton(getString(R.string.add)){dialog, which ->
            val navController = requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main)
                ?.findNavController()
            val bundle = Bundle().apply {
                putBoolean(StringConstants.ADD_TRANSACTION.ADD_RECURRING_EXPENSE, true)
            }

            // Clear the back stack to the current fragment
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.navigation_config, true)
                .build()

            navController!!.navigate(R.id.navigation_add_expense, bundle, navOptions)
        }

        val dialog = builder.create()

        dialog.setOnShowListener {
            dialog.getButton(Dialog.BUTTON_POSITIVE).setTextColor(getAlertDialogTextButtonColor())
            dialog.getButton(Dialog.BUTTON_NEGATIVE).setTextColor(getAlertDialogTextButtonColor())
        }

        dialog.show()
    }

    private fun recurringEarningDialog(){
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle(getString(R.string.earnings_2))
        builder.setMessage(getString(R.string.edit_recurring_transaction_message_step_2))

        builder.setPositiveButton(getString(R.string.list)){dialog, which ->

        }

        builder.setNegativeButton(getString(R.string.add)){dialog, which ->

        }

        val dialog = builder.create()

        dialog.setOnShowListener {
            dialog.getButton(Dialog.BUTTON_POSITIVE).setTextColor(getAlertDialogTextButtonColor())
            dialog.getButton(Dialog.BUTTON_NEGATIVE).setTextColor(getAlertDialogTextButtonColor())
        }

        dialog.show()
    }

    private fun getAlertDialogTextButtonColor() : Int{
        val typedValue = TypedValue()
        val theme: Resources.Theme = requireActivity().theme
        theme.resolveAttribute(R.attr.alertDialogTextButtonColor, typedValue, true)
        val colorOnSurfaceVariant = ContextCompat.getColor(requireContext(), typedValue.resourceId)
        return colorOnSurfaceVariant
    }

}