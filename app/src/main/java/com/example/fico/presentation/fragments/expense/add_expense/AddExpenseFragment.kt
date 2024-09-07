package com.example.fico.presentation.fragments.expense.add_expense

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
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.fico.R
import com.example.fico.databinding.FragmentAddExpenseBinding
import com.example.fico.service.UploadFile
import com.example.fico.shared.constants.StringConstants
import com.example.fico.presentation.activities.expense.ComonExpenseImportFileInstructionsActivity
import com.example.fico.presentation.activities.expense.InstallmentExpenseImportFileInstructionsActivity
import com.example.fico.presentation.viewmodel.AddExpenseViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.example.fico.DataStoreManager
import com.example.fico.api.FirebaseAPI
import com.example.fico.presentation.adapters.CategoryListAdapter
import com.example.fico.presentation.interfaces.OnCategorySelectedListener
import com.example.fico.shared.ConnectionFunctions
import com.google.android.material.datepicker.MaterialDatePicker
import java.util.*
import kotlin.collections.ArrayList
import org.koin.android.ext.android.inject
import com.example.fico.shared.DateFunctions
import com.example.fico.shared.constants.CategoriesList

class AddExpenseFragment : Fragment(), OnCategorySelectedListener {


    private val READ_COMON_EXPENSE_REQUEST_CODE: Int = 43
    private val READ_INSTALLMENT_EXPENSE_REQUEST_CODE: Int = 44
    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddExpenseViewModel by inject()
    private val permissionRequestCode = 123
    private val permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
    private lateinit var adapter: CategoryListAdapter
    @RequiresApi(Build.VERSION_CODES.O)
    private val currentDate = DateFunctions().getCurrentlyDate()
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Action when receive Broadcast
            if (intent?.action == StringConstants.UPLOAD_FILE_SERVICE.SUCCESS_UPLOAD) {
                // Show message to user
                Toast.makeText(
                    context, "Dados salvos com sucesso !!", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    private val firebaseAPI : FirebaseAPI by inject()
    private val dataStore : DataStoreManager by inject()

    private companion object {
        private const val STORAGE_PERMISSION_CODE = 100
        private const val TAG = "PERMISSION_TAG"
    }
    private lateinit var menu : Menu
    private val categoriesList : CategoriesList by inject()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddExpenseBinding.inflate(
            inflater, container, false
        )
        var rootView = binding.root

        setUpListeners()
        setColorBasedOnTheme()

        binding.etPaymentDate.inputType = InputType.TYPE_NULL

        setMaxLength(binding.etInstallments, 3)

        //Create category chooser
        adapter = CategoryListAdapter(categoriesList.getExpenseCategoryList().sortedBy { it.description }, this)
        binding.rvCategory.adapter = adapter

        return rootView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()

        viewModel.getDefaultPaymentDay()

        binding.etPurchaseDate.setText(currentDate)

        val filter = IntentFilter().apply {
            addAction(StringConstants.UPLOAD_FILE_SERVICE.SUCCESS_UPLOAD)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            requireContext().registerReceiver(receiver, filter)
        }

    }

    override fun onPause() {
        super.onPause()
        requireContext().unregisterReceiver(receiver)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_expense_menu, menu)
        //Save instance of menu
        this.menu = menu
        super.onCreateOptionsMenu(menu, inflater)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add_expense_menu_common -> {
                binding.tilInstallments.visibility = View.GONE
                binding.etPaymentDate.hint = getString(R.string.payment_date)
                clearUserInputs()
                return true
            }

            R.id.add_expense_menu_installments -> {
                binding.tilInstallments.visibility = View.VISIBLE
                binding.etPaymentDate.hint = getString(R.string.payment_date_field_installment_hint)
                clearUserInputs()
                return true
            }

            //TODO Change function and instructions to add expenses from file
            /*R.id.add_expense_menu_get_data_from_file -> {
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
            }*/

            R.id.add_earning_transaction_menu -> {
                changeComponentsToEarningState()
                return true
            }

            R.id.add_expense_transaction_menu -> {
                changeComponentsToExpenseState()
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
                if(viewModel.getOperation() == StringConstants.ADD_TRANSACTION.ADD_EXPENSE){
                    if (binding.tilInstallments.visibility == View.GONE) {
                        if (verifyFields(
                                binding.etPrice,
                                binding.etDescription,
                                binding.actvCategory,
                                binding.etPaymentDate,
                                binding.etPurchaseDate
                            )
                        ) {
                            val internetConnection =
                                ConnectionFunctions().internetConnectionVerification(requireContext())
                            if (internetConnection) {
                                val existsDefaultBudget = viewModel.checkIfExistDefaultBudget().await()
                                if (existsDefaultBudget) {
                                    viewModel.addExpense(
                                        binding.etPrice.text.toString(),
                                        binding.etDescription.text.toString(),
                                        binding.actvCategory.text.toString(),
                                        binding.etPaymentDate.text.toString(),
                                        binding.etPurchaseDate.text.toString(),
                                        false
                                    )
                                } else {
                                    if (setUpDefaultBudgetAlertDialog().await()) {
                                        viewModel.addExpense(
                                            binding.etPrice.text.toString(),
                                            binding.etDescription.text.toString(),
                                            binding.actvCategory.text.toString(),
                                            binding.etPaymentDate.text.toString(),
                                            binding.etPurchaseDate.text.toString(),
                                            false
                                        )
                                    }
                                }
                            }/*else{
                            viewModel.addExpenseLocal(
                                binding.etPrice.text.toString(),
                                binding.etDescription.text.toString(),
                                binding.actvCategory.text.toString(),
                                binding.etDate.text.toString(),
                                false
                            )
                        }*/
                        }
                    } else if (binding.tilInstallments.visibility == View.VISIBLE) {
                        if (verifyFields(
                                binding.etPrice,
                                binding.etDescription,
                                binding.actvCategory,
                                binding.etPaymentDate,
                                binding.etPurchaseDate
                            )
                        ) {
                            if (binding.etInstallments.text.toString() != "0") {
                                val existsDefaultBudget = viewModel.checkIfExistDefaultBudget().await()
                                if (existsDefaultBudget) {
                                    viewModel.addExpense(
                                        binding.etPrice.text.toString(),
                                        binding.etDescription.text.toString(),
                                        binding.actvCategory.text.toString(),
                                        binding.etPaymentDate.text.toString(),
                                        binding.etPurchaseDate.text.toString(),
                                        true,
                                        binding.etInstallments.text.toString().toInt()
                                    )
                                } else {
                                    if (setUpDefaultBudgetAlertDialog().await()) {
                                        viewModel.addExpense(
                                            binding.etPrice.text.toString(),
                                            binding.etDescription.text.toString(),
                                            binding.actvCategory.text.toString(),
                                            binding.etPaymentDate.text.toString(),
                                            binding.etPurchaseDate.text.toString(),
                                            true,
                                            binding.etInstallments.text.toString().toInt()
                                        )
                                    }
                                }
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "O número de parcelas não pode ser 0",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
            }
            binding.btSave.isEnabled = true
        }

        binding.actvCategory.setOnClickListener {
            binding.actvCategory.showDropDown()
        }

        binding.ivPaymentDate.setOnClickListener {
            binding.btSave.visibility = View.VISIBLE
            binding.ivPaymentDate.isEnabled = false

            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Escolha a Data")
                .build()

            datePicker.addOnPositiveButtonClickListener {
                val selectedDateInMillis = it
                val formattedDate = formatDate(selectedDateInMillis)
                binding.etPaymentDate.setText(formattedDate)
                binding.ivPaymentDate.isEnabled = true
            }

            datePicker.addOnNegativeButtonClickListener {
                binding.ivPaymentDate.isEnabled = true
            }

            datePicker.addOnDismissListener {
                binding.ivPaymentDate.isEnabled = true
            }

            datePicker.show(parentFragmentManager, "PaymentDate")
        }

        binding.ivPurchaseDate.setOnClickListener {
            binding.btSave.visibility = View.VISIBLE
            binding.ivPurchaseDate.isEnabled = false

            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Escolha a Data")
                .build()

            datePicker.addOnPositiveButtonClickListener {
                val selectedDateInMillis = it
                val formattedDate = formatDate(selectedDateInMillis)
                binding.etPurchaseDate.setText(formattedDate)
                binding.ivPurchaseDate.isEnabled = true
            }

            datePicker.addOnNegativeButtonClickListener {
                binding.ivPurchaseDate.isEnabled = true
            }

            datePicker.addOnDismissListener {
                binding.ivPurchaseDate.isEnabled = true
            }

            datePicker.show(parentFragmentManager, "PurchaseDate")
        }

        binding.ivReceivedDate.setOnClickListener {
            binding.btSave.visibility = View.VISIBLE
            binding.ivReceivedDate.isEnabled = false

            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Escolha a Data")
                .build()

            datePicker.addOnPositiveButtonClickListener {
                val selectedDateInMillis = it
                val formattedDate = formatDate(selectedDateInMillis)
                binding.etReceivedDate.setText(formattedDate)
                binding.ivReceivedDate.isEnabled = true
            }

            datePicker.addOnNegativeButtonClickListener {
                binding.ivReceivedDate.isEnabled = true
            }

            datePicker.addOnDismissListener {
                binding.ivReceivedDate.isEnabled = true
            }

            datePicker.show(parentFragmentManager, "PurchaseDate")
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

        viewModel.paymentDayLiveData.observe(viewLifecycleOwner) { paymentDay ->
            if(paymentDay != null){
                val paymentDate = DateFunctions().paymentDate(paymentDay)
                binding.etPaymentDate.setText(paymentDate)
            }
        }

        binding.ivArrowUpGetPurchaseDate.setOnClickListener {
            binding.etPaymentDate.text = binding.etPurchaseDate.text
        }

        viewModel.addExpenseResult.observe(viewLifecycleOwner) { result ->
            hideKeyboard(requireContext(), binding.btSave)
            clearUserInputs()
            if (result) {
                Snackbar.make(
                    binding.rvCategory,
                    "Gasto adicionado com sucesso",
                    Snackbar.LENGTH_LONG
                ).show()
            } else {
                Snackbar.make(binding.rvCategory, "Falha ao adicionar gasto", Snackbar.LENGTH_LONG)
                    .show()
            }
        }

        viewModel.setDefaultBudgetResult.observe(requireActivity()){ result ->
            if(result){
                Snackbar.make(binding.btSave, getString(R.string.change_default_budget_success_message),Snackbar.LENGTH_LONG).show()
            }
            else{
                Snackbar.make(binding.btSave, getString(R.string.change_default_budget_failure_message),Snackbar.LENGTH_LONG).show()
            }
        }

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

                    viewModel.setDefaultBudget(formattedNumString)

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

    fun importDataAlertDialog() {
        MaterialAlertDialogBuilder(requireContext()).setTitle("Importar Gastos")
            .setMessage("Selecione o tipo de gasto que deseja importar.")
            .setNeutralButton("Gastos Parcelados") { dialog, which ->
                importInstallmentExpenseTypeAlertDialog()
            }.setPositiveButton("Gastos Comuns") { dialog, which ->
                importComonExpenseTypeAlertDialog()
            }.show()
    }

    fun importComonExpenseTypeAlertDialog() {
        MaterialAlertDialogBuilder(requireContext()).setTitle("Importar Gastos Comuns")
            .setMessage("Os dados devem estar no formato correto.")
            .setNeutralButton("Ver Formato Correto") { dialog, which ->
                startActivity(
                    Intent(
                        requireContext(),
                        ComonExpenseImportFileInstructionsActivity::class.java
                    )
                )
            }.setPositiveButton("Selecionar Arquivo") { dialog, which ->
                performFileSearch(READ_COMON_EXPENSE_REQUEST_CODE)
            }.show()
    }

    fun importInstallmentExpenseTypeAlertDialog() {
        MaterialAlertDialogBuilder(requireContext()).setTitle("Importar Gastos Parcelados")
            .setMessage("Os dados devem estar no formato correto.")
            .setNeutralButton("Ver Formato Correto") { dialog, which ->
                startActivity(
                    Intent(
                        requireContext(),
                        InstallmentExpenseImportFileInstructionsActivity::class.java
                    )
                )
            }.setPositiveButton("Selecionar Arquivo") { dialog, which ->
                performFileSearch(READ_INSTALLMENT_EXPENSE_REQUEST_CODE)
            }.show()
    }

    private fun performFileSearch(requestCode: Int) {
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
                    val outputStream =
                        FileOutputStream(AddExpenseImportDataFromFile().getNewFileUri().path)

                    inputStream.use { input ->
                        outputStream.use { output ->
                            input.copyTo(output)
                        }
                    }
                    // Work with a copy of file
                    val newPath = AddExpenseImportDataFromFile().getNewFileUri().path.toString()
                    var readFileResult = AddExpenseImportDataFromFile().readFromExcelFile(newPath)
                    if (readFileResult.second) {
                        lifecycleScope.launch(Dispatchers.Main) {

                            val expensesList = readFileResult.first

                            // innit the upload data to database service
                            val serviceIntent = Intent(requireContext(), UploadFile(firebaseAPI, dataStore)::class.java)
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
        } else if (requestCode == READ_INSTALLMENT_EXPENSE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            resultData?.data?.also { uri ->
                val inputStream = requireContext().contentResolver.openInputStream(uri)

                if (inputStream != null) {
                    val outputStream =
                        FileOutputStream(AddExpenseImportDataFromFile().getNewFileUri().path)

                    inputStream.use { input ->
                        outputStream.use { output ->
                            input.copyTo(output)
                        }
                    }
                    // Work with a copy of file
                    val newPath = AddExpenseImportDataFromFile().getNewFileUri().path.toString()
                    var readFileResult = AddExpenseImportDataFromFile().readFromExcelFile(newPath)
                    if (readFileResult.second) {
                        lifecycleScope.launch(Dispatchers.Main) {

                            val expensesList = readFileResult.first

                            // innit the upload data to database service
                            val serviceIntent = Intent(requireContext(), UploadFile(firebaseAPI, dataStore)::class.java)
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

    private fun setColorBasedOnTheme() {
        when (context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.ivPaymentDate.setImageResource(R.drawable.baseline_calendar_month_light)
                binding.ivPurchaseDate.setImageResource(R.drawable.baseline_calendar_month_light)
                binding.ivArrowUpGetPurchaseDate.setImageResource(R.drawable.arrow_up_light)
                binding.ivReceivedDate.setImageResource(R.drawable.baseline_calendar_month_light)

            }

            Configuration.UI_MODE_NIGHT_NO -> {
                binding.ivPaymentDate.setImageResource(R.drawable.baseline_calendar_month_dark)
                binding.ivPurchaseDate.setImageResource(R.drawable.baseline_calendar_month_dark)
                binding.ivArrowUpGetPurchaseDate.setImageResource(R.drawable.arrow_up_black)
                binding.ivReceivedDate.setImageResource(R.drawable.baseline_calendar_month_dark)
            }

            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }
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

    private fun hideKeyboard(context: Context, view: View) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("GMT")
        val date = Date(timestamp)
        return sdf.format(date)
    }

    override fun onCategorySelected(description: String) {
        binding.actvCategory.setText(description)
    }

    private fun clearUserInputs() {
        binding.etPrice.setText("")
        binding.etDescription.setText("")
        binding.actvCategory.setText("")
        binding.etInstallments.setText("")
        adapter.clearCategorySelection()
    }

    private fun changeComponentsToEarningState(){
        menu.findItem(R.id.add_earning_transaction_menu).isVisible = false
        menu.findItem(R.id.add_expense_menu_common).isVisible = false
        menu.findItem(R.id.add_expense_menu_installments).isVisible = false
        menu.findItem(R.id.add_expense_transaction_menu).isVisible = true
        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.add_income)
        viewModel.changeOperation(StringConstants.ADD_TRANSACTION.ADD_EARNING)
        binding.tilPurchaseDate.visibility = View.GONE
        binding.etPurchaseDate.visibility = View.GONE
        binding.ivPurchaseDate.visibility = View.GONE
        binding.ivArrowUpGetPurchaseDate.visibility = View.GONE
        binding.tilPaymentDate.visibility = View.GONE
        binding.etPaymentDate.visibility = View.GONE
        binding.ivPaymentDate.visibility = View.GONE
        binding.tilReceivedDate.visibility = View.VISIBLE
        binding.etReceivedDate.visibility = View.VISIBLE
        binding.ivReceivedDate.visibility = View.VISIBLE
        binding.tilInstallments.visibility = View.VISIBLE
        binding.etInstallments.visibility = View.VISIBLE
        adapter.updateCategories(categoriesList.getEarningCategoryList().sortedBy { it.description })
    }

    private fun changeComponentsToExpenseState(){
        menu.findItem(R.id.add_earning_transaction_menu).isVisible = true
        menu.findItem(R.id.add_expense_menu_common).isVisible = true
        menu.findItem(R.id.add_expense_menu_installments).isVisible = true
        menu.findItem(R.id.add_expense_transaction_menu).isVisible = false
        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.add_expense_title)
        viewModel.changeOperation(StringConstants.ADD_TRANSACTION.ADD_EXPENSE)
        binding.tilPurchaseDate.visibility = View.VISIBLE
        binding.etPurchaseDate.visibility = View.VISIBLE
        binding.ivPurchaseDate.visibility = View.VISIBLE
        binding.ivArrowUpGetPurchaseDate.visibility = View.VISIBLE
        binding.tilPaymentDate.visibility = View.VISIBLE
        binding.etPaymentDate.visibility = View.VISIBLE
        binding.ivPaymentDate.visibility = View.VISIBLE
        binding.tilReceivedDate.visibility = View.GONE
        binding.etReceivedDate.visibility = View.GONE
        binding.ivReceivedDate.visibility = View.GONE
        adapter.updateCategories(categoriesList.getExpenseCategoryList().sortedBy { it.description })
    }

}