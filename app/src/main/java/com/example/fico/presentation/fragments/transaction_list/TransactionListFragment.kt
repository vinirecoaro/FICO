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
import com.example.fico.databinding.FragmentTransactionListBinding
import com.example.fico.model.Earning
import com.example.fico.model.Expense
import com.example.fico.model.Transaction
import com.example.fico.utils.constants.StringConstants
import com.example.fico.presentation.activities.EditTransactionActivity
import com.example.fico.presentation.adapters.TransactionListAdapter
import com.example.fico.presentation.interfaces.OnListItemClick
import com.example.fico.presentation.interfaces.XLSInterface
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
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE
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
            SwipeToDeleteCallback(binding.rvExpenseList, viewModel, transactionListAdapter, viewLifecycleOwner)
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

        viewLifecycleOwner.lifecycleScope.launch{
            viewModel.uiState.collectLatest{state ->
                when(state){
                    TransactionFragmentState.Empty -> {
                        clearFilterItem.isVisible = false
                        filterItem.isVisible = false
                        generateFileItem.isVisible = false
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
                        }
                        StringConstants.DATABASE.EXPENSE -> {
                            val expenseList = allList.filter { it.type == StringConstants.DATABASE.EXPENSE }
                            clearAllFilter()
                            updateTransactionTotalValue(expenseList)
                            transactionListAdapter.updateTransactions(expenseList)
                        }
                        StringConstants.DATABASE.EARNING -> {
                            val earningList = allList.filter { it.type == StringConstants.DATABASE.EARNING }
                            clearAllFilter()
                            updateTransactionTotalValue(earningList)
                            transactionListAdapter.updateTransactions(earningList)
                        }
                    }
                }
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun getTransactionsListForFile(): Pair<MutableList<Expense>,MutableList<Earning>> {
        val expensesList: MutableList<Expense> = ArrayList()
        val earningsList: MutableList<Earning> = ArrayList()
        viewModel.showListLiveData.observe(viewLifecycleOwner, Observer { transactions ->
            for (transation in transactions) {
                if(transation.type == StringConstants.DATABASE.EXPENSE){
                    val modifiedExpense = Expense(
                        transation.id,
                        FormatValuesFromDatabase().priceToFile(transation.price),
                        transation.description,
                        transation.category,
                        transation.paymentDate,
                        transation.purchaseDate,
                        transation.inputDateTime
                    )
                    expensesList.add(modifiedExpense)
                }else if(transation.type == StringConstants.DATABASE.EARNING){
                    val modifiedEarning = Earning(
                        transation.id,
                        FormatValuesFromDatabase().priceToFile(transation.price),
                        transation.description,
                        transation.category,
                        transation.paymentDate,
                        transation.inputDateTime
                    )
                    earningsList.add(modifiedEarning)
                }

            }
        })

        val listsPair = Pair(expensesList, earningsList)

        return listsPair
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
                val snackbar = Snackbar.make(binding.rvExpenseList, getString(R.string.excluded_item), Snackbar.LENGTH_SHORT)
                snackbar.setAction(getString(R.string.undo)) {
                    lifecycleScope.launch {
                        val deletedExpesense = viewModel.getDeletedItem().toExpense()
                        viewModel.undoDeleteExpense(deletedExpesense, false, 1)
                    }

                }.show()
            }else{
                //Show snackbar with failure message
                Snackbar.make(binding.rvExpenseList, getString(R.string.exclude_item_fail_message), Snackbar.LENGTH_SHORT).show()
            }
        }

        viewModel.deleteEarningResult.observe(viewLifecycleOwner){ result ->
            if(result){
                //Show snackbar to undo the action
                val snackbar = Snackbar.make(binding.rvExpenseList, getString(R.string.excluded_item), Snackbar.LENGTH_SHORT)
                snackbar.setAction(getString(R.string.undo)) {
                    lifecycleScope.launch {
                        viewModel.undoDeleteEarning(viewModel.getDeletedItem().toEarning())
                    }

                }.show()
            }else{
                //Show snackbar with failure message
                Snackbar.make(binding.rvExpenseList, getString(R.string.exclude_item_fail_message), Snackbar.LENGTH_SHORT).show()
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
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.delete_expense))
                    .setMessage(getString(R.string.exclude_installment_expense_message_instruction_message))
                    .setPositiveButton(R.string.ok) { dialog, which ->
                        viewModel.updateShowFilteredList()
                    }
                    .show()
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
                        viewModel.showAllTransactions()
                    }
                    binding.btExpensesTransacList.id -> {
                        binding.btAllTransacList.isClickable = true
                        binding.btExpensesTransacList.isClickable = false
                        binding.btEarningsTransacList.isClickable = true
                        viewModel.updateTransactionTypeFilter(StringConstants.DATABASE.EXPENSE)
                        viewModel.showExpenseTransactions()
                    }
                    binding.btEarningsTransacList.id -> {
                        binding.btAllTransacList.isClickable = true
                        binding.btExpensesTransacList.isClickable = true
                        binding.btEarningsTransacList.isClickable = false
                        viewModel.updateTransactionTypeFilter(StringConstants.DATABASE.EARNING)
                        viewModel.showEarningTransactions()
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
                Snackbar.make(
                    binding.rvExpenseList,
                    getString(R.string.add_earning_success_message),
                    Snackbar.LENGTH_SHORT
                ).show()
            } else {
                Snackbar.make(
                    binding.rvExpenseList,
                    getString(R.string.add_earning_failure_message),
                    Snackbar.LENGTH_LONG)
                    .show()
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
            if(resultCode == Activity.RESULT_OK){
                viewModel.updateOperation(StringConstants.OPERATIONS.UPDATE)
                Snackbar.make(
                    binding.rvExpenseList,
                    getString(R.string.update_expense_success_message),
                    Snackbar.LENGTH_SHORT
                ).show()
            }else if(resultCode == Activity.RESULT_CANCELED){
                Snackbar.make(
                    binding.rvExpenseList,
                    getString(R.string.update_expense_fail_message),
                    Snackbar.LENGTH_SHORT
                ).show()
            }else if(resultCode == StringConstants.RESULT_CODES.DELETE_INSTALLMENT_EXPENSE_RESULT_OK){
                viewModel.updateOperation(StringConstants.OPERATIONS.DELETE)
                val deletedItem = viewModel.getEditingTransaction()
                viewModel.updateDeletedItem(deletedItem)
                Snackbar.make(
                    binding.rvExpenseList,
                    getString(R.string.delete_expense_success_message),
                    Snackbar.LENGTH_SHORT
                ).show()
            }else if(resultCode == StringConstants.RESULT_CODES.DELETE_INSTALLMENT_EXPENSE_RESULT_FAILURE){
                Snackbar.make(
                    binding.rvExpenseList,
                    getString(R.string.delete_expense_fail_message),
                    Snackbar.LENGTH_SHORT
                ).show()
            }else if(resultCode == StringConstants.RESULT_CODES.EDIT_EARNING_EXPENSE_RESULT_OK){
                viewModel.updateOperation(StringConstants.OPERATIONS.UPDATE)
                Snackbar.make(
                    binding.rvExpenseList,
                    getString(R.string.edit_earning_success_message),
                    Snackbar.LENGTH_SHORT
                ).show()
            }else if(resultCode == StringConstants.RESULT_CODES.EDIT_EARNING_EXPENSE_RESULT_FAILURE){
                Snackbar.make(
                    binding.rvExpenseList,
                    getString(R.string.edit_earning_failure_message),
                    Snackbar.LENGTH_SHORT
                ).show()
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

    private fun generateFileAndShare() {
        val transactionsLists = getTransactionsListForFile()
        val gson = Gson()
        var expensesJsonArray = gson.toJsonTree(transactionsLists.first).asJsonArray
        var earningsJsonArray = gson.toJsonTree(transactionsLists.second).asJsonArray

        var file = generateXlsFile(
            requireActivity(), StringConstants.XLS.EXPENSE_TITLES, StringConstants.XLS.EARNINGS_TITLES,
            StringConstants.XLS.EXPENSE_INDEX_NAME, StringConstants.XLS.EARNINGS_INDEX_NAME, expensesJsonArray,
            earningsJsonArray, HashMap(), StringConstants.XLS.SHEET_NAME_EXPENSES, StringConstants.XLS.SHEET_NAME_EARNINGS,
            StringConstants.XLS.FILE_NAME, 0
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

        val tvTextFilter = dialogView.findViewById<TextView>(R.id.tv_text_filter)
        val tvTextFilterValues = dialogView.findViewById<TextView>(R.id.tv_text_filter_values)
        val vSeparatorTextFilterValues = dialogView.findViewById<View>(R.id.v_dialog_transaction_fragment_separator_line_2)
        val rdTextFilter = dialogView.findViewById<RadioButton>(R.id.rb_text_filter)
        val tvDateFilter = dialogView.findViewById<TextView>(R.id.tv_date_filter)
        val tvDateFilterValues = dialogView.findViewById<TextView>(R.id.tv_date_filter_values)
        val vSeparatorDateFilterValues = dialogView.findViewById<View>(R.id.v_dialog_transaction_fragment_separator_line_5)
        val rdDateFilter = dialogView.findViewById<RadioButton>(R.id.rb_date_filter)

        //verify radio state and set value
        val textFilterState = viewModel.textFilterState.value
        if(textFilterState != null && textFilterState != false){
            rdTextFilter.isChecked = textFilterState
            var filterTextValuesString = ""
            viewModel.textFilterValues.value!!.forEachIndexed { index, filter ->
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

        builder.setView(dialogView)

        builder.setNegativeButton(getString(R.string.cancel)){dialog, which ->

        }

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
                viewModel.applyTextFilter(filter)
            }
        }

        val dialog = builder.create()

        dialog.show()
    }

    private fun clearAllFilter(){
        viewModel.setIsFilteredState(false)
        clearTextFilter()
        clearDateFilter()
    }

    private fun clearTextFilter(){
        viewModel.setTextFilterState(false)
        viewModel.clearTextFilterValues()
    }

    private fun clearDateFilter(){
        viewModel.setDateFilterState(false)
        viewModel.clearDateFilterValues()
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

}