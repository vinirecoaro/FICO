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

        binding.rvExpenseList.layoutManager = LinearLayoutManager(requireContext())
        transactionListAdapter = TransactionListAdapter(categoriesList.getExpenseCategoryListFull(), categoriesList.getEarningCategoryList())
        binding.rvExpenseList.adapter = transactionListAdapter

        val swipeToDeleteCallback =
            SwipeToDeleteCallback(binding.rvExpenseList, viewModel, transactionListAdapter, viewLifecycleOwner)
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvExpenseList)

        binding.actvDate.setText(DateFunctions().getCurrentlyDateForFilter())
        viewModel.updateFilter(DateFunctions().getCurrentlyDateForFilter())

        // Initial selected button on toggle group
        binding.tbTransacList.check(binding.btAllTransacList.id)
        binding.btAllTransacList.isClickable = false

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
        viewModel.getExpenseList(binding.actvDate.text.toString())
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
        return super.onPrepareOptionsMenu(menu)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            //TODO Change function and instructions to export expenses to file
            /*R.id.expense_list_menu_generate_excel_file -> {
                if (checkPermission()){
                    generateFileAndShare()
                }else{
                    lifecycleScope.launch {
                        requestPermission()
                    }
                }
                return true
            }*/

            R.id.transaction_list_menu_filter -> {
                filterDialog()
                return true
            }

            R.id.transaction_list_menu_clear_filter -> {
                val transactionList = viewModel.transactionsListLiveData.value?.toList()
                if(!transactionList.isNullOrEmpty()){
                    clearAllFilter()
                    transactionListAdapter.updateTransactions(transactionList)
                }
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun getExpenseList(): MutableList<Expense> {
        val expenseList: MutableList<Expense> = ArrayList()
        viewModel.expensesLiveData.observe(viewLifecycleOwner, Observer { expenses ->
            for (expense in expenses) {
                var modifiedExpense = Expense(
                    "",
                    FormatValuesFromDatabase().priceToFile(expense.price),
                    expense.description,
                    expense.category,
                    expense.paymentDate,
                    expense.purchaseDate,
                    expense.inputDateTime
                )
                expenseList.add(modifiedExpense)
            }
        })
        return expenseList
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
        }
        binding.ivClearFilter.setOnClickListener {
            binding.actvDate.setText("")
            viewModel.getEarningList("")
            viewModel.getExpenseList("")
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

        viewModel.transactionsListLiveData.observe(viewLifecycleOwner){ transactionsList ->
            transactionListAdapter.updateTransactions(transactionsList)
            transactionListAdapter.setOnItemClickListener(object : OnListItemClick {
                override fun onListItemClick(position: Int) {
                    val selectItem = transactionsList[position]
                    editExpense(selectItem)
                }
            })
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

        viewModel.deleteExpenseResult.observe(viewLifecycleOwner){
            if(it){
                //Show snackbar to undo the action
                val snackbar = Snackbar.make(binding.rvExpenseList, "Item excluido", Snackbar.LENGTH_LONG)
                snackbar.setAction("Desfazer") {
                    lifecycleScope.launch {
                        viewModel.undoDeleteExpense(viewModel.deletedItem!!, false, 1)
                    }

                }.show()
            }else{
                //Show snackbar with failure message
                Snackbar.make(binding.rvExpenseList, "Falha ao excluir item", Snackbar.LENGTH_LONG).show()
            }
        }

        viewModel.addExpenseResult.observe(viewLifecycleOwner){result ->
            if(result){
                Snackbar.make(binding.rvExpenseList, "Exclusão cancelada com sucesso", Snackbar.LENGTH_LONG).show()
            }else{
                Snackbar.make(binding.rvExpenseList, "Falha ao cancelar a exclusão do item", Snackbar.LENGTH_LONG).show()
            }
        }

        viewModel.installmentExpenseSwiped.observe(viewLifecycleOwner){result ->
            if(result){
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Apagar gasto")
                    .setMessage("Para apagar um gasto parcelado clique no item desejado e faça a exclusão na janela de edição")
                    .setPositiveButton("Ok") { dialog, which ->
                        viewModel.getExpenseList(viewModel.monthFilterLiveData.value.toString())
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
                    }
                    is TransactionFragmentState.Error -> {
                        binding.ivNoInfoAvailable.visibility = View.VISIBLE
                        binding.tvNoInfoAvailable.visibility = View.VISIBLE
                        binding.pbTransactionList.visibility = View.GONE
                        binding.actvDate.visibility = View.GONE
                        binding.ivClearFilter.visibility = View.GONE
                        binding.rvExpenseList.visibility = View.GONE
                    }
                    TransactionFragmentState.Loading -> {
                        binding.ivNoInfoAvailable.visibility = View.GONE
                        binding.tvNoInfoAvailable.visibility = View.GONE
                        binding.pbTransactionList.visibility = View.VISIBLE
                        binding.actvDate.visibility = View.GONE
                        binding.ivClearFilter.visibility = View.GONE
                        binding.rvExpenseList.visibility = View.GONE
                    }
                    TransactionFragmentState.Success -> {
                        binding.ivNoInfoAvailable.visibility = View.GONE
                        binding.tvNoInfoAvailable.visibility = View.GONE
                        binding.pbTransactionList.visibility = View.GONE
                        binding.actvDate.visibility = View.VISIBLE
                        binding.ivClearFilter.visibility = View.VISIBLE
                        binding.rvExpenseList.visibility = View.VISIBLE
                    }
                }
            }
        }

        viewModel.filteredTransactionsListLiveData.observe(viewLifecycleOwner){ filteredTransactionList ->
            transactionListAdapter.updateTransactions(filteredTransactionList)
            val total = viewModel.calculateFilteredTotalValue(filteredTransactionList)
            val totalAbsolute = total.abs()
            if(total < BigDecimal(0)){
                binding.etTotalPrice.setTextColor(Color.RED)
            }else{
                binding.etTotalPrice.setTextColor(Color.GREEN)
            }
            binding.etTotalPrice.setText(FormatValuesFromDatabase().price(totalAbsolute.toString()))
            binding.tilTotalPrice.visibility = View.VISIBLE
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
                    }
                    binding.btExpensesTransacList.id -> {
                        binding.btAllTransacList.isClickable = true
                        binding.btExpensesTransacList.isClickable = false
                        binding.btEarningsTransacList.isClickable = true
                    }
                    binding.btEarningsTransacList.id -> {
                        binding.btAllTransacList.isClickable = true
                        binding.btExpensesTransacList.isClickable = true
                        binding.btEarningsTransacList.isClickable = false
                    }
                }
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

    fun editExpense(transaction: Transaction) {
        val intent = Intent(requireContext(), EditTransactionActivity::class.java)
        if(transaction.type == StringConstants.DATABASE.EXPENSE){
            val sureExpense = Expense(
                id = transaction.id,
                price = transaction.price,
                description = transaction.description,
                category = transaction.category,
                paymentDate = transaction.paymentDate,
                purchaseDate = transaction.purchaseDate,
                inputDateTime = transaction.inputDateTime,
                nOfInstallment = transaction.nOfInstallment
            )
            intent.putExtra("expense", sureExpense)
            startActivityForResult(intent, StringConstants.REQUEST_CODES.EXPENSE_LIST_TO_EDIT_EXPENSE)
        }else{
            //TODO edit earning
            Toast.makeText(requireContext(),"Em construção, aguarde !!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Check if edit expense was successfully completed
        if(requestCode == StringConstants.REQUEST_CODES.EXPENSE_LIST_TO_EDIT_EXPENSE){
            if(resultCode == Activity.RESULT_OK){
                Snackbar.make(
                    binding.rvExpenseList,
                    "Gasto alterado com sucesso",
                    Snackbar.LENGTH_LONG
                ).show()
            }else if(resultCode == Activity.RESULT_CANCELED){
                Snackbar.make(
                    binding.rvExpenseList,
                    "Falha ao alterar gasto",
                    Snackbar.LENGTH_LONG
                ).show()
            }else if(resultCode == StringConstants.RESULT_CODES.DELETE_INSTALLMENT_EXPENSE_RESULT_OK){
                Snackbar.make(
                    binding.rvExpenseList,
                    "Gasto excluído com sucesso",
                    Snackbar.LENGTH_LONG
                ).show()
            }else if(resultCode == StringConstants.RESULT_CODES.DELETE_INSTALLMENT_EXPENSE_RESULT_FAILURE){
                Snackbar.make(
                    binding.rvExpenseList,
                    "Falha ao excluir gasto",
                    Snackbar.LENGTH_LONG
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
                        "Manage External Storage Permission is denied ...",
                        Toast.LENGTH_LONG
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
                        "Manage External Storage Permission is denied ...",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun generateFileAndShare() {
        val expenseList = getExpenseList()
        val gson = Gson()
        var jsonArray = gson.toJsonTree(expenseList).asJsonArray

        var file = generateXlsFile(
            requireActivity(), StringConstants.XLS.TITLES,
            StringConstants.XLS.INDEX_NAME, jsonArray, HashMap(), StringConstants.XLS.SHEET_NAME,
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
            textFilter()
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
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            // Convert milliseconds to the desired format
            val startDate = dateFormat.format(Date(it.first ?: 0))
            val endDate = dateFormat.format(Date(it.second ?: 0))

            val dates = Pair(startDate,endDate)

            viewModel.applyDateFilter(dates)
        }
    }

    private fun textFilter(){
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
        binding.tilTotalPrice.visibility = View.GONE
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

}