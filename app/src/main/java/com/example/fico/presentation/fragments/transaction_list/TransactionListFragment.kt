package com.example.fico.presentation.fragments.transaction_list

import SwipeToDeleteCallback
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fico.R
import com.example.fico.api.FormatValuesFromDatabase
import com.example.fico.presentation.components.dialogs.Dialogs
import com.example.fico.presentation.components.PersonalizedSnackBars
import com.example.fico.databinding.FragmentTransactionListBinding
import com.example.fico.model.Earning
import com.example.fico.model.Expense
import com.example.fico.model.Transaction
import com.example.fico.utils.constants.StringConstants
import com.example.fico.presentation.activities.EditTransactionActivity
import com.example.fico.presentation.adapters.TransactionListAdapter
import com.example.fico.interfaces.OnListItemClick
import com.example.fico.interfaces.XLSInterface
import com.example.fico.presentation.compose.components.ComposeDialogs
import com.example.fico.presentation.viewmodel.TransactionListViewModel
import com.example.fico.utils.DateFunctions
import com.example.fico.utils.constants.CategoriesList
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.io.File
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class TransactionListFragment : Fragment(), XLSInterface {

    private var _binding: FragmentTransactionListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TransactionListViewModel by inject()
    private lateinit var transactionListAdapter : TransactionListAdapter
    private var expenseMonthsList = arrayOf<String>()
    private val permissionRequestCode = 123
    private val permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private companion object {
        private const val STORAGE_PERMISSION_CODE = 100
        const val TAG = "PERMISSION_TAG"
    }
    private val categoriesList : CategoriesList by inject()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionListBinding.inflate(inflater, container, false)
        val rootView = binding.root

        // Recycler View configuration
        binding.rvExpenseList.layoutManager = LinearLayoutManager(requireContext())
        transactionListAdapter = TransactionListAdapter(categoriesList.getExpenseCategoryListFull(), categoriesList.getEarningCategoryList())
        binding.rvExpenseList.adapter = transactionListAdapter

        // Item list swiping configuration
        val swipeToDeleteCallback =
            SwipeToDeleteCallback(viewModel, transactionListAdapter, requireActivity())
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvExpenseList)

        // Updating fields with current date
        binding.actvDate.setText(DateFunctions().getCurrentlyDateForFilter())
        viewModel.updateFilter(DateFunctions().getCurrentlyDateForFilter())

        setUpListeners()
        setColorBasedOnTheme()

        return rootView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()
        if(viewModel.returningFromEdit.value == null || viewModel.returningFromEdit.value == false){
            viewModel.getExpenseList(binding.actvDate.text.toString())
            // Initial selected button on toggle group
            binding.tbTransacList.check(binding.btAllTransacList.id)
            binding.btAllTransacList.isClickable = false
            viewModel.updateTransactionTypeFilter(StringConstants.DATABASE.TRANSACTION)
        }else{
            viewModel.updateShowFilteredList()
            viewModel.changeReturningFromEditState(false)
            viewModel.updateEditingTransaction(Transaction.empty())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.transaction_list_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    // check isFiltered state to show or not clear option
    override fun onPrepareOptionsMenu(menu: Menu){
        val clearFilterItem = menu.findItem(R.id.transaction_list_menu_clear_filter)
        clearFilterItem.isVisible = viewModel.isFiltered.value == true

        val filterItem = menu.findItem(R.id.transaction_list_menu_filter)
        val generateFileItem = menu.findItem(R.id.transaction_list_menu_generate_excel_file)
        val orderByValueItem = menu.findItem(R.id.transaction_list_menu_order_by_value)

        viewLifecycleOwner.lifecycleScope.launch{
            viewModel.uiState.collectLatest{state ->
                when(state){
                    TransactionFragmentState.Empty -> {
                        clearFilterItem.isVisible = false
                        filterItem.isVisible = false
                        generateFileItem.isVisible = false
                        orderByValueItem.isVisible = false
                    }
                    is TransactionFragmentState.Error -> {
                        clearFilterItem.isVisible = false
                        filterItem.isVisible = false
                        generateFileItem.isVisible = false
                    }
                    TransactionFragmentState.Loading -> {
                        clearFilterItem.isVisible = false
                        filterItem.isVisible = false
                        generateFileItem.isVisible = false
                    }
                    TransactionFragmentState.Success -> {
                        filterItem.isVisible = true
                        generateFileItem.isVisible = true
                        orderByValueItem.isVisible = true
                    }
                }
            }
        }

        return super.onPrepareOptionsMenu(menu)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.transaction_list_menu_generate_excel_file -> {
                if (checkPermission()){
                    generateFileAndShare()
                }else{
                    lifecycleScope.launch {
                        requestPermission()
                    }
                }
                return true
            }

            R.id.transaction_list_menu_filter -> {
                filterDialog()
                return true
            }

            R.id.transaction_list_menu_clear_filter -> {
                val allList = viewModel.transactionsListLiveData.value?.toList()
                if(!allList.isNullOrEmpty()){
                    when(viewModel.transactionTypeFilter.value){
                        StringConstants.DATABASE.TRANSACTION -> {
                            clearAllFilter()
                            updateTransactionTotalValue(allList)
                            transactionListAdapter.updateTransactions(allList)
                            viewModel.updateShowList(allList)
                        }
                        StringConstants.DATABASE.EXPENSE -> {
                            val expenseList = allList.filter { it.type == StringConstants.DATABASE.EXPENSE }
                            clearAllFilter()
                            updateTransactionTotalValue(expenseList)
                            transactionListAdapter.updateTransactions(expenseList)
                            viewModel.updateShowList(expenseList)
                        }
                        StringConstants.DATABASE.EARNING -> {
                            val earningList = allList.filter { it.type == StringConstants.DATABASE.EARNING }
                            clearAllFilter()
                            updateTransactionTotalValue(earningList)
                            transactionListAdapter.updateTransactions(earningList)
                            viewModel.updateShowList(earningList)
                        }
                    }
                }
                return true
            }

            R.id.transaction_list_menu_order_by_value -> {
                viewModel.orderShowListByValue()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getTransactionsListForFile(): Triple<MutableList<Expense>,MutableList<Earning>,MutableList<Expense>> {
        val expensesList: MutableList<Expense> = ArrayList()
        val earningsList: MutableList<Earning> = ArrayList()
        val installmentExpensesList: MutableList<Expense> = ArrayList()
        viewModel.showListLiveData.observe(viewLifecycleOwner, Observer { transactions ->
            for (transaction in transactions) {
                if(transaction.type == StringConstants.DATABASE.EXPENSE){
                    val expenseIdLength = transaction.id.length
                    //Verify if is a installment expense
                    if (expenseIdLength == 41) {
                        val commonId = FormatValuesFromDatabase().commonIdOnInstallmentExpense(transaction.id)
                        //Will add just one installment off expense
                        if(!installmentExpensesList.any{
                            val commonExpenseId = FormatValuesFromDatabase().commonIdOnInstallmentExpense(it.id)
                            val expenseFromListNOfInstallment = FormatValuesFromDatabase().installmentExpenseNofInstallment(it.id)
                            val analyzedExpenseNOfInstallment = FormatValuesFromDatabase().installmentExpenseNofInstallment(transaction.id)

                            //Conditions
                            commonExpenseId == commonId &&
                            expenseFromListNOfInstallment == analyzedExpenseNOfInstallment &&
                            it.price == transaction.price
                        }){
                            val modifiedExpense = Expense(
                                transaction.id,
                                transaction.price, // not format value to make multiply for number of installment in next step
                                transaction.description,
                                transaction.category,
                                transaction.paymentDate,
                                transaction.purchaseDate,
                                transaction.inputDateTime
                            )
                            installmentExpensesList.add(modifiedExpense)
                        }
                    }else{
                        val modifiedExpense = Expense(
                            transaction.id,
                            FormatValuesFromDatabase().priceToFile(transaction.price),
                            transaction.description,
                            transaction.category,
                            transaction.paymentDate,
                            transaction.purchaseDate,
                            transaction.inputDateTime
                        )
                        expensesList.add(modifiedExpense)
                    }
                }else if(transaction.type == StringConstants.DATABASE.EARNING){
                    val modifiedEarning = Earning(
                        transaction.id,
                        FormatValuesFromDatabase().priceToFile(transaction.price),
                        transaction.description,
                        transaction.category,
                        transaction.paymentDate,
                        transaction.inputDateTime
                    )
                    earningsList.add(modifiedEarning)
                }

            }
        })

        val formattedInstallmentExpenseList = mutableListOf<Expense>()

        installmentExpensesList.forEach { expense ->
            val nOfInstallments = FormatValuesFromDatabase().installmentExpenseNofInstallment(expense.id).toInt().toString()
            val fullPrice = FormatValuesFromDatabase().priceToFile(BigDecimal(expense.price).multiply(BigDecimal(nOfInstallments)).toString())
            val formattedDescription = FormatValuesFromDatabase().installmentExpenseDescription(expense.description)
            val initialPaymentDate = FormatValuesFromDatabase().installmentExpenseInitialDate(expense.id, expense.paymentDate)
            val formattedExpense = Expense(
                expense.id,
                fullPrice,
                formattedDescription,
                expense.category,
                initialPaymentDate,
                expense.purchaseDate,
                expense.inputDateTime,
                nOfInstallments
            )
            formattedInstallmentExpenseList.add(formattedExpense)
        }

        val listsTriple = Triple(expensesList, earningsList, formattedInstallmentExpenseList)

        return listsTriple
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setUpListeners() {
        binding.actvDate.setOnClickListener {
            binding.actvDate.showDropDown()
        }

        binding.actvDate.setOnItemClickListener { parent, view, position, id ->
            val selectedOption = parent.getItemAtPosition(position).toString()
            viewModel.getExpenseList(selectedOption)
            viewModel.getEarningList(selectedOption)
            if(viewModel.isFiltered.value == true){
                clearAllFilter()
            }
            //Verify which button is selected
            val selectedButtonId = binding.tbTransacList.checkedButtonId
            if(selectedButtonId != binding.btAllTransacList.id){
                binding.tbTransacList.check(binding.btAllTransacList.id)
                binding.btAllTransacList.isClickable = false
            }
        }

        binding.ivClearFilter.setOnClickListener {
            binding.actvDate.setText("")
            viewModel.updateOperation(StringConstants.OPERATIONS.CLEAR_MONTH_FILTER)
            viewModel.updateShowFilteredList()
            clearAllFilter()
        }

        viewModel.expensesLiveData.observe(viewLifecycleOwner, Observer { expenses ->
            viewModel.getExpenseMonths()
            viewModel.getEarningList(binding.actvDate.text.toString())
        })

        viewModel.earningsListLiveData.observe(viewLifecycleOwner){ earningList ->
            val expenseList = viewModel.expensesLiveData.value!!.toList()
            viewModel.updateTransactionsList(expenseList,earningList)
        }

        viewModel.expenseMonthsLiveData.observe(viewLifecycleOwner, Observer { expenseMonths ->
            expenseMonthsList = expenseMonths.toTypedArray()
            actvConfig()
        })

        binding.actvDate.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.updateFilter(binding.actvDate.text.toString())
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        viewModel.deleteExpenseResult.observe(viewLifecycleOwner){result ->
            if(result){
                //Show snackbar to undo the action
                PersonalizedSnackBars.successMessageWithAction(binding.root, getString(R.string.excluded_item), getString(R.string.undo), ::undoDeleteExpense).show()
            }else{
                //Show snackbar with failure message
                PersonalizedSnackBars.failureMessage(binding.root, getString(R.string.exclude_item_fail_message)).show()
            }
        }

        viewModel.deleteEarningResult.observe(viewLifecycleOwner){ result ->
            if(result){
                //Show snackbar to undo the action
                PersonalizedSnackBars.successMessageWithAction(binding.root, getString(R.string.excluded_item), getString(R.string.undo), ::undoDeleteEarning).show()
            }else{
                //Show snackbar with failure message
                PersonalizedSnackBars.failureMessage(binding.root, getString(R.string.exclude_item_fail_message)).show()
            }
        }

        viewModel.addExpenseResult.observe(viewLifecycleOwner){result ->
            if(result){
                Snackbar.make(binding.rvExpenseList, getString(R.string.undo_exclude_item_success_message), Snackbar.LENGTH_SHORT).show()
            }else{
                Snackbar.make(binding.rvExpenseList, getString(R.string.undo_exclude_item_fail_message), Snackbar.LENGTH_SHORT).show()
            }
        }

        viewModel.installmentExpenseSwiped.observe(viewLifecycleOwner){result ->
            viewModel.updateOperation(StringConstants.OPERATIONS.SWIPPED_INSTALLMENT_EXPENSE)
            if(result){
                val dialog = Dialogs.dialogModelOne(
                    requireActivity(),
                    requireContext(),
                    getString(R.string.delete_expense),
                    getString(R.string.exclude_installment_expense_message_instruction_message),
                    getString(R.string.ok)
                ){viewModel.updateShowFilteredList()}
                dialog.show()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch{
            viewModel.uiState.collectLatest{state ->
                when(state){
                    TransactionFragmentState.Empty -> {
                        binding.ivNoInfoAvailable.visibility = View.VISIBLE
                        binding.tvNoInfoAvailable.visibility = View.VISIBLE
                        binding.pbTransactionList.visibility = View.GONE
                        binding.actvDate.visibility = View.GONE
                        binding.ivClearFilter.visibility = View.GONE
                        binding.rvExpenseList.visibility = View.GONE
                        binding.tilTotalPrice.visibility = View.GONE
                        binding.tbTransacList.visibility = View.GONE
                    }
                    is TransactionFragmentState.Error -> {
                        binding.ivNoInfoAvailable.visibility = View.VISIBLE
                        binding.tvNoInfoAvailable.visibility = View.VISIBLE
                        binding.pbTransactionList.visibility = View.GONE
                        binding.actvDate.visibility = View.GONE
                        binding.ivClearFilter.visibility = View.GONE
                        binding.rvExpenseList.visibility = View.GONE
                        binding.tilTotalPrice.visibility = View.GONE
                        binding.tbTransacList.visibility = View.GONE
                    }
                    TransactionFragmentState.Loading -> {
                        binding.ivNoInfoAvailable.visibility = View.GONE
                        binding.tvNoInfoAvailable.visibility = View.GONE
                        binding.pbTransactionList.visibility = View.VISIBLE
                        binding.actvDate.visibility = View.GONE
                        binding.ivClearFilter.visibility = View.GONE
                        binding.rvExpenseList.visibility = View.GONE
                        binding.tilTotalPrice.visibility = View.GONE
                        binding.tbTransacList.visibility = View.GONE
                    }
                    TransactionFragmentState.Success -> {
                        binding.ivNoInfoAvailable.visibility = View.GONE
                        binding.tvNoInfoAvailable.visibility = View.GONE
                        binding.pbTransactionList.visibility = View.GONE
                        binding.actvDate.visibility = View.VISIBLE
                        binding.ivClearFilter.visibility = View.VISIBLE
                        binding.rvExpenseList.visibility = View.VISIBLE
                        binding.tilTotalPrice.visibility = View.VISIBLE
                        binding.tbTransacList.visibility = View.VISIBLE
                    }
                }
            }
        }

        viewModel.isFiltered.observe(viewLifecycleOwner){ state ->
            requireActivity().invalidateOptionsMenu()
        }

        binding.tbTransacList.addOnButtonCheckedListener{ group, checkedId, isChecked ->
            if(isChecked){
                when(checkedId){
                    binding.btAllTransacList.id -> {
                        binding.btAllTransacList.isClickable = false
                        binding.btExpensesTransacList.isClickable = true
                        binding.btEarningsTransacList.isClickable = true
                        viewModel.updateTransactionTypeFilter(StringConstants.DATABASE.TRANSACTION)
                        viewModel.showTransactionsBasedOnType(StringConstants.DATABASE.TRANSACTION)
                    }
                    binding.btExpensesTransacList.id -> {
                        binding.btAllTransacList.isClickable = true
                        binding.btExpensesTransacList.isClickable = false
                        binding.btEarningsTransacList.isClickable = true
                        viewModel.updateTransactionTypeFilter(StringConstants.DATABASE.EXPENSE)
                        viewModel.showTransactionsBasedOnType(StringConstants.DATABASE.EXPENSE)
                    }
                    binding.btEarningsTransacList.id -> {
                        binding.btAllTransacList.isClickable = true
                        binding.btExpensesTransacList.isClickable = true
                        binding.btEarningsTransacList.isClickable = false
                        viewModel.updateTransactionTypeFilter(StringConstants.DATABASE.EARNING)
                        viewModel.showTransactionsBasedOnType(StringConstants.DATABASE.EARNING)
                    }
                }
            }
        }

        viewModel.showListLiveData.observe(viewLifecycleOwner){ transacList ->
            transactionListAdapter.updateTransactions(transacList)
            updateTransactionTotalValue(transacList)
            transactionListAdapter.setOnItemClickListener(object : OnListItemClick {
                override fun onListItemClick(position: Int) {
                     val selectItem = transacList[position]
                    editTransaction(selectItem)
                }
            })
        }

        viewModel.addEarningResult.observe(viewLifecycleOwner){ result ->
            if (result) {
                PersonalizedSnackBars.successMessage(binding.root, getString(R.string.add_earning_success_message)).show()

            } else {
                PersonalizedSnackBars.failureMessage(binding.root, getString(R.string.add_earning_failure_message)).show()
            }
        }

        viewModel.internetConnection.observe(viewLifecycleOwner){state ->
            if(!state){
                PersonalizedSnackBars.noInternetConnection(binding.rvExpenseList, requireActivity()).show()
            }
        }

    }

    private fun actvConfig() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            expenseMonthsList
        )
        binding.actvDate.setAdapter(adapter)
    }

    fun editTransaction(transaction: Transaction) {
        viewModel.updateEditingTransaction(transaction)
        val intent = Intent(requireContext(), EditTransactionActivity::class.java)
        intent.putExtra(StringConstants.TRANSACTION_LIST.TRANSACTION, transaction)
        startActivityForResult(intent, StringConstants.REQUEST_CODES.TRANSACTION_LIST_TO_EDIT_TRANSACTION)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Check if edit expense was successfully completed
        if(requestCode == StringConstants.REQUEST_CODES.TRANSACTION_LIST_TO_EDIT_TRANSACTION){
            viewModel.changeReturningFromEditState(true)
            when (resultCode) {

                Activity.RESULT_OK -> {
                    viewModel.updateOperation(StringConstants.OPERATIONS.UPDATE)
                    PersonalizedSnackBars.successMessage(binding.root, getString(R.string.update_expense_success_message)).show()
                }

                Activity.RESULT_CANCELED -> {
                    PersonalizedSnackBars.failureMessage(binding.root, getString(R.string.update_expense_fail_message)).show()
                }

                StringConstants.RESULT_CODES.DELETE_INSTALLMENT_EXPENSE_RESULT_OK -> {
                    viewModel.updateOperation(StringConstants.OPERATIONS.DELETE)
                    val deletedItem = viewModel.getEditingTransaction()
                    viewModel.updateDeletedItem(deletedItem)
                    PersonalizedSnackBars.successMessage(binding.root, getString(R.string.delete_expense_success_message)).show()
                }

                StringConstants.RESULT_CODES.DELETE_INSTALLMENT_EXPENSE_RESULT_FAILURE -> {
                    PersonalizedSnackBars.failureMessage(binding.root, getString(R.string.delete_expense_fail_message)).show()
                }

                StringConstants.RESULT_CODES.EDIT_EARNING_EXPENSE_RESULT_OK -> {
                    viewModel.updateOperation(StringConstants.OPERATIONS.UPDATE)
                    PersonalizedSnackBars.successMessage(binding.root, getString(R.string.edit_earning_success_message)).show()
                }

                StringConstants.RESULT_CODES.EDIT_EARNING_EXPENSE_RESULT_FAILURE -> {
                    PersonalizedSnackBars.failureMessage(binding.root, getString(R.string.edit_earning_failure_message)).show()
                }

                StringConstants.RESULT_CODES.DELETE_EXPENSE_RESULT_OK -> {
                    viewModel.updateOperation(StringConstants.OPERATIONS.DELETE)
                    val deletedItem = viewModel.getEditingTransaction()
                    viewModel.updateDeletedItem(deletedItem)
                    PersonalizedSnackBars.successMessage(binding.root, getString(R.string.delete_expense_success_message)).show()
                }

                StringConstants.RESULT_CODES.DELETE_EXPENSE_RESULT_FAILURE -> {
                    PersonalizedSnackBars.failureMessage(binding.root, getString(R.string.delete_expense_fail_message)).show()
                }

                StringConstants.RESULT_CODES.DELETE_EARNING_RESULT_OK -> {
                    viewModel.updateOperation(StringConstants.OPERATIONS.DELETE)
                    val deletedItem = viewModel.getEditingTransaction()
                    viewModel.updateDeletedItem(deletedItem)
                    PersonalizedSnackBars.successMessage(binding.root, getString(R.string.delete_earning_success_message)).show()
                }

                StringConstants.RESULT_CODES.DELETE_EARNING_RESULT_FAILURE -> {
                    PersonalizedSnackBars.failureMessage(binding.root, getString(R.string.delete_earning_fail_message)).show()
                }
            }
        }
    }

    private fun setColorBasedOnTheme() {
        when (context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.ivClearFilter.setImageResource(R.drawable.baseline_cancel_light)
            }

            Configuration.UI_MODE_NIGHT_NO -> {
                binding.ivClearFilter.setImageResource(R.drawable.baseline_cancel_dark)
            }

            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }
    }

    private fun shareFile(filePath: File) {
        val fileUri = FileProvider.getUriForFile(
            requireContext(),
            StringConstants.FILE_PROVIDER.AUTHORITY, // authorities deve corresponder ao valor definido no AndroidManifest.xml
            filePath
        )
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "application/vnd.ms-excel"
        intent.putExtra(Intent.EXTRA_STREAM, fileUri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val chooser = Intent.createChooser(intent, "Share File")

        val resInfoList: List<ResolveInfo> =
            requireActivity().packageManager.queryIntentActivities(
                chooser,
                PackageManager.MATCH_DEFAULT_ONLY
            )

        for (resolveInfo in resInfoList) {
            val packageName: String = resolveInfo.activityInfo.packageName
            requireActivity().grantUriPermission(
                packageName,
                fileUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        startActivity(chooser)
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Log.d(TAG, "requestPermission: try")
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                val uri = Uri.fromParts("package", requireActivity().packageName, null)
                intent.data = uri
                storageActivityResultLauncher.launch(intent)
            } catch (e: java.lang.Exception) {
                Log.d(TAG, "RequestPermission: ", e)
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                storageActivityResultLauncher.launch(intent)
            }
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), permissions,
                STORAGE_PERMISSION_CODE
            )
        }
    }

    private val storageActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            Log.d(TAG, "storageActivityResultLauncher: ")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    Log.d(TAG, "storageActivityResultLauncher: ")
                    lifecycleScope.launch {
                        delay(500)
                    }
                } else {
                    Log.d(TAG, "storageActivityResultLauncher: ")
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.external_storage_permission_is_denied),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {

            }
        }

    private fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            val write = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            val read = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == permissionRequestCode) {
            if (grantResults.isNotEmpty()) {
                val write = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val read = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (write && read) {
                    Log.d(TAG, "onRequestPermissionResult: External Storage Permission granted")
                } else {
                    Log.d(TAG, "onRequestPermissionResult: External Storage Permission denied ...")
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.external_storage_permission_is_denied),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun generateFileAndShare() {
        val transactionsLists = getTransactionsListForFile()
        val gson = Gson()
        var expensesJsonArray = gson.toJsonTree(transactionsLists.first).asJsonArray
        var earningsJsonArray = gson.toJsonTree(transactionsLists.second).asJsonArray
        var installmentExpensesJsonArray = gson.toJsonTree(transactionsLists.third).asJsonArray

        var file = generateXlsFile(
            requireActivity(), StringConstants.XLS.EXPENSE_TITLES, StringConstants.XLS.EARNINGS_TITLES,
            StringConstants.XLS.INSTALLMENT_EXPENSES_TITLES, StringConstants.XLS.EXPENSE_INDEX_NAME,
            StringConstants.XLS.EARNINGS_INDEX_NAME, StringConstants.XLS.INSTALLMENT_EXPENSES_INDEX_NAME,
            expensesJsonArray, earningsJsonArray, installmentExpensesJsonArray, HashMap(),
            StringConstants.XLS.SHEET_NAME_EXPENSES, StringConstants.XLS.SHEET_NAME_EARNINGS,
            StringConstants.XLS.SHEET_NAME_INSTALLMENT_EXPENSES, StringConstants.XLS.FILE_NAME, 0
        )

        if (file != null) {
            shareFile(file)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun filterDialog(){
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle(getString(R.string.select_filter))

        val inflater = LayoutInflater.from(requireContext())
        val dialogView = inflater.inflate(R.layout.dialog_transaction_fragment_filter, null)

        val tvTextFilter = dialogView.findViewById<TextView>(R.id.tv_description_filter)
        val tvTextFilterValues = dialogView.findViewById<TextView>(R.id.tv_description_filter_values)
        val vSeparatorTextFilterValues = dialogView.findViewById<View>(R.id.v_dialog_transaction_fragment_separator_line_description)
        val rdTextFilter = dialogView.findViewById<RadioButton>(R.id.rb_description_filter)
        val tvDateFilter = dialogView.findViewById<TextView>(R.id.tv_date_filter)
        val tvDateFilterValues = dialogView.findViewById<TextView>(R.id.tv_date_filter_values)
        val vSeparatorDateFilterValues = dialogView.findViewById<View>(R.id.v_dialog_transaction_fragment_separator_line_date)
        val rdDateFilter = dialogView.findViewById<RadioButton>(R.id.rb_date_filter)
        val tvCategoryFilter = dialogView.findViewById<TextView>(R.id.tv_category_filter)
        val tvCategoryFilterValues = dialogView.findViewById<TextView>(R.id.tv_category_filter_values)
        val vSeparatorCategoryFilterValues = dialogView.findViewById<View>(R.id.v_dialog_transaction_fragment_separator_line_category)
        val rdCategoryFilter = dialogView.findViewById<RadioButton>(R.id.rb_category_filter)

        //verify radio state and set value

        //Description Filter
        val textFilterState = viewModel.descriptionFilterState.value
        if(textFilterState != null && textFilterState != false){
            rdTextFilter.isChecked = textFilterState
            var filterTextValuesString = ""
            viewModel.descriptionFilterValues.value!!.forEachIndexed { index, filter ->
                if (index == 0){
                    filterTextValuesString = getString(R.string.filterValues) + " $filter"
                }else{
                    filterTextValuesString += " - $filter"
                }
            }
            tvTextFilterValues.text = filterTextValuesString
            tvTextFilterValues.visibility = View.VISIBLE
            vSeparatorTextFilterValues.visibility = View.VISIBLE
        }else{
            tvTextFilterValues.visibility = View.GONE
            vSeparatorTextFilterValues.visibility = View.GONE
        }

        //Date Filter
        val dateFilterState = viewModel.dateFilterState.value
        if(dateFilterState != null && dateFilterState != false){
            rdDateFilter.isChecked = dateFilterState
            val dateFilterValues = viewModel.dateFilterValue.value!!
            val dateFilterValuesString = "${dateFilterValues.first}  -  ${dateFilterValues.second}"
            tvDateFilterValues.text = dateFilterValuesString
            tvDateFilterValues.visibility = View.VISIBLE
            vSeparatorDateFilterValues.visibility = View.VISIBLE
        }else{
            tvDateFilterValues.visibility = View.GONE
            vSeparatorDateFilterValues.visibility = View.GONE
        }

        //Category Filter
        val categoryFilterState = viewModel.categoryFilterState.value
        if(categoryFilterState != null && categoryFilterState != false){
            rdCategoryFilter.isChecked = categoryFilterState
            var categoryFilterValueString = ""
            viewModel.categoryFilterValue.value!!.forEachIndexed { index, filter ->
                if (index == 0){
                    categoryFilterValueString = getString(R.string.filterValues) + " $filter"
                }else{
                    categoryFilterValueString += " - $filter"
                }
            }
            tvCategoryFilterValues.text = categoryFilterValueString
            tvCategoryFilterValues.visibility = View.VISIBLE
            vSeparatorCategoryFilterValues.visibility = View.VISIBLE
        }else{
            tvCategoryFilterValues.visibility = View.GONE
            vSeparatorCategoryFilterValues.visibility = View.GONE
        }

        builder.setView(dialogView)

        val dialog = builder.create()

        dialog.show()

        tvTextFilter.setOnClickListener {
            textFilterDialog()
            dialog.cancel()
        }

        tvDateFilter.setOnClickListener {
            dateRangePicker()
            dialog.cancel()
        }

        tvCategoryFilter.setOnClickListener {
            categoryFilterDialog()
            dialog.cancel()
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun dateRangePicker(){
        val dateRangePickerBuilder = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText(getString(R.string.select_dates))

        if(viewModel.monthFilterLiveData.value != null && viewModel.monthFilterLiveData.value != ""){

            val infoPair = DateFunctions().formatMonthValueFromFilterTransactionListToMonthYear(viewModel.monthFilterLiveData.value!!)
            val month = infoPair.first-1
            val year = infoPair.second

            val calendarStart = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, 1)
            }
            val calendarEnd = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            }
            val constraints = CalendarConstraints.Builder()
                .setStart(calendarStart.timeInMillis) // Início do mês
                .setEnd(calendarEnd.timeInMillis) // Fim do mês
                .build()

            dateRangePickerBuilder.setCalendarConstraints(constraints)
        }

        val dateRangePicker = dateRangePickerBuilder.build()

        dateRangePicker.show(requireActivity().supportFragmentManager, "dataRangePicker")

        dateRangePicker.addOnPositiveButtonClickListener {

            val adjustedStartDate = formatDate(it.first)
            val adjustedEndDate = formatDate(it.second)

            val dates = Pair(adjustedStartDate,adjustedEndDate)

            viewModel.applyDateFilter(dates)
        }
    }

    private fun textFilterDialog(){
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle(getString(R.string.text_filter))

        val inflater = LayoutInflater.from(requireContext())
        val dialogView = inflater.inflate(R.layout.dialog_transaction_fragment_text_filter, null)

        val etTextFilter = dialogView.findViewById<TextInputEditText>(R.id.et_text_filter)

        builder.setView(dialogView)

        builder.setPositiveButton(getString(R.string.to_filter)){dialog, which ->
            if(!etTextFilter.text.isNullOrEmpty()){
                val filter = etTextFilter.text.toString()
                viewModel.applyDescriptionFilter(filter)
            }
        }

        val dialog = builder.create()

        dialog.show()
    }

    private fun categoryFilterDialog(){
        ComposeDialogs.showCategoryListDialog(
            composeView = binding.cmpViewTransactionList,
            items = viewModel.getShowListCategories(requireContext()),
            title = getString(R.string.categories),
        ) { categoriesListRes ->
            val categoryNames = categoriesListRes.map { getString(it) }
            viewModel.applyCategoryFilter(categoryNames)
        }
    }

    private fun clearAllFilter(){
        viewModel.setIsFilteredState(false)
        clearTextFilter()
        clearDateFilter()
        clearCategoryFilter()
    }

    private fun clearTextFilter(){
        viewModel.setDescriptionFilterState(false)
        viewModel.clearDescriptionFilterValues()
    }

    private fun clearDateFilter(){
        viewModel.setDateFilterState(false)
        viewModel.clearDateFilterValues()
    }

    private fun clearCategoryFilter(){
        viewModel.setCategoryFilterState(false)
        viewModel.clearCategoryFilterValues()
    }

    private fun updateTransactionTotalValue(transactionList : List<Transaction>){
        val total = viewModel.calculateFilteredTotalValue(transactionList)
        val totalAbsolute = total.abs()
        if(total < BigDecimal(0)){
            binding.etTotalPrice.setTextColor(Color.RED)
        }else{
            binding.etTotalPrice.setTextColor(Color.GREEN)
        }
        binding.etTotalPrice.setText(FormatValuesFromDatabase().price(totalAbsolute.toString()))
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("GMT")
        val date = Date(timestamp)
        return sdf.format(date)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun undoDeleteExpense(){
        lifecycleScope.launch {
            val deletedExpense = viewModel.getDeletedItem().toExpense()
            viewModel.undoDeleteExpense(deletedExpense, false, 1)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun undoDeleteEarning(){
        viewModel.undoDeleteEarning(viewModel.getDeletedItem().toEarning())
    }

}