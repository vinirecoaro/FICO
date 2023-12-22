package com.example.fico.ui.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.fico.R
import com.example.fico.databinding.FragmentAddExpenseBinding
import com.example.fico.model.Expense
import com.example.fico.model.ImportFileInstructionsComponents
import com.example.fico.service.UploadFile
import com.example.fico.ui.ImportFileInstructionsActivity
import com.example.fico.ui.interfaces.OnButtonClickListener
import com.example.fico.ui.viewmodel.AddExpenseSetBudgetSharedViewModel
import com.example.fico.ui.viewmodel.AddExpenseViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.apache.poi.hssf.usermodel.HSSFWorkbook
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

class AddExpenseFragment : Fragment(), OnButtonClickListener{

    private var _binding : FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!
    private val categoryOptions = arrayOf("Comida", "Transporte", "Investimento", "Necessidade", "Remédio", "Entretenimento")
    private val viewModel by viewModels<AddExpenseViewModel>()
    private val sharedViewModel: AddExpenseSetBudgetSharedViewModel by activityViewModels()
    private val READ_REQUEST_CODE: Int = 42
    private val permissionRequestCode = 123
    private val permissions = arrayOf(
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private companion object{
        private const val STORAGE_PERMISSION_CODE = 100
        private const val TAG = "PERMISSION_TAG"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddExpenseBinding.inflate(inflater,container,false)
        var rootView = binding.root

        setUpListeners()
        actvConfig()
        setColorBasedOnTheme()

        binding.etDate.setText(viewModel.getCurrentlyDate())
        binding.etDate.inputType = InputType.TYPE_NULL

        setMaxLength(binding.etInstallments, 3)

        return rootView
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
                    if (checkPermission()){
                        if(viewModel.checkIfExistDefaultBudget().await()){
                            importDataAlertDialog()
                        }else{
                            setUpDefaultBudgetAlertDialog()
                        }
                    }else{
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
                if(binding.etInstallments.visibility == View.GONE){
                    if(verifyFields(binding.etPrice, binding.etDescription, binding.actvCategory, binding.etDate)){
                        val day = binding.etDate.text.toString().substring(0, 2)
                        val month = binding.etDate.text.toString().substring(3, 5)
                        val year = binding.etDate.text.toString().substring(6, 10)
                        val modifiedDate = "$year-$month-$day"
                        val checkDate = "$year-$month"

                        val regex = Regex("[\\d,.]+")
                        val justNumber = regex.find(binding.etPrice.text.toString())
                        val formatNum = DecimalFormat("#.#####")
                        val numClean = justNumber!!.value.replace(",","").replace(".","").toFloat()
                        val formatedNum = formatNum.format(numClean/100)
                        val formattedNumString = formatedNum.toString().replace(",",".")

                        val existsDate = viewModel.checkIfExistsDateOnDatabse(checkDate).await()
                        if (existsDate) {
                            viewModel.addExpense(
                                formattedNumString,
                                binding.etDescription.text.toString(),
                                binding.actvCategory.text.toString(),
                                modifiedDate
                            )
                            binding.etPrice.setText("")
                            binding.etDescription.setText("")
                            binding.actvCategory.setText("")
                        } else {
                            binding.btSave.visibility = View.GONE
                            binding.dpDateExpense.visibility = View.GONE
                            binding.fragSetBudget.visibility = View.VISIBLE
                            val setMonthBudget = SetMonthBudget.newInstance(
                                formattedNumString,
                                binding.etDescription.text.toString(),
                                binding.actvCategory.text.toString(),
                                modifiedDate
                            )

                            childFragmentManager.beginTransaction()
                                .replace(binding.fragSetBudget.id, setMonthBudget)
                                .commit()

                        }
                    }
                }else if(binding.etInstallments.visibility == View.VISIBLE){
                    if(verifyFields(binding.etPrice, binding.etDescription, binding.actvCategory, binding.etDate)){
                        val day = binding.etDate.text.toString().substring(0, 2)
                        val month = binding.etDate.text.toString().substring(3, 5)
                        val year = binding.etDate.text.toString().substring(6, 10)
                        val modifiedDate = "$year-$month-$day"

                        val regex = Regex("[\\d,.]+")
                        val justNumber = regex.find(binding.etPrice.text.toString())
                        val formatNum = DecimalFormat("#.#####")
                        val numClean = justNumber!!.value.replace(",","").replace(".","").toFloat()/binding.etInstallments.text.toString().toInt()
                        val formatedNum = formatNum.format(numClean/100)
                        val formattedNumString = formatedNum.toString().replace(",",".")

                        val existsDefaultBudget = viewModel.checkIfExistDefaultBudget().await()
                        if (existsDefaultBudget) {
                            viewModel.addInstallmentsExpense(
                                formattedNumString,
                                binding.etDescription.text.toString(),
                                binding.actvCategory.text.toString(),
                                modifiedDate,
                                binding.etInstallments.text.toString().toInt()
                            )
                            binding.etPrice.setText("")
                            binding.etDescription.setText("")
                            binding.actvCategory.setText("")
                            binding.etInstallments.setText("")
                        } else {
                            if(setUpDefaultBudgetAlertDialog().await()){
                                viewModel.addInstallmentsExpense(
                                    formattedNumString,
                                    binding.etDescription.text.toString(),
                                    binding.actvCategory.text.toString(),
                                    modifiedDate,
                                    binding.etInstallments.text.toString().toInt()
                                )
                                binding.etPrice.setText("")
                                binding.etDescription.setText("")
                                binding.actvCategory.setText("")
                                binding.etInstallments.setText("")
                            }
                        }
                    }
                }
            }
            binding.btSave.isEnabled = true
        }

        sharedViewModel.price.observe(viewLifecycleOwner) { price ->
            binding.btSave.visibility = View.VISIBLE
            binding.etPrice.setText(price)
        }

        sharedViewModel.description.observe(viewLifecycleOwner) { desc ->
            binding.etDescription.setText(desc)
        }

        sharedViewModel.category.observe(viewLifecycleOwner) { cat ->
            binding.actvCategory.setText(cat)
        }

        binding.actvCategory.setOnClickListener {
            binding.actvCategory.showDropDown()
            binding.dpDateExpense.visibility = View.GONE
        }

        binding.ivDate.setOnClickListener{
            binding.fragSetBudget.visibility = View.GONE
            binding.btSave.visibility = View.VISIBLE
            binding.dpDateExpense.visibility = View.VISIBLE
            binding.dpDateExpense.setOnDateChangedListener { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
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
                    val formatted = (NumberFormat.getCurrencyInstance().format(parsed / 100.0))
                    binding.etPrice.removeTextChangedListener(this)
                    binding.etPrice.setText(formatted)
                    binding.etPrice.setSelection(formatted.length)
                    binding.etPrice.addTextChangedListener(this)
                }
            }
        })

    }

    private fun actvConfig() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categoryOptions)
        binding.actvCategory.setAdapter(adapter)
    }

    private fun verifyFields(vararg text: EditText) : Boolean{
        for (i in text){
            if (i.text.toString() == "" || i == null){
                Snackbar.make(binding.btSave,"Preencher o campo ${i.hint}", Snackbar.LENGTH_LONG).show()
                return false
            }
        }
        return true
    }

    override fun onSaveButtonFragmentClick() {
        binding.btSave.performClick()
    }

    private fun setUpDefaultBudgetAlertDialog() : CompletableDeferred<Boolean> {
        val result = CompletableDeferred<Boolean>()
        val builder = AlertDialog.Builder(requireContext())

        builder.setTitle("Definir o budget padrão")

        val defaultBudget = EditText(requireContext())
        defaultBudget.hint = "Budget Padrão"
        defaultBudget.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
        builder.setView(defaultBudget)

        defaultBudget.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                if (!text.isEmpty()) {
                    val parsed = text.replace("[^\\d]".toRegex(), "").toLong()
                    val formatted = (NumberFormat.getCurrencyInstance().format(parsed / 100.0))
                    defaultBudget.removeTextChangedListener(this)
                    defaultBudget.setText(formatted)
                    defaultBudget.setSelection(formatted.length)
                    defaultBudget.addTextChangedListener(this)
                }
            }
        })

        builder.setPositiveButton("Salvar") { dialog, which ->
            val saveButton =  (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            saveButton.isEnabled = false
            lifecycleScope.launch {
                if(defaultBudget.text.toString() != ""){

                    val regex = Regex("[\\d,.]+")
                    val justNumber = regex.find(defaultBudget.text.toString())
                    val formatNum = DecimalFormat("#.##")
                    val numClean = justNumber!!.value.replace(",","").replace(".","").toFloat()
                    val formatedNum = formatNum.format(numClean/100)
                    val formattedNumString = formatedNum.toString().replace(",",".")

                    if(viewModel.setDefaultBudget(formattedNumString).await()){
                        val rootView: View? = activity?.findViewById(android.R.id.content)
                        if (rootView != null) {
                            val snackbar = Snackbar.make(rootView, "Budget editado com sucesso", Snackbar.LENGTH_LONG)
                            snackbar.show()
                            result.complete(true)
                        }
                    }else{
                        val rootView: View? = activity?.findViewById(android.R.id.content)
                        if (rootView != null) {
                            val snackbar = Snackbar.make(rootView, "Falha ao editar o Default", Snackbar.LENGTH_LONG)
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
                source: CharSequence?,
                start: Int,
                end: Int,
                dest: Spanned?,
                dstart: Int,
                dend: Int
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

    private fun setColorBasedOnTheme(){
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

    private fun performFileSearch() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/vnd.ms-excel" // Define o tipo MIME para arquivos Excel
        }

        startActivityForResult(intent, READ_REQUEST_CODE)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            resultData?.data?.also { uri ->
                val inputStream = requireContext().contentResolver.openInputStream(uri)

                if (inputStream != null) {
                    val outputStream = FileOutputStream(getNewFileUri().path) // Substitua getNewFileUri() pelo método que você usa para obter a URI do novo arquivo.

                    inputStream.use { input ->
                        outputStream.use { output ->
                            input.copyTo(output)
                        }
                    }
                    // Agora você pode trabalhar com o novo arquivo (cópia) no código.
                    val newPath = getNewFileUri().path.toString()
                    var readFileResult  = readFromExcelFile(newPath)
                    if(readFileResult.second){
                        lifecycleScope.launch(Dispatchers.Main){

                            val serviceIntent = Intent(requireContext(), UploadFile(readFileResult.first)::class.java)
                            requireContext().startService(serviceIntent)

                            /*for (expense in readFileResult.first){
                                val dateToCheck = expense.date.substring(0,7)
                                val existDate = viewModel.checkIfExistsDateOnDatabse(dateToCheck)
                                if(existDate.await()){
                                    viewModel.addExpense(expense.price,
                                        expense.description,
                                        expense.category,
                                        expense.date)
                                    delay(100)
                                }else{
                                    viewModel.setUpBudget(
                                        viewModel.getDefaultBudget(formatted = false).await(),
                                        dateToCheck)
                                    viewModel.addExpense(
                                        expense.price,
                                        expense.description,
                                        expense.category,
                                        expense.date)
                                    delay(100)
                                }
                            }*/
                        }
                        Toast.makeText(
                            requireContext(),
                            "Dados importados com sucesso !",
                            Toast.LENGTH_LONG).show()
                    }else{
                        Toast.makeText(
                            requireContext(),
                            "Falha ao importar os dados, " +
                                    "verifique se os dados estão corretos !",
                            Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    fun readFromExcelFile(filepath: String) : Pair<MutableList<Expense>,Boolean>{
        val inputStream = FileInputStream(filepath)
        //Instantiate Excel workbook using existing file:
        var xlWb = WorkbookFactory.create(inputStream)
        //Get reference to first sheet:
        val xlWs = xlWb.getSheetAt(0)

        var price = ""
        var description = ""
        var category = ""
        var date = ""

        var result = true

        val expenseList = mutableListOf<Expense>()

        val numberOfRows = xlWs.lastRowNum

        for (rowIndex in xlWs.firstRowNum+1..numberOfRows) {
            val row = xlWs.getRow(rowIndex) ?: continue

            val numberOfColumns = row.lastCellNum

            // Iterando pelas colunas dentro da linha atual
            for (columnIndex in row.firstCellNum until numberOfColumns) {
                val cell = row.getCell(columnIndex)
                val cellValue = getCellValueAsString(cell)
                when (columnIndex) {
                    0 -> {
                        price = cellValue.replace("R$","")
                        price = cellValue.replace("R$ ","")
                        price = cellValue.replace("$","")
                        price = cellValue.replace("$ ","")
                        price = cellValue.replace(",",".")
                        if(price == "xxx" || price == "XXX"){
                            return Pair(expenseList, result)
                        }
                        else if(price.toDoubleOrNull() == null){
                            result = false
                            expenseList.clear()
                            return Pair(expenseList, result)
                        }
                    }
                    1 -> {
                        description = cellValue.replace("  "," ")
                        description = cellValue.replace("  "," ")
                    }
                    2 -> {
                        category = cellValue
                    }
                    3 -> {
                        if(cell.cellType == CellType.STRING){
                            date = cellValue
                        }else if(cell.cellType == CellType.NUMERIC){
                            val dateDouble = cell.numericCellValue
                            val dateformat = SimpleDateFormat("dd/MM/yyyy")
                            date = dateformat.format(DateUtil.getJavaDate(dateDouble))
                        }

                        if(verifyDateFormat(date)){
                            val day = date.substring(0, 2)
                            val month = date.substring(3, 5)
                            val year = date.substring(6, 10)
                            date = "$year-$month-$day"
                        }else{
                            result = false
                            expenseList.clear()
                            return Pair(expenseList, result)
                        }
                    }
                }

            }
            val expense = Expense("", price, description, category, date)
            expenseList.add(expense)
        }
        xlWb.close()
        inputStream.close()

        return Pair(expenseList, result)
    }

    private fun verifyDateFormat(date: String) : Boolean{
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
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val newFile = File(downloadsDir, "expenses.xlsx")
        return Uri.fromFile(newFile)
    }

    private fun requestPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            try {
                Log.d(TAG, "requestPermission: try")
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                val uri = Uri.fromParts("package", requireActivity().packageName, null)
                intent.data = uri
                storageActivityResultLauncher.launch(intent)
            }catch (e : java.lang.Exception){
                Log.d(TAG, "RequestPermission: ", e)
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                storageActivityResultLauncher.launch(intent)
            }
        }
        else{
            ActivityCompat.requestPermissions(requireActivity(), permissions,
                STORAGE_PERMISSION_CODE
            )
        }
    }

    private val storageActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        Log.d(TAG, "storageActivityResultLauncher: ")
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            if(Environment.isExternalStorageManager()){
                Log.d(TAG, "storageActivityResultLauncher: ")
                lifecycleScope.launch {
                    delay(500)
                }
            }else{
                Log.d(TAG, "storageActivityResultLauncher: ")
                Toast.makeText(requireContext(),"Manage External Storage Permission is denied ...",
                    Toast.LENGTH_LONG).show()
            }
        }
        else{

        }
    }

    private fun checkPermission() : Boolean{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            Environment.isExternalStorageManager()
        }else{
            val write = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val read = ContextCompat.checkSelfPermission(requireContext(),  android.Manifest.permission.READ_EXTERNAL_STORAGE)
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
            if(grantResults.isNotEmpty()){
                val write = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val read = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if(write && read){
                    Log.d(TAG, "onRequestPermissionResult: External Storage Permission granted")
                }else{
                    Log.d(TAG, "onRequestPermissionResult: External Storage Permission denied ...")
                    Toast.makeText(requireContext(),"Manage External Storage Permission is denied ...",
                        Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun importDataAlertDialog(){
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Importar Dados")
            .setMessage("Os dados devem estar no formato correto")
            .setNeutralButton("Ver Formato Correto") { dialog, which ->
                startActivity(Intent(requireContext(),ImportFileInstructionsActivity::class.java))
            }
            .setPositiveButton("Selecionar Arquivo") { dialog, which ->
                performFileSearch()
            }
            .show()
    }

}
