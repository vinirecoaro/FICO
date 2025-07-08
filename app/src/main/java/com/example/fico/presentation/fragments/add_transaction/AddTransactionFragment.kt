package com.example.fico.presentation.fragments.add_transaction

import android.Manifest
import android.app.Activity
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
import com.example.fico.service.UploadFile
import com.example.fico.utils.constants.StringConstants
import com.example.fico.presentation.activities.ComonExpenseImportFileInstructionsActivity
import com.example.fico.presentation.activities.InstallmentExpenseImportFileInstructionsActivity
import com.example.fico.presentation.viewmodel.AddTransactionViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
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
import com.example.fico.presentation.components.dialogs.Dialogs
import com.example.fico.presentation.components.PersonalizedSnackBars
import com.example.fico.databinding.FragmentAddTransactionBinding
import com.example.fico.model.RecurringTransaction
import com.example.fico.model.Transaction
import com.example.fico.presentation.adapters.CategoryListAdapter
import com.example.fico.interfaces.OnCategorySelectedListener
import com.example.fico.model.CreditCard
import com.example.fico.utils.internet.ConnectionFunctions
import java.util.*
import org.koin.android.ext.android.inject
import com.example.fico.utils.DateFunctions
import com.example.fico.utils.constants.CategoriesList
import com.example.fico.presentation.components.inputs.InputFieldFunctions
import com.example.fico.presentation.compose.components.ComposeDialogs
import java.math.BigDecimal
import java.math.RoundingMode

class AddTransactionFragment : Fragment(), OnCategorySelectedListener {

    private val READ_COMON_EXPENSE_REQUEST_CODE: Int = 43
    private val READ_INSTALLMENT_EXPENSE_REQUEST_CODE: Int = 44
    private var _binding: FragmentAddTransactionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddTransactionViewModel by inject()
    private val permissionRequestCode = 123
    private val permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
    private lateinit var adapter: CategoryListAdapter
    @RequiresApi(Build.VERSION_CODES.O)
    private val currentDate = DateFunctions().getCurrentDate()
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Action when receive Broadcast
            if (intent?.action == StringConstants.UPLOAD_FILE_SERVICE.SUCCESS_UPLOAD) {
                // Show message to user
                Toast.makeText(
                    context, getString(R.string.save_data_success_message), Toast.LENGTH_SHORT
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
        _binding = FragmentAddTransactionBinding.inflate(
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

        viewModel.getPayWithCreditCardSwitchState()

        binding.etPurchaseDate.setText(currentDate)
        binding.etReceivedDate.setText(currentDate)

        val filter = IntentFilter().apply {
            addAction(StringConstants.UPLOAD_FILE_SERVICE.SUCCESS_UPLOAD)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            requireContext().registerReceiver(receiver, filter)
        }

        val recurringExpenseState = arguments?.getBoolean(StringConstants.ADD_TRANSACTION.ADD_RECURRING_EXPENSE) ?: false
        val recurringEarningState = arguments?.getBoolean(StringConstants.ADD_TRANSACTION.ADD_RECURRING_EARNING) ?: false
        if(recurringExpenseState){
            viewModel.updateRecurringMode(StringConstants.ADD_TRANSACTION.ADD_RECURRING_EXPENSE)
            arguments = null
        }
        else if(recurringEarningState){
            viewModel.updateRecurringMode(StringConstants.ADD_TRANSACTION.ADD_RECURRING_EARNING)
            arguments = null
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
        inflater.inflate(R.menu.add_transaction_menu, menu)
        //Save instance of menu
        this.menu = menu
        super.onCreateOptionsMenu(menu, inflater)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.add_recurring_expense_menu -> {
                viewModel.getRecurringTransactionList(StringConstants.DATABASE.RECURRING_EXPENSE)
                return true
            }

            R.id.add_recurring_earning_menu -> {
                viewModel.getRecurringTransactionList(StringConstants.DATABASE.RECURRING_EARNING)
                return true
            }

            R.id.add_transaction_menu_get_data_from_file -> {
                lifecycleScope.launch {
                    if(hasInternetConnection()){
                        if (checkPermission()) {
                            if (viewModel.checkIfExistDefaultBudget().await()) {
                                importDataAlertDialog()
                            } else {
                                setUpDefaultBudgetAlertDialog()
                            }
                        } else {
                            requestPermission()
                        }
                    }else{
                        PersonalizedSnackBars.noInternetConnection(binding.btSave, requireActivity()).show()
                    }
                }
                return true
            }

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
                        if(binding.swtPayWithCreditCard.isChecked){
                            if (InputFieldFunctions.isFilled(requireActivity(), binding.root,
                                    binding.etPrice,
                                    binding.etDescription,
                                    binding.actvCategory,
                                    binding.etPaymentDate,
                                    binding.etPurchaseDate
                                )
                            ) {
                                if (hasInternetConnection()) {
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
                                        setUpDefaultBudgetAlertDialog()
                                    }
                                }else{
                                    PersonalizedSnackBars.noInternetConnection(binding.btSave, requireActivity()).show()
                                }
                            }
                        }else{
                            if (InputFieldFunctions.isFilled(requireActivity(), binding.root,
                                    binding.etPrice,
                                    binding.etDescription,
                                    binding.actvCategory,
                                    binding.etPurchaseDate
                                )
                            ) {
                                if (hasInternetConnection()) {
                                    val existsDefaultBudget = viewModel.checkIfExistDefaultBudget().await()
                                    if (existsDefaultBudget) {
                                        viewModel.addExpense(
                                            price = binding.etPrice.text.toString(),
                                            description = binding.etDescription.text.toString(),
                                            category = binding.actvCategory.text.toString(),
                                            paymentDate = binding.etPurchaseDate.text.toString(),
                                            purchaseDate = binding.etPurchaseDate.text.toString(),
                                            installment = false
                                        )
                                    } else {
                                        setUpDefaultBudgetAlertDialog()
                                    }
                                }else{
                                    PersonalizedSnackBars.noInternetConnection(binding.btSave, requireActivity()).show()
                                }
                            }
                        }

                    } else if (binding.tilInstallments.visibility == View.VISIBLE) {
                        if(binding.swtPayWithCreditCard.isChecked){
                            if (InputFieldFunctions.isFilled(requireActivity(), binding.root,
                                    binding.etPrice,
                                    binding.etDescription,
                                    binding.actvCategory,
                                    binding.etPaymentDate,
                                    binding.etPurchaseDate
                                )
                            ) {
                                if(hasInternetConnection()){
                                    if (binding.etInstallments.text.toString().toInt() > 1) {
                                        val existsDefaultBudget = viewModel.checkIfExistDefaultBudget().await()
                                        if (existsDefaultBudget) {
                                            viewModel.addExpense(
                                                price = binding.etPrice.text.toString(),
                                                description =  binding.etDescription.text.toString(),
                                                category =  binding.actvCategory.text.toString(),
                                                paymentDate =  binding.etPaymentDate.text.toString(),
                                                purchaseDate =  binding.etPurchaseDate.text.toString(),
                                                installment = true,
                                                nOfInstallments =  binding.etInstallments.text.toString().toInt()
                                            )
                                        } else {
                                            setUpDefaultBudgetAlertDialog()
                                        }
                                    } else {
                                        Toast.makeText(
                                            requireContext(),
                                            getString(R.string.wrong_installment_input_message),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }else{
                                    PersonalizedSnackBars.noInternetConnection(binding.btSave, requireActivity()).show()
                                }
                            }
                        }else{
                            if (InputFieldFunctions.isFilled(requireActivity(), binding.root,
                                    binding.etPrice,
                                    binding.etDescription,
                                    binding.actvCategory,
                                    binding.etPurchaseDate
                                )
                            ) {
                                if(hasInternetConnection()){
                                    if (binding.etInstallments.text.toString().toInt() > 1) {
                                        val existsDefaultBudget = viewModel.checkIfExistDefaultBudget().await()
                                        if (existsDefaultBudget) {
                                            viewModel.addExpense(
                                                price = binding.etPrice.text.toString(),
                                                description =  binding.etDescription.text.toString(),
                                                category =  binding.actvCategory.text.toString(),
                                                paymentDate =  binding.etPurchaseDate.text.toString(),
                                                purchaseDate =  binding.etPurchaseDate.text.toString(),
                                                installment = true,
                                                nOfInstallments =  binding.etInstallments.text.toString().toInt()
                                            )
                                        } else {
                                            setUpDefaultBudgetAlertDialog()
                                        }
                                    } else {
                                        Toast.makeText(
                                            requireContext(),
                                            getString(R.string.wrong_installment_input_message),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }else{
                                    PersonalizedSnackBars.noInternetConnection(binding.btSave, requireActivity()).show()
                                }
                            }
                        }
                    }
                }
                else if(viewModel.getOperation() == StringConstants.ADD_TRANSACTION.ADD_EARNING){
                    if (InputFieldFunctions.isFilled(requireActivity(), binding.root,
                            binding.etPrice,
                            binding.etDescription,
                            binding.actvCategory,
                            binding.etReceivedDate
                        )
                    ){
                        if(hasInternetConnection()){
                            viewModel.addEarning(
                                binding.etPrice.text.toString(),
                                binding.etDescription.text.toString(),
                                binding.actvCategory.text.toString(),
                                binding.etReceivedDate.text.toString()
                            )
                        }else{
                            PersonalizedSnackBars.noInternetConnection(binding.btSave, requireActivity()).show()
                        }
                    }
                }
                else if (viewModel.getOperation() == StringConstants.ADD_TRANSACTION.ADD_RECURRING_EXPENSE){
                    if (InputFieldFunctions.isFilled(requireActivity(), binding.root,
                            binding.etPrice,
                            binding.etDescription,
                            binding.actvCategory,
                        )
                    ){
                        if(hasInternetConnection()){
                            if(binding.etRecurringTransactionDay.text != null && binding.etRecurringTransactionDay.text.toString() != ""){
                                if(DateFunctions().isValidMonthDay(binding.etRecurringTransactionDay.text.toString().toInt())){
                                    viewModel.addRecurringTransaction(
                                        binding.etPrice.text.toString(),
                                        binding.etDescription.text.toString(),
                                        binding.actvCategory.text.toString(),
                                        binding.etRecurringTransactionDay.text.toString(),
                                        StringConstants.DATABASE.RECURRING_EXPENSE
                                    )
                                }else{
                                    Snackbar.make(binding.etRecurringTransactionDay, getString(R.string.invalid_day), Snackbar.LENGTH_LONG).show()
                                }
                            }else{
                                viewModel.addRecurringTransaction(
                                    binding.etPrice.text.toString(),
                                    binding.etDescription.text.toString(),
                                    binding.actvCategory.text.toString(),
                                    "",
                                    StringConstants.DATABASE.RECURRING_EXPENSE
                                )
                            }
                        }else{
                            PersonalizedSnackBars.noInternetConnection(binding.btSave, requireActivity()).show()
                        }
                    }
                }
                else if (viewModel.getOperation() == StringConstants.ADD_TRANSACTION.ADD_RECURRING_EARNING){
                    if (InputFieldFunctions.isFilled(requireActivity(), binding.root,
                            binding.etPrice,
                            binding.etDescription,
                            binding.actvCategory,
                        )
                    ){
                        if(hasInternetConnection()){
                            if(binding.etRecurringTransactionDay.text != null && binding.etRecurringTransactionDay.text.toString() != ""){
                                if(DateFunctions().isValidMonthDay(binding.etRecurringTransactionDay.text.toString().toInt())){
                                    viewModel.addRecurringTransaction(
                                        binding.etPrice.text.toString(),
                                        binding.etDescription.text.toString(),
                                        binding.actvCategory.text.toString(),
                                        binding.etRecurringTransactionDay.text.toString(),
                                        StringConstants.DATABASE.RECURRING_EARNING
                                    )
                                }else{
                                    Snackbar.make(binding.etRecurringTransactionDay, getString(R.string.invalid_day), Snackbar.LENGTH_LONG).show()
                                }
                            }else{
                                viewModel.addRecurringTransaction(
                                    binding.etPrice.text.toString(),
                                    binding.etDescription.text.toString(),
                                    binding.actvCategory.text.toString(),
                                    "",
                                    StringConstants.DATABASE.RECURRING_EARNING
                                )
                            }
                        }else{
                            PersonalizedSnackBars.noInternetConnection(binding.btSave, requireActivity()).show()
                        }
                    }
                }
            }
            binding.btSave.isEnabled = true
        }

        binding.actvCategory.setOnClickListener {
            binding.actvCategory.showDropDown()
        }

        binding.ivPurchaseDate.setOnClickListener {
            binding.btSave.visibility = View.VISIBLE
            binding.ivPurchaseDate.isEnabled = false

            val datePicker = Dialogs.datePicker(
                requireContext(),
                { dateInMillis -> onPurchaseDateChanged(dateInMillis, binding.etPurchaseDate) },
                { binding.ivPurchaseDate.isEnabled = true },
                { binding.ivPurchaseDate.isEnabled = true },
            )

            datePicker.show(parentFragmentManager, "PurchaseDate")
        }

        binding.ivReceivedDate.setOnClickListener {
            binding.btSave.visibility = View.VISIBLE
            binding.ivReceivedDate.isEnabled = false

            val datePicker = Dialogs.datePicker(
                requireContext(),
                { dateInMillis -> setSelectedDate(dateInMillis, binding.etReceivedDate) },
                { binding.ivReceivedDate.isEnabled = true },
                { binding.ivReceivedDate.isEnabled = true },
            )

            datePicker.show(parentFragmentManager, "PurchaseDate")
        }

        binding.tvPaymentDate.setOnClickListener {
            binding.btSave.visibility = View.VISIBLE
            binding.tvPaymentDate.isEnabled = false

            val datePicker = Dialogs.datePicker(
                requireContext(),
                { dateInMillis -> onPaymentDateChange(dateInMillis) },
                { binding.tvPaymentDate.isEnabled = true },
                { binding.tvPaymentDate.isEnabled = true },
            )

            datePicker.show(parentFragmentManager, "PaymentDate")
        }

        binding.etPrice.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                if (text.isNotEmpty()) {
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

        viewModel.addExpenseResult.observe(viewLifecycleOwner) { result ->
            hideKeyboard(requireContext(), binding.btSave)
            clearUserInputs()
            if (result) {
                PersonalizedSnackBars.successMessage(binding.root, getString(R.string.add_expense_success_message)).show()
            } else {
                PersonalizedSnackBars.failureMessage(binding.root, getString(R.string.add_expense_failure_message)).show()
            }
        }

        viewModel.setDefaultBudgetResult.observe(requireActivity()){ result ->
            if(result){
                PersonalizedSnackBars.successMessage(binding.root, getString(R.string.change_default_budget_success_message)).show()
            }
            else{
                PersonalizedSnackBars.failureMessage(binding.root, getString(R.string.change_default_budget_failure_message)).show()
            }
        }

        binding.swtPayWithCreditCard.setOnCheckedChangeListener { compoundButton, isChecked ->
            if(isChecked){
                viewModel.getCreditCardList()
            }else{
                hideCreditCardComponents()
            }
        }

        viewModel.payWithCreditCardSwitchInitialState.observe(requireActivity()){ state ->
            binding.swtPayWithCreditCard.isChecked = state
            if(state){
                viewModel.getCreditCardList()
            }else{
                hideCreditCardComponents()
            }
        }

        viewModel.addEarningResult.observe(viewLifecycleOwner){ result ->
            hideKeyboard(requireContext(), binding.btSave)
            clearUserInputs()
            if (result) {
                PersonalizedSnackBars.successMessage(binding.root, getString(R.string.add_earning_success_message)).show()
            } else {
                PersonalizedSnackBars.failureMessage(binding.root, getString(R.string.add_earning_failure_message)).show()
            }
        }

        viewModel.recurringMode.observe(viewLifecycleOwner){mode ->
            if(mode == StringConstants.ADD_TRANSACTION.ADD_RECURRING_EXPENSE){
                changeComponentsToRecurringExpenseState()
            }
            else if(mode == StringConstants.ADD_TRANSACTION.ADD_RECURRING_EARNING){
                changeComponentsToRecurringEarningState()
            }
        }

        viewModel.addRecurringTransactionResult.observe(viewLifecycleOwner){ result ->
            hideKeyboard(requireContext(), binding.btSave)
            clearUserInputs()
            if (result) {
                PersonalizedSnackBars.successMessage(binding.root, getString(R.string.add_recurring_transaction_success_message)).show()
            } else {
                PersonalizedSnackBars.failureMessage(binding.root, getString(R.string.add_recurring_transaction_failure_message)).show()
            }
        }

        viewModel.recurringTransactionsList.observe(viewLifecycleOwner){ recurringTransactionList ->
            val transactionList = mutableListOf<Transaction>()
            val list =  recurringTransactionList.first
            val type =  recurringTransactionList.second
            list.forEach { recurringExpense -> transactionList.add(recurringExpense.toTransaction()) }
            recurringTransactionListDialog(transactionList, type)
        }

        //Personalized box Stroke and Hint color
        InputFieldFunctions.defineStrokeColorOnFocused(
            requireActivity(),
            requireContext(),
            Pair(binding.tilPrice, binding.etPrice),
            Pair(binding.tilDescription, binding.etDescription),
            Pair(binding.tilInstallments, binding.etInstallments),
            Pair(binding.tilPurchaseDate, binding.etPurchaseDate)
        )

        binding.rgExpenseType.setOnCheckedChangeListener { group, checkedId  ->
            when(checkedId){
                R.id.rb_common_expense -> {
                    changeToCommonExpense()
                }
                R.id.rb_installment_expense -> {
                    changeToInstallmentExpense()
                }
            }
        }

        viewModel.getCreditCardList.observe(viewLifecycleOwner){creditCardList ->
            //Show credit card selection button
            binding.cvSelectCreditCard.visibility = View.VISIBLE

            // Verify if have default credit card
            lifecycleScope.launch {
                if(creditCardList != null){
                    val defaultCreditCardId = viewModel.getDefaultCreditCardId().await()
                    if(defaultCreditCardId != ""){
                        val defaultCreditCard = creditCardList.find { it.id == defaultCreditCardId }
                        if(defaultCreditCard != null){
                            viewModel.setSelectedCreditCard(defaultCreditCard)
                            showCreditCardPreview(defaultCreditCard, true)
                        }
                    }
                }
            }
        }

        binding.cvSelectCreditCard.setOnClickListener {
            lifecycleScope.launch {
                val creditCardList = viewModel.getCreditCardList.value ?: emptyList()
                val defaultCreditCardId = viewModel.getDefaultCreditCardId().await()
                ComposeDialogs.showCreditCardListDialog(
                    composeView = binding.composeDialogHost,
                    items = creditCardList,
                    defaultCreditCardId = defaultCreditCardId,
                    title = getString(R.string.cards),
                    paymentDayLabel = getString(R.string.payment_day)
                ) { selected ->
                    viewModel.setSelectedCreditCard(selected)
                    if(selected.id == defaultCreditCardId){
                        showCreditCardPreview(selected, true)
                    }else{
                        showCreditCardPreview(selected, false)
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onPurchaseDateChanged(dateInMillis : Long, editText : EditText){
        setSelectedDate(dateInMillis, editText)
        //Recalculate payment date
        val selectedCreditCard = viewModel.getSelectedCreditCard()
        if(selectedCreditCard != null){
            val paymentDate = DateFunctions().paymentDate(
                selectedCreditCard.expirationDay,
                selectedCreditCard.closingDay,
                binding.etPurchaseDate.text.toString()
            )
            binding.tvPaymentDate.text = paymentDate
            //Set payment day on textInput
            binding.etPaymentDate.setText(paymentDate)
        }
    }

    private fun setSelectedDate(dateInMillis : Long, editText : EditText){
        val formattedDate = formatDate(dateInMillis)
        editText.setText(formattedDate)
        editText.isEnabled = true
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setUpDefaultBudgetAlertDialog(){
        val dialog = Dialogs.dialogModelTwo(
            requireActivity(),
            requireContext(),
            binding.btSave,
            getString(R.string.define_default_budget_title),
            getString(R.string.default_budget_activity_title),
            StringConstants.PERSONALIZED_INPUT_TYPE.MONEY,
            getString(R.string.save)
        ){ newDefaultBudget -> setUpDefaultBudget(newDefaultBudget)}
        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setUpDefaultBudget(defaultBudget : String){
        if(hasInternetConnection()){
            lifecycleScope.launch {
                if (defaultBudget != "") {

                    val regex = Regex("[\\d,.]+")
                    val justNumber = regex.find(defaultBudget)
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
        }else{
            PersonalizedSnackBars.noInternetConnection(binding.btSave, requireActivity()).show()
        }

    }

    fun importDataAlertDialog() {
        MaterialAlertDialogBuilder(requireContext()).setTitle(getString(R.string.import_transactions))
            .setMessage(getString(R.string.import_transactions_dialog_message))
            .setNeutralButton(getString(R.string.instructions)) { dialog, which ->
                startActivity(
                    Intent(
                        requireContext(),
                        InstallmentExpenseImportFileInstructionsActivity::class.java
                    )
                )
            }
            .setPositiveButton(getString(R.string.select_file)) { dialog, which ->
                performFileSearch(READ_COMON_EXPENSE_REQUEST_CODE)
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
                        FileOutputStream(AddTransactionImportDataFromFile().getNewFileUri().path)

                    inputStream.use { input ->
                        outputStream.use { output ->
                            input.copyTo(output)
                        }
                    }
                    // Work with a copy of file
                    val newPath = AddTransactionImportDataFromFile().getNewFileUri().path.toString()
                    var readFileResult = AddTransactionImportDataFromFile().readFromExcelFile(newPath, requireContext())
                    if (readFileResult.result) {
                        lifecycleScope.launch(Dispatchers.Main) {

                            val expensesList = readFileResult.expenseList
                            val earningList = readFileResult.earningList
                            val installmentExpenseList = readFileResult.installmentExpenseList

                            // innit the upload data to database service
                            val serviceIntent = Intent(requireContext(), UploadFile()::class.java)

                            serviceIntent
                                .putParcelableArrayListExtra(StringConstants.XLS.EXPENSE_LIST, ArrayList(expensesList))
                                .putParcelableArrayListExtra(StringConstants.XLS.EARNING_LIST, ArrayList(earningList))
                                .putParcelableArrayListExtra(StringConstants.XLS.INSTALLMENT_EXPENSE_LIST, ArrayList(installmentExpenseList))
                            requireContext().startService(serviceIntent)
                        }
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.data_format_success_message),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.data_formart_failure_message),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    private fun setMaxLength(editText: EditText, maxLength: Int) {
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
                binding.ivReceivedDate.setImageResource(R.drawable.baseline_calendar_month_light)

            }

            Configuration.UI_MODE_NIGHT_NO -> {
                binding.ivPaymentDate.setImageResource(R.drawable.baseline_calendar_month_dark)
                binding.ivPurchaseDate.setImageResource(R.drawable.baseline_calendar_month_dark)
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
                    getString(R.string.external_storage_permission_is_denied),
                    Toast.LENGTH_LONG
                ).show()
            }
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
                        getString(R.string.external_storage_permission_is_denied),
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
        binding.etRecurringTransactionDay.setText("")
        adapter.clearCategorySelection()
    }

    private fun changeComponentsToEarningState(){
        menu.findItem(R.id.add_earning_transaction_menu).isVisible = false
        menu.findItem(R.id.add_recurring_expense_menu).isVisible = false
        menu.findItem(R.id.add_expense_transaction_menu).isVisible = true
        menu.findItem(R.id.add_recurring_earning_menu).isVisible = true
        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.add_income)
        viewModel.changeOperation(StringConstants.ADD_TRANSACTION.ADD_EARNING)
        binding.tilPurchaseDate.visibility = View.GONE
        binding.etPurchaseDate.visibility = View.GONE
        binding.ivPurchaseDate.visibility = View.GONE
        binding.cvCreditCardPreview.visibility = View.GONE
        binding.tilInstallments.visibility = View.GONE
        binding.etInstallments.visibility = View.GONE
        binding.tvSwtPayWithCreditCard.visibility = View.GONE
        binding.swtPayWithCreditCard.visibility = View.GONE
        binding.tilReceivedDate.visibility = View.VISIBLE
        binding.etReceivedDate.visibility = View.VISIBLE
        binding.ivReceivedDate.visibility = View.VISIBLE
        binding.tilRecurringTransactionDay.visibility = View.GONE
        binding.etRecurringTransactionDay.visibility = View.GONE
        binding.rgExpenseType.visibility = View.GONE
        binding.clCreditCard.visibility = View.GONE
        adapter.updateCategories(categoriesList.getEarningCategoryList().sortedBy { it.description })
    }

    private fun changeComponentsToExpenseState(){
        menu.findItem(R.id.add_earning_transaction_menu).isVisible = true
        menu.findItem(R.id.add_recurring_expense_menu).isVisible = true
        menu.findItem(R.id.add_expense_transaction_menu).isVisible = false
        menu.findItem(R.id.add_recurring_earning_menu).isVisible = false
        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.add_expense_title)
        viewModel.changeOperation(StringConstants.ADD_TRANSACTION.ADD_EXPENSE)
        binding.tilPurchaseDate.visibility = View.VISIBLE
        binding.etPurchaseDate.visibility = View.VISIBLE
        binding.ivPurchaseDate.visibility = View.VISIBLE
        binding.tvSwtPayWithCreditCard.visibility = View.VISIBLE
        binding.swtPayWithCreditCard.visibility = View.VISIBLE
        binding.tilReceivedDate.visibility = View.GONE
        binding.etReceivedDate.visibility = View.GONE
        binding.ivReceivedDate.visibility = View.GONE
        binding.tilRecurringTransactionDay.visibility = View.GONE
        binding.etRecurringTransactionDay.visibility = View.GONE
        binding.rgExpenseType.visibility = View.VISIBLE
        binding.clCreditCard.visibility = View.VISIBLE
        adapter.updateCategories(categoriesList.getExpenseCategoryList().sortedBy { it.description })
    }

    private fun changeComponentsToRecurringExpenseState(){
        menu.findItem(R.id.add_earning_transaction_menu).isVisible = false
        menu.findItem(R.id.add_recurring_expense_menu).isVisible = false
        menu.findItem(R.id.add_expense_transaction_menu).isVisible = false
        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.add_recurring_expense)
        viewModel.changeOperation(StringConstants.ADD_TRANSACTION.ADD_RECURRING_EXPENSE)
        binding.tilPurchaseDate.visibility = View.GONE
        binding.etPurchaseDate.visibility = View.GONE
        binding.ivPurchaseDate.visibility = View.GONE
        binding.cvCreditCardPreview.visibility = View.GONE
        binding.tilInstallments.visibility = View.GONE
        binding.etInstallments.visibility = View.GONE
        binding.tvSwtPayWithCreditCard.visibility = View.GONE
        binding.swtPayWithCreditCard.visibility = View.GONE
        binding.tilReceivedDate.visibility = View.GONE
        binding.etReceivedDate.visibility = View.GONE
        binding.ivReceivedDate.visibility = View.GONE
        binding.tilRecurringTransactionDay.visibility = View.VISIBLE
        binding.etRecurringTransactionDay.visibility = View.VISIBLE
        binding.rgExpenseType.visibility = View.GONE
        adapter.updateCategories(categoriesList.getExpenseCategoryList().sortedBy { it.description })
    }

    private fun changeComponentsToRecurringEarningState(){
        menu.findItem(R.id.add_earning_transaction_menu).isVisible = false
        menu.findItem(R.id.add_recurring_expense_menu).isVisible = false
        menu.findItem(R.id.add_expense_transaction_menu).isVisible = false
        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.add_recurring_earning)
        viewModel.changeOperation(StringConstants.ADD_TRANSACTION.ADD_RECURRING_EARNING)
        binding.tilPurchaseDate.visibility = View.GONE
        binding.etPurchaseDate.visibility = View.GONE
        binding.ivPurchaseDate.visibility = View.GONE
        binding.cvCreditCardPreview.visibility = View.GONE
        binding.tilInstallments.visibility = View.GONE
        binding.etInstallments.visibility = View.GONE
        binding.tvSwtPayWithCreditCard.visibility = View.GONE
        binding.swtPayWithCreditCard.visibility = View.GONE
        binding.tilReceivedDate.visibility = View.GONE
        binding.etReceivedDate.visibility = View.GONE
        binding.ivReceivedDate.visibility = View.GONE
        binding.tilRecurringTransactionDay.visibility = View.VISIBLE
        binding.etRecurringTransactionDay.visibility = View.VISIBLE
        binding.rgExpenseType.visibility = View.GONE
        adapter.updateCategories(categoriesList.getEarningCategoryList().sortedBy { it.description })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun recurringTransactionListDialog(recurringTransactionList : List<Transaction>, transactionType : String){

        val dialog = Dialogs.dialogTransactionList(
            requireActivity(),
            requireContext(),
            transactionType,
            recurringTransactionList,
            categoriesList,
            {recurringTransaction -> fillFieldsWithRecurringExpense(recurringTransaction)},
            {recurringTransaction -> fillFieldsWithRecurringEarning(recurringTransaction)}
        )

        dialog.show()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fillFieldsWithRecurringExpense(recurringExpense : RecurringTransaction){
        val formattedPrice = BigDecimal(recurringExpense.price).setScale(2, RoundingMode.HALF_UP).toString()
        binding.etPrice.setText(formattedPrice)
        binding.etDescription.setText(recurringExpense.description)
        binding.actvCategory.setText(recurringExpense.category)
        adapter.selectCategory(recurringExpense.category)
        if(recurringExpense.day != ""){
            val date = DateFunctions().purchaseDateForRecurringExpense(recurringExpense.day)
            binding.etPurchaseDate.setText(date)
            if(binding.swtPayWithCreditCard.isChecked){
                //TODO check behavior
                viewModel.getCreditCardList()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fillFieldsWithRecurringEarning(recurringEarning : RecurringTransaction){
        val formattedPrice = BigDecimal(recurringEarning.price).setScale(2, RoundingMode.HALF_UP).toString()
        binding.etPrice.setText(formattedPrice)
        binding.etDescription.setText(recurringEarning.description)
        binding.actvCategory.setText(recurringEarning.category)
        adapter.selectCategory(recurringEarning.category)
        if(recurringEarning.day != ""){
            val date = DateFunctions().purchaseDateForRecurringExpense(recurringEarning.day)
            binding.etReceivedDate.setText(date)
        }
    }

    private fun hasInternetConnection() : Boolean{
        return ConnectionFunctions.internetConnectionVerification(requireContext())
    }

    private fun changeToCommonExpense(){
        binding.tilInstallments.visibility = View.GONE
        binding.etInstallments.visibility = View.GONE
    }

    private fun changeToInstallmentExpense(){
        binding.tilInstallments.visibility = View.VISIBLE
        binding.etInstallments.visibility = View.VISIBLE
        binding.tilPaymentDate.hint = getString(R.string.payment_date_field_installment_hint)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showCreditCardPreview(creditCard : CreditCard, isDefaultCreditCard : Boolean){
        if(isDefaultCreditCard){
            binding.ivDefaultCardIcon.visibility = View.VISIBLE
        }else{
            binding.ivDefaultCardIcon.visibility = View.GONE
        }
        //Colors
        binding.llCreditCardPreview.setBackgroundColor(creditCard.colors.background)
        binding.tvCreditCardName.setTextColor(creditCard.colors.text)
        binding.tvPaymentDate.setTextColor(creditCard.colors.text)
        binding.tvPaymentDateTitle.setTextColor(creditCard.colors.text)
        //Text
        binding.tvCreditCardName.text = creditCard.nickName
        val paymentDate = DateFunctions().paymentDate(
            creditCard.expirationDay,
            creditCard.closingDay,
            binding.etPurchaseDate.text.toString()
            )
        binding.tvPaymentDate.text = paymentDate
        //Set payment day on textInput
        binding.etPaymentDate.setText(paymentDate)
        //Show
        binding.cvCreditCardPreview.visibility = View.VISIBLE
    }

    private fun hideCreditCardComponents(){
        binding.cvCreditCardPreview.visibility = View.GONE
        binding.cvSelectCreditCard.visibility = View.GONE
        binding.ivDefaultCardIcon.visibility = View.GONE
        binding.etPaymentDate.setText("")
    }

    private fun onPaymentDateChange(dateInMillis : Long){
        setSelectedDate(dateInMillis, binding.etPaymentDate)
        //Recalculate payment date
        binding.tvPaymentDate.text = binding.etPaymentDate.text.toString()
    }

}