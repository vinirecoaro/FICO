package com.example.fico.presentation.fragments

import android.app.Dialog
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fico.R
import com.example.fico.components.Dialogs
import com.example.fico.databinding.FragmentConfigurationBinding
import com.example.fico.model.Transaction
import com.example.fico.presentation.activities.BudgetConfigurationListActivity
import com.example.fico.presentation.activities.DefaultPaymentDateConfigurationActivity
import com.example.fico.presentation.activities.EditTransactionActivity
import com.example.fico.presentation.adapters.ExpenseConfigurationListAdapter
import com.example.fico.presentation.adapters.TransactionListAdapter
import com.example.fico.interfaces.OnListItemClick
import com.example.fico.presentation.viewmodel.TransactionConfigurationViewModel
import com.example.fico.utils.constants.CategoriesList
import com.example.fico.utils.constants.StringConstants
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject

class TransactionConfigurationFragment : Fragment(), OnListItemClick {

    private var _binding : FragmentConfigurationBinding? = null
    private val binding get() = _binding!!
    private val viewModel : TransactionConfigurationViewModel by inject()
    private lateinit var configuratonListAdapter: ExpenseConfigurationListAdapter
    private lateinit var recurringTransactionListAdapter : TransactionListAdapter
    private val categoriesList : CategoriesList by inject()
    private val startEditTransactionActivityForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){result ->
        when(result.resultCode){
            StringConstants.RESULT_CODES.EDIT_RECURRING_TRANSACTION_OK -> {
                Snackbar.make(
                    binding.rvConfigurationList,
                    getString(R.string.edit_recurring_transaction_success_message),
                    Snackbar.LENGTH_SHORT
                ).show()
            }

            StringConstants.RESULT_CODES.EDIT_RECURRING_TRANSACTION_FAILURE -> {
                Snackbar.make(
                    binding.rvConfigurationList,
                    getString(R.string.edit_recurring_transaction_failure_message),
                    Snackbar.LENGTH_SHORT
                ).show()
            }

            StringConstants.RESULT_CODES.DELETE_RECURRING_TRANSACTION_RESULT_OK -> {
                Snackbar.make(
                    binding.rvConfigurationList,
                    getString(R.string.delete_recurring_transaction_success_message),
                    Snackbar.LENGTH_SHORT
                ).show()
            }

            StringConstants.RESULT_CODES.DELETE_RECURRING_TRANSACTION_RESULT_FAILURE -> {
                Snackbar.make(
                    binding.rvConfigurationList,
                    getString(R.string.delete_recurring_transaction_failure_message),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
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

    @RequiresApi(Build.VERSION_CODES.O)
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

        viewModel.recurringTransactionsList.observe(viewLifecycleOwner){ recurringTransactionList ->
            val transactionList = mutableListOf<Transaction>()
            val list =  recurringTransactionList.first
            val type =  recurringTransactionList.second
            list.forEach { recurringExpense -> transactionList.add(recurringExpense.toTransaction()) }
            recurringTransactionListDialog(transactionList, type)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onListItemClick(position: Int) {
        val item = viewModel.configurationList[position]
        when (item) {
            getString(R.string.budget_configuration_list) -> {
                startActivity(Intent(requireContext(), BudgetConfigurationListActivity::class.java))
            }
            getString(R.string.default_payment_date) -> {
                startActivity(Intent(requireContext(), DefaultPaymentDateConfigurationActivity::class.java))
            }
            getString(R.string.update_database_info_per_month_and_total_expense) -> {
                viewModel.updateInfoPerMonthAndTotalExpense()
            }
            getString(R.string.recurring_transactions_configuration_list) -> {
                selectTransactionTypeDialog()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun selectTransactionTypeDialog(){
        val dialog = Dialogs.dialogModelThree(
            activity = requireActivity(),
            context = requireContext(),
            title = getString(R.string.edit_recurring_transaction),
            subtitle = getString(R.string.edit_recurring_transaction_message),
            rightButtonText =  getString(R.string.expenses_2),
            rightButtonFunction = ::recurringExpenseDialog,
            leftButtonText = getString(R.string.earnings_2),
            leftButtonFunction = ::recurringEarningDialog
        )

        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun recurringExpenseDialog(){

        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle(getString(R.string.expenses_2))
        builder.setMessage(getString(R.string.edit_recurring_transaction_message_step_2))

        builder.setPositiveButton(getString(R.string.list)){dialog, which ->
            viewModel.getRecurringTransactionList(StringConstants.DATABASE.RECURRING_EXPENSE)
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun recurringTransactionListDialog(recurringTransactionList : List<Transaction>, transactionType : String){
        val builder = MaterialAlertDialogBuilder(requireContext())

        if(transactionType ==  StringConstants.DATABASE.RECURRING_EXPENSE){
            builder.setTitle(getString(R.string.dialog_recurring_expense_list_title))
        } else if(transactionType ==  StringConstants.DATABASE.RECURRING_EARNING){
            builder.setTitle(getString(R.string.dialog_recurring_earning_list_title))
        }

        val inflater = LayoutInflater.from(requireContext())
        val dialogView = inflater.inflate(R.layout.dialog_recurring_expenses, null)

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.rv_recurring_expenses_list)

        // Recycler View configuration
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recurringTransactionListAdapter = TransactionListAdapter(categoriesList.getExpenseCategoryListFull(), categoriesList.getEarningCategoryList())
        recurringTransactionListAdapter.updateTransactions(recurringTransactionList)
        recyclerView.adapter = recurringTransactionListAdapter

        builder.setView(dialogView)

        val dialog = builder.create()
        dialog.show()

        // Listeners
        recurringTransactionListAdapter.setOnItemClickListener { position ->
            val selectItem = recurringTransactionList[position]
            val intent = Intent(requireContext(), EditTransactionActivity::class.java)
            intent.putExtra(StringConstants.TRANSACTION_LIST.TRANSACTION, selectItem)
            startEditTransactionActivityForResult.launch(intent)
            dialog.cancel()
        }
    }

    private fun recurringEarningDialog(){
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle(getString(R.string.earnings_2))
        builder.setMessage(getString(R.string.edit_recurring_transaction_message_step_2))

        builder.setPositiveButton(getString(R.string.list)){dialog, which ->
            viewModel.getRecurringTransactionList(StringConstants.DATABASE.RECURRING_EARNING)
        }

        builder.setNegativeButton(getString(R.string.add)){dialog, which ->
            val navController = requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main)
                ?.findNavController()
            val bundle = Bundle().apply {
                putBoolean(StringConstants.ADD_TRANSACTION.ADD_RECURRING_EARNING, true)
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

    private fun getAlertDialogTextButtonColor() : Int{
        val typedValue = TypedValue()
        val theme: Resources.Theme = requireActivity().theme
        theme.resolveAttribute(R.attr.alertDialogTextButtonColor, typedValue, true)
        val colorOnSurfaceVariant = ContextCompat.getColor(requireContext(), typedValue.resourceId)
        return colorOnSurfaceVariant
    }

}