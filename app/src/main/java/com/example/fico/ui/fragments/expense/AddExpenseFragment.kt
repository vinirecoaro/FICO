package com.example.fico.ui.fragments.expense

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.text.*
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.fico.R
import com.example.fico.databinding.FragmentAddExpenseBinding
import com.example.fico.model.Expense
import com.example.fico.service.UploadFile
import com.example.fico.util.constants.AppConstants
import com.example.fico.ui.activities.expense.ComonExpenseImportFileInstructionsActivity
import com.example.fico.ui.activities.expense.InstallmentExpenseImportFileInstructionsActivity
import com.example.fico.ui.interfaces.OnButtonClickListener
import com.example.fico.ui.viewmodel.AddExpenseViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import android.view.inputmethod.InputMethodManager

class AddExpenseFragment : Fragment(), OnButtonClickListener {

    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!
    private val categoryOptions = arrayOf(
        "Comida", "Transporte",
        "Investimento", "Necessidade",
        "Remédio", "Entretenimento"
    )
    private val viewModel by viewModels<AddExpenseViewModel>()
    private val READ_COMON_EXPENSE_REQUEST_CODE: Int = 43
    private val READ_INSTALLMENT_EXPENSE_REQUEST_CODE: Int = 44
    private val permissionRequestCode = 123
    private val permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private companion object {
        private const val STORAGE_PERMISSION_CODE = 100
        private const val TAG = "PERMISSION_TAG"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddExpenseBinding.inflate(
            inflater, container, false
        )
        var rootView = binding.root

        setUpListeners()
        actvConfig()
        setColorBasedOnTheme()

        binding.etDate.inputType = InputType.TYPE_NULL

        setMaxLength(binding.etInstallments, 3)

        return rootView
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        binding.etDate.setText(viewModel.getCurrentlyDate())
        val filter = IntentFilter().apply {
            addAction(AppConstants.UPLOAD_FILE_SERVICE.SUCCESS_UPLOAD)
        }
        requireContext().registerReceiver(receiver, filter)
    }

    override fun onPause() {
        super.onPause()
        requireContext().unregisterReceiver(receiver)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_expense_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add_expense_menu_common -> {
                binding.etInstallments.visibility = View.GONE
                binding.etPrice.setText("")
                binding.etDescription.setText("")
                binding.actvCategory.setText("")
                binding.etInstallments.setText("")
                return true
            }

            R.id.add_expense_menu_installments -> {
                binding.etInstallments.visibility = View.VISIBLE
                binding.etPrice.setText("")
                binding.etDescription.setText("")
                binding.actvCategory.setText("")
                return true
            }

            R.id.add_expense_menu_get_data_from_file -> {
                lifecycleScope.launch {
                    if (checkPermission()) {
                        if (viewModel.checkIfExistDefaultBudget().await()) {
                            importDataAlertDialog()
                        } else {
                            setUpDefaultBudgetAlertDialog()
                        }
                    } else {
                        requestPermission()
                    }
                }
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpListeners() {
        binding.btSave.setOnClickListener {
            binding.btSave.isEnabled = false
            lifecycleScope.launch(Dispatchers.Main) {
                if (binding.etInstallments.visibility == View.GONE) {
                    if (verifyFields(
                            binding.etPrice,
                            binding.etDescription,
                            binding.actvCategory,
                            binding.etDate
                        )
                    ) {
                        val existsDefaultBudget = viewModel.checkIfExistDefaultBudget().await()
                        if (existsDefaultBudget){
                            if(viewModel.addExpense(
                                binding.etPrice.text.toString(),
                                binding.etDescription.text.toString(),
                                binding.actvCategory.text.toString(),
                                binding.etDate.text.toString(),
                            false).await()){
                                hideKeyboard(requireContext(),binding.btSave)
                                Toast.makeText(requireContext(), "Gasto adicionado com sucesso", Toast.LENGTH_LONG).show()
                                binding.etPrice.setText("")
                                binding.etDescription.setText("")
                                binding.actvCategory.setText("")
                                binding.etInstallments.setText("")
                            }
                        } else {
                            if (setUpDefaultBudgetAlertDialog().await()) {
                                if(viewModel.addExpense(
                                        binding.etPrice.text.toString(),
                                        binding.etDescription.text.toString(),
                                        binding.actvCategory.text.toString(),
                                        binding.etDate.text.toString(),
                                        false).await()){
                                    hideKeyboard(requireContext(),binding.btSave)
                                    Toast.makeText(requireContext(), "Gasto adicionado com sucesso", Toast.LENGTH_LONG).show()
                                    binding.etPrice.setText("")
                                    binding.etDescription.setText("")
                                    binding.actvCategory.setText("")
                                    binding.etInstallments.setText("")
                                }
                            }
                        }
                    }
                } else if (binding.etInstallments.visibility == View.VISIBLE) {
                    if (verifyFields(
                            binding.etPrice,
                            binding.etDescription,
                            binding.actvCategory,
                            binding.etDate
                        )
                    ){
                        if(binding.etInstallments.text.toString() != "0"){
                            val existsDefaultBudget = viewModel.checkIfExistDefaultBudget().await()
                            if (existsDefaultBudget) {
                                if(viewModel.addExpense(
                                        binding.etPrice.text.toString(),
                                        binding.etDescription.text.toString(),
                                        binding.actvCategory.text.toString(),
                                        binding.etDate.text.toString(),
                                        true,
                                        binding.etInstallments.text.toString().toInt()
                                    ).await()){
                                        hideKeyboard(requireContext(),binding.btSave)
                                        Toast.makeText(requireContext(), "Gasto adicionado com sucesso", Toast.LENGTH_LONG).show()
                                        binding.etPrice.setText("")
                                        binding.etDescription.setText("")
                                        binding.actvCategory.setText("")
                                        binding.etInstallments.setText("")
                                }
                            } else {
                                if (setUpDefaultBudgetAlertDialog().await()) {
                                    if(viewModel.addExpense(
                                        binding.etPrice.text.toString(),
                                        binding.etDescription.text.toString(),
                                        binding.actvCategory.text.toString(),
                                        binding.etDate.text.toString(),
                                        true,
                                        binding.etInstallments.text.toString().toInt()
                                    ).await()){
                                        hideKeyboard(requireContext(),binding.btSave)
                                        Toast.makeText(requireContext(), "Gasto adicionado com sucesso", Toast.LENGTH_LONG).show()
                                        binding.etPrice.setText("")
                                        binding.etDescription.setText("")
                                        binding.actvCategory.setText("")
                                        binding.etInstallments.setText("")
                                    }
                                }
                            }
                        }else{
                            Toast.makeText(requireContext(), "O número de parcelas não pode ser 0", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
            binding.btSave.isEnabled = true
        }

        binding.actvCategory.setOnClickListener {
            binding.actvCategory.showDropDown()
            binding.dpDateExpense.visibility = View.GONE
        }

        binding.ivDate.setOnClickListener {
            binding.btSave.visibility = View.VISIBLE
            binding.dpDateExpense.visibility = View.VISIBLE
            hideKeyboard(requireContext(), it)
            binding.dpDateExpense.setOnDateChangedListener {
                    _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = String.format(
                    "%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear
                )
                binding.etDate.setText(selectedDate)
            }
        }

        binding.etPrice.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.dpDateExpense.visibility = View.GONE
            }
        }

        binding.etDescription.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.dpDateExpense.visibility = View.GONE
            }
        }

        binding.actvCategory.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.dpDateExpense.visibility = View.GONE
            }
        }

        binding.etPrice.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                if (!text.isEmpty()) {
                    val parsed = text.replace("[^\\d]".toRegex(), "").toLong()
                    val formatted = (
                            NumberFormat.getCurrencyInstance().format(parsed / 100.0)
                            )
                    binding.etPrice.removeTextChangedListener(this)
                    binding.etPrice.setText(formatted)
                    binding.etPrice.setSelection(formatted.length)
                    binding.etPrice.addTextChangedListener(this)
                }
            }
        })
    }

    private fun actvConfig() {
        val adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_dropdown_item_1line, categoryOptions
        )
        binding.actvCategory.setAdapter(adapter)
    }

    private fun verifyFields(vararg text: EditText): Boolean {
        for (i in text) {
            if (i.text.toString() == "" || i == null) {
                Snackbar.make(
                    binding.btSave, "Preencher o campo ${i.hint}", Snackbar.LENGTH_LONG
                ).show()
                return false
            }
        }
        return true
    }

    override fun onSaveButtonFragmentClick() {
        binding.btSave.performClick()
    }

    private fun setUpDefaultBudgetAlertDialog(): CompletableDeferred<Boolean> {
        val result = CompletableDeferred<Boolean>()
        val builder = AlertDialog.Builder(requireContext())

        builder.setTitle("Definir o orçamento padrão")

        val defaultBudget = EditText(requireContext())
        defaultBudget.hint = "Orçamento Padrão"
        defaultBudget.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
        builder.setView(defaultBudget)

        defaultBudget.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                if (!text.isEmpty()) {
                    val parsed = text.replace("[^\\d]".toRegex(), "").toLong()
                    val formatted = (
                            NumberFormat.getCurrencyInstance().format(parsed / 100.0)
                            )
                    defaultBudget.removeTextChangedListener(this)
                    defaultBudget.setText(formatted)
                    defaultBudget.setSelection(formatted.length)
                    defaultBudget.addTextChangedListener(this)
                }
            }
        })

        builder.setPositiveButton("Salvar") { dialog, which ->
            val saveButton = (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            saveButton.isEnabled = false
            lifecycleScope.launch {
                if (defaultBudget.text.toString() != "") {

                    val regex = Regex("[\\d,.]+")
                    val justNumber = regex.find(defaultBudget.text.toString())
                    val formatNum = DecimalFormat("#.##")
                    val numClean = justNumber!!.value
                        .replace(",", "")
                        .replace(".", "").toFloat()
                    val formatedNum = formatNum.format(numClean / 100)
                    val formattedNumString = formatedNum.toString()
                        .replace(",", ".")

                    if (viewModel.setDefaultBudget(formattedNumString).await()) {
                        val rootView: View? = activity?.findViewById(android.R.id.content)
                        if (rootView != null) {
                            val snackbar = Snackbar.make(
                                rootView, "Orçamento padrão salvo com sucesso", Snackbar.LENGTH_LONG
                            )
                            snackbar.show()
                            result.complete(true)
                        }
                    } else {
                        val rootView: View? = activity?.findViewById(android.R.id.content)
                        if (rootView != null) {
                            val snackbar = Snackbar.make(
                                rootView, "Falha ao editar o Orçamento Padrão", Snackbar.LENGTH_LONG
                            )
                            snackbar.show()
                            result.complete(false)
                        }
                    }
                }
            }
            saveButton.isEnabled = true
        }

        builder.setNegativeButton("Cancelar") { dialog, which ->

        }

        val alertDialog = builder.create()
        alertDialog.show()
        return result
    }

    fun setMaxLength(editText: EditText, maxLength: Int) {
        val inputFilter = object : InputFilter {
            override fun filter(
                source: CharSequence?, start: Int,
                end: Int, dest: Spanned?, dstart: Int, dend: Int
            ): CharSequence? {
                val inputText = editText.text.toString() + source.toString()
                if (inputText.length <= maxLength) {
                    return null // Aceita a entrada
                }
                return "" // Rejeita a entrada se exceder o limite
            }
        }
        val filters = editText.filters
        val newFilters = if (filters != null) {
            val newFilters = filters.copyOf(filters.size + 1)
            newFilters[filters.size] = inputFilter
            newFilters
        } else {
            arrayOf(inputFilter)
        }
        editText.filters = newFilters
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setColorBasedOnTheme() {
        when (context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.ivDate.setImageResource(R.drawable.baseline_calendar_month_light)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.ivDate.setImageResource(R.drawable.baseline_calendar_month_dark)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }
    }

    private fun performFileSearch(requestCode : Int) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/vnd.ms-excel" // Define o tipo MIME para arquivos Excel
        }

        startActivityForResult(intent, requestCode)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)

        if (requestCode == READ_COMON_EXPENSE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            resultData?.data?.also { uri ->
                val inputStream = requireContext().contentResolver.openInputStream(uri)

                if (inputStream != null) {
                    val outputStream = FileOutputStream(getNewFileUri().path)

                    inputStream.use { input ->
                        outputStream.use { output ->
                            input.copyTo(output)
                        }
                    }
                    // Work with a copy of file
                    val newPath = getNewFileUri().path.toString()
                    var readFileResult = readFromExcelFile(newPath)
                    if (readFileResult.second) {
                        lifecycleScope.launch(Dispatchers.Main) {

                            val expensesList = readFileResult.first

                            // innit the upload data to database service
                            val serviceIntent = Intent(requireContext(), UploadFile()::class.java)
                            serviceIntent.putParcelableArrayListExtra(
                                "expensesList", ArrayList(expensesList)
                            )
                            requireContext().startService(serviceIntent)
                        }
                        Toast.makeText(
                            requireContext(),
                            "Dados corretos, salvando dados !",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Falha ao importar os dados, " +
                                    "verifique se os dados estão corretos !",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        } else if ( requestCode == READ_INSTALLMENT_EXPENSE_REQUEST_CODE && resultCode == Activity.RESULT_OK ){
            resultData?.data?.also { uri ->
                val inputStream = requireContext().contentResolver.openInputStream(uri)

                if (inputStream != null) {
                    val outputStream = FileOutputStream(getNewFileUri().path)

                    inputStream.use { input ->
                        outputStream.use { output ->
                            input.copyTo(output)
                        }
                    }
                    // Work with a copy of file
                    val newPath = getNewFileUri().path.toString()
                    var readFileResult = readFromExcelFile(newPath)
                    if (readFileResult.second) {
                        lifecycleScope.launch(Dispatchers.Main) {

                            val expensesList = readFileResult.first

                            // innit the upload data to database service
                            val serviceIntent = Intent(requireContext(), UploadFile()::class.java)
                            serviceIntent.putParcelableArrayListExtra(
                                "expensesList", ArrayList(expensesList)
                            )
                            serviceIntent.putExtra("installmentExpense", true)
                            requireContext().startService(serviceIntent)
                        }
                        Toast.makeText(
                            requireContext(),
                            "Dados corretos, salvando dados !",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Falha ao importar os dados, " +
                                    "verifique se os dados estão corretos !",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    fun readFromExcelFile(filepath: String): Pair<MutableList<Expense>, Boolean> {
        val inputStream = FileInputStream(filepath)
        //Instantiate Excel workbook using existing file:
        var xlWb = WorkbookFactory.create(inputStream)
        //Get reference to first sheet:
        val xlWs = xlWb.getSheetAt(0)

        var price = ""
        var description = ""
        var category = ""
        var date = ""
        var installment = ""

        var result = true

        val expenseList = mutableListOf<Expense>()

        val numberOfRows = xlWs.lastRowNum

        for (rowIndex in xlWs.firstRowNum + 1..numberOfRows) {
            val row = xlWs.getRow(rowIndex) ?: continue

            val numberOfColumns = row.lastCellNum

            // Iterando pelas colunas dentro da linha atual
            for (columnIndex in row.firstCellNum until numberOfColumns) {
                val cell = row.getCell(columnIndex)
                val cellValue = getCellValueAsString(cell)
                when (columnIndex) {
                    0 -> {
                        price = cellValue.replace("R$", "")
                        price = cellValue.replace("R$ ", "")
                        price = cellValue.replace("$", "")
                        price = cellValue.replace("$ ", "")
                        price = cellValue.replace(",", ".")
                        if (price == "xxx" || price == "XXX") {
                            return Pair(expenseList, result)
                        } else if (price.toDoubleOrNull() == null) {
                            result = false
                            expenseList.clear()
                            return Pair(expenseList, result)
                        }
                    }
                    1 -> {
                        description = cellValue.replace("  ", " ")
                        description = cellValue.replace("  ", " ")
                    }
                    2 -> {
                        category = cellValue
                    }
                    3 -> {
                        if (cell.cellType == CellType.STRING) {
                            date = cellValue
                        } else if (cell.cellType == CellType.NUMERIC) {
                            val dateDouble = cell.numericCellValue
                            val dateformat = SimpleDateFormat("dd/MM/yyyy")
                            date = dateformat.format(DateUtil.getJavaDate(dateDouble))
                        }

                        if (verifyDateFormat(date)) {
                            val day = date.substring(0, 2)
                            val month = date.substring(3, 5)
                            val year = date.substring(6, 10)
                            date = "$year-$month-$day"
                        } else {
                            result = false
                            expenseList.clear()
                            return Pair(expenseList, result)
                        }
                    }
                    4 -> {
                        installment = cellValue
                    }
                }

            }
            val expense = Expense("", price, description, category, date, installment)
            expenseList.add(expense)
        }
        xlWb.close()
        inputStream.close()

        return Pair(expenseList, result)
    }

    private fun verifyDateFormat(date: String): Boolean {
        val formatoData = "\\d{2}/\\d{2}/\\d{4}" // Expressão regular para o formato "dd/mm/aaaa"
        return date.matches(Regex(formatoData))
    }

    fun getCellValueAsString(cell: Cell?): String {
        return when (cell?.cellType) {
            CellType.STRING -> cell.stringCellValue
            CellType.NUMERIC -> cell.numericCellValue.toString()
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            else -> ""
        }
    }

    // Método para gerar uma URI para o novo arquivo (exemplo).
    private fun getNewFileUri(): Uri {
        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val newFile = File(downloadsDir, "expenses.xlsx")
        return Uri.fromFile(newFile)
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Log.d(TAG, "requestPermission: try")
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                val uri = Uri.fromParts(
                    "package", requireActivity().packageName, null
                )
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
                requireActivity(), permissions, STORAGE_PERMISSION_CODE
            )
        }
    }

    private val storageActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
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
                requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            val read = ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE
            )
            write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == permissionRequestCode) {
            if (grantResults.isNotEmpty()) {
                val write = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val read = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (write && read) {
                    Log.d(
                        TAG,
                        "onRequestPermissionResult: External Storage Permission granted"
                    )
                } else {
                    Log.d(
                        TAG,
                        "onRequestPermissionResult: External Storage Permission denied ..."
                    )
                    Toast.makeText(
                        requireContext(),
                        "Manage External Storage Permission is denied ...",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun importDataAlertDialog() {
        MaterialAlertDialogBuilder(requireContext()).setTitle("Importar Gastos")
            .setMessage("Selecione o tipo de gasto que deseja importar.")
            .setNeutralButton("Gastos Parcelados") { dialog, which ->
                importInstallmentExpenseTypeAlertDialog()
            }.setPositiveButton("Gastos Comuns") { dialog, which ->
                importComonExpenseTypeAlertDialog()
            }.show()
    }

    private fun importComonExpenseTypeAlertDialog() {
        MaterialAlertDialogBuilder(requireContext()).setTitle("Importar Gastos Comuns")
            .setMessage("Os dados devem estar no formato correto.")
            .setNeutralButton("Ver Formato Correto") { dialog, which ->
                startActivity(Intent(requireContext(), ComonExpenseImportFileInstructionsActivity::class.java))
            }.setPositiveButton("Selecionar Arquivo") { dialog, which ->
                performFileSearch(READ_COMON_EXPENSE_REQUEST_CODE)
            }.show()
    }

    private fun importInstallmentExpenseTypeAlertDialog() {
        MaterialAlertDialogBuilder(requireContext()).setTitle("Importar Gastos Parcelados")
            .setMessage("Os dados devem estar no formato correto.")
            .setNeutralButton("Ver Formato Correto") { dialog, which ->
                startActivity(Intent(requireContext(), InstallmentExpenseImportFileInstructionsActivity::class.java))
            }.setPositiveButton("Selecionar Arquivo") { dialog, which ->
                performFileSearch(READ_INSTALLMENT_EXPENSE_REQUEST_CODE)
            }.show()
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Action when receive Broadcast
            if (intent?.action == AppConstants.UPLOAD_FILE_SERVICE.SUCCESS_UPLOAD) {
                // Show message to user
                Toast.makeText(
                    context, "Dados salvos com sucesso !!", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun hideKeyboard(context: Context, view: View){
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken,0)
    }

}