package com.example.fico.presentation.activities

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.Spanned
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.example.fico.R
import com.example.fico.presentation.viewmodel.EditTransactionViewModel
import com.example.fico.api.FormatValuesFromDatabase
import com.example.fico.presentation.components.dialogs.Dialogs
import com.example.fico.presentation.components.PersonalizedSnackBars
import com.example.fico.databinding.ActivityEditTransactionBinding
import com.example.fico.model.Transaction
import com.example.fico.presentation.adapters.CategoryListAdapter
import com.example.fico.interfaces.OnCategorySelectedListener
import com.example.fico.utils.constants.CategoriesList
import com.example.fico.utils.constants.StringConstants
import com.example.fico.utils.internet.ConnectionFunctions
import com.example.fico.presentation.components.inputs.InputFieldFunctions
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class EditTransactionActivity : AppCompatActivity(), OnCategorySelectedListener {

    val binding by lazy { ActivityEditTransactionBinding.inflate(layoutInflater) }
    private val viewModel : EditTransactionViewModel by inject()
    private lateinit var adapter: CategoryListAdapter
    private var expenseIdLength = 0
    private lateinit var editingTransaction : Transaction
    private val categoriesList : CategoriesList by inject()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setColorBasedOnTheme()

        binding.editExpenseToolbar.setTitle(getString(R.string.edit))
        binding.editExpenseToolbar.setTitleTextColor(Color.WHITE)

        //Create category chooser
        adapter =
            CategoryListAdapter(categoriesList.getExpenseCategoryList().sortedBy { it.description }, this)
        binding.rvCategory.adapter = adapter

        val intent = intent
        if (intent != null) {
            val transaction = intent.getSerializableExtra(StringConstants.TRANSACTION_LIST.TRANSACTION) as? Transaction
            if (transaction != null) {

                val priceFormatted = FormatValuesFromDatabase().price(transaction.price)

                if(transaction.type == StringConstants.DATABASE.EXPENSE){
                    editingTransaction = transaction
                    expenseIdLength = transaction.id.length
                    //Verify if is a installment expense
                    if (expenseIdLength == 41) {
                        binding.tilInstallments.visibility = View.VISIBLE
                        binding.tilPaymentDateEdit.hint = getString(R.string.payment_date_field_installment_hint)

                        val installmentPriceFormatted = FormatValuesFromDatabase().installmentExpensePrice(
                            transaction.price,
                            transaction.id
                        )
                        val description =
                            FormatValuesFromDatabase().installmentExpenseDescription(transaction.description)
                        val nOfInstallment =
                            FormatValuesFromDatabase().installmentExpenseNofInstallment(transaction.id)
                        val initialDate = FormatValuesFromDatabase().installmentExpenseInitialDate(
                            transaction.id,
                            transaction.paymentDate
                        )

                        binding.etPrice.setText(installmentPriceFormatted)
                        binding.etDescription.setText(description)
                        binding.actvCategory.setText(transaction.category)
                        binding.etInstallments.setText(nOfInstallment)
                        binding.etPaymentDateEdit.setText(initialDate)
                        binding.etPurchaseDateEdit.setText(transaction.purchaseDate)
                    } else {

                        binding.etPrice.setText(priceFormatted)
                        binding.etDescription.setText(transaction.description)
                        binding.actvCategory.setText(transaction.category)
                        binding.etPaymentDateEdit.setText(transaction.paymentDate)
                        binding.etPurchaseDateEdit.setText(transaction.purchaseDate)
                    }
                }

                else if(transaction.type == StringConstants.DATABASE.EARNING){
                    editingTransaction = transaction
                    changeComponentsToEarningState()

                    binding.etPrice.setText(priceFormatted)
                    binding.etDescription.setText(transaction.description)
                    binding.actvCategory.setText(transaction.category)
                    binding.etPaymentDateEdit.setText(transaction.paymentDate)
                }

                else if(transaction.type == StringConstants.DATABASE.RECURRING_EXPENSE
                    || transaction.type == StringConstants.DATABASE.RECURRING_EARNING)
                {
                    editingTransaction = transaction

                    changeComponentsToRecurringTransactionState(transaction)

                    binding.etPrice.setText(priceFormatted)
                    binding.etDescription.setText(transaction.description)
                    binding.actvCategory.setText(transaction.category)
                    binding.etPaymentDateEdit.setText(transaction.paymentDate)
                }

                adapter.selectCategory(transaction.category)
            }
        }

        //Insert a back button on Navigation bar
        setSupportActionBar(binding.editExpenseToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setUpListeners()

        setMaxLength(binding.etInstallments, 3)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.edit_expense_menu, menu)
        return true
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.edit_expense_menu_delete -> {
                if(hasInternetConnection()){
                    when(val type = editingTransaction.type){

                        //delete expense
                        StringConstants.DATABASE.EXPENSE -> {
                            if(expenseIdLength == 41)   {
                                dialogDeleteInstallmentExpense()
                            }else{
                                dialogDeleteExpense()
                            }
                        }

                        //delete earning
                        StringConstants.DATABASE.EARNING -> {
                            dialogDeleteEarning()
                        }

                        //delete recurring transaction
                        StringConstants.DATABASE.RECURRING_EXPENSE, StringConstants.DATABASE.RECURRING_EARNING -> {
                            dialogDeleteRecurringTransaction(type)
                        }
                    }
                }else{
                    PersonalizedSnackBars.noInternetConnection(binding.btSave, this).show()
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

            if(hasInternetConnection()){
                when (editingTransaction.type) {

                    StringConstants.DATABASE.EXPENSE -> {
                        val expense = editingTransaction.toExpense()
                        lifecycleScope.launch(Dispatchers.Main) {
                            // Verify if is commom expense
                            if (binding.tilInstallments.visibility == View.GONE) {
                                if (InputFieldFunctions.isFilled(
                                        this@EditTransactionActivity,
                                        binding.btSave,
                                        binding.etPrice,
                                        binding.etDescription,
                                        binding.actvCategory,
                                        binding.etPaymentDateEdit,
                                        binding.etPurchaseDateEdit
                                    )
                                ) {
                                    viewModel.saveEditExpense(
                                        expense!!,
                                        binding.etPrice.text.toString(),
                                        binding.etDescription.text.toString(),
                                        binding.actvCategory.text.toString(),
                                        binding.etPaymentDateEdit.text.toString(),
                                        binding.etPurchaseDateEdit.text.toString(),
                                        false
                                    )
                                }
                            } else if (binding.tilInstallments.visibility == View.VISIBLE) {
                                if (InputFieldFunctions.isFilled(
                                        this@EditTransactionActivity,
                                        binding.btSave,
                                        binding.etPrice,
                                        binding.etDescription,
                                        binding.actvCategory,
                                        binding.etInstallments,
                                        binding.etPaymentDateEdit,
                                        binding.etPurchaseDateEdit
                                    )
                                ) {
                                    if (binding.etInstallments.text.toString() != StringConstants.GENERAL.ZERO_STRING) {
                                        viewModel.saveEditExpense(
                                            expense!!,
                                            binding.etPrice.text.toString(),
                                            binding.etDescription.text.toString(),
                                            binding.actvCategory.text.toString(),
                                            binding.etPaymentDateEdit.text.toString(),
                                            binding.etPurchaseDateEdit.text.toString(),
                                            true,
                                            binding.etInstallments.text.toString().toInt()
                                        )
                                    } else {
                                        Toast.makeText(
                                            this@EditTransactionActivity,
                                            getString(R.string.wrong_installment_input_message),
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }
                        }
                    }

                    StringConstants.DATABASE.EARNING -> {
                        val earning = editingTransaction.toEarning()
                        lifecycleScope.launch(Dispatchers.Main) {
                            if(InputFieldFunctions.isFilled(
                                    this@EditTransactionActivity,
                                    binding.btSave,
                                    binding.etPrice,
                                    binding.etDescription,
                                    binding.actvCategory,
                                    binding.etPaymentDateEdit)
                            ){
                                viewModel.saveEditEarning(
                                    earning,
                                    binding.etPrice.text.toString(),
                                    binding.etDescription.text.toString(),
                                    binding.actvCategory.text.toString(),
                                    binding.etPaymentDateEdit.text.toString(),
                                )
                            }
                        }
                    }

                    StringConstants.DATABASE.RECURRING_EXPENSE,
                    StringConstants.DATABASE.RECURRING_EARNING -> {
                        val recurringTransaction = editingTransaction.toRecurringTransaction()
                        lifecycleScope.launch(Dispatchers.Main) {
                            if(InputFieldFunctions.isFilled(
                                    this@EditTransactionActivity,
                                    binding.btSave,
                                    binding.etPrice,
                                    binding.etDescription,
                                    binding.actvCategory)
                            ){
                                if(binding.etPaymentDateEdit.text.isNullOrEmpty()){
                                    viewModel.saveEditRecurringTransaction(
                                        recurringTransaction,
                                        binding.etPrice.text.toString(),
                                        binding.etDescription.text.toString(),
                                        binding.actvCategory.text.toString(),
                                        ""
                                    )
                                }else{
                                    if(binding.etPaymentDateEdit.text.toString().toInt() in 1..31){
                                        viewModel.saveEditRecurringTransaction(
                                            recurringTransaction,
                                            binding.etPrice.text.toString(),
                                            binding.etDescription.text.toString(),
                                            binding.actvCategory.text.toString(),
                                            binding.etPaymentDateEdit.text.toString()
                                        )
                                    }else{
                                        Snackbar.make(binding.btSave,getString(R.string.invalid_day), Snackbar.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                    }
                }
            }else{
                PersonalizedSnackBars.noInternetConnection(binding.btSave, this).show()
            }

            binding.btSave.isEnabled = true
        }

        binding.ivPaymentDate.setOnClickListener {
            binding.btSave.visibility = View.VISIBLE
            binding.ivPaymentDate.isEnabled = false

            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.choose_date)
                .build()

            datePicker.addOnPositiveButtonClickListener {
                val selectedDateInMillis = it
                val formattedDate = formatDate(selectedDateInMillis)
                binding.etPaymentDateEdit.setText(formattedDate)
                binding.ivPaymentDate.isEnabled = true
            }

            datePicker.addOnNegativeButtonClickListener {
                binding.ivPaymentDate.isEnabled = true
            }

            datePicker.addOnDismissListener {
                binding.ivPaymentDate.isEnabled = true
            }

            datePicker.show(supportFragmentManager, "PaymentDate")
        }

        binding.ivPurchaseDateEdit.setOnClickListener {
            binding.btSave.visibility = View.VISIBLE
            binding.ivPurchaseDateEdit.isEnabled = false

            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.choose_date)
                .build()

            datePicker.addOnPositiveButtonClickListener {
                val selectedDateInMillis = it
                val formattedDate = formatDate(selectedDateInMillis)
                binding.etPurchaseDateEdit.setText(formattedDate)
                binding.ivPurchaseDateEdit.isEnabled = true
            }

            datePicker.addOnNegativeButtonClickListener {
                binding.ivPurchaseDateEdit.isEnabled = true
            }

            datePicker.addOnDismissListener {
                binding.ivPurchaseDateEdit.isEnabled = true
            }

            datePicker.show(supportFragmentManager, "PurchaseDate")
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

        binding.editExpenseToolbar.setNavigationOnClickListener {
            setResult(StringConstants.RESULT_CODES.BACK_BUTTON_PRESSED)
            finish()
        }

        viewModel.editExpenseResult.observe(this) { result ->
            if (result) {
                //Minimize keyboard and show message
                hideKeyboard(this, binding.btSave)
                setResult(Activity.RESULT_OK)
                finish()
            } else {
                //Minimize keyboard and show message
                hideKeyboard(this, binding.btSave)
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }

        viewModel.editEarningResult.observe(this) { result ->
            if (result) {
                //Minimize keyboard and show message
                hideKeyboard(this, binding.btSave)
                setResult(StringConstants.RESULT_CODES.EDIT_EARNING_EXPENSE_RESULT_OK)
                finish()
            } else {
                //Minimize keyboard and show message
                hideKeyboard(this, binding.btSave)
                setResult(StringConstants.RESULT_CODES.EDIT_EARNING_EXPENSE_RESULT_FAILURE)
                finish()
            }
        }

        viewModel.editRecurringTransactionResult.observe(this) { result ->
            if (result) {
                //Minimize keyboard and show message
                hideKeyboard(this, binding.btSave)
                setResult(StringConstants.RESULT_CODES.EDIT_RECURRING_TRANSACTION_OK)
                finish()
            } else {
                //Minimize keyboard and show message
                hideKeyboard(this, binding.btSave)
                setResult(StringConstants.RESULT_CODES.EDIT_RECURRING_TRANSACTION_FAILURE)
                finish()
            }
        }

        viewModel.deleteExpenseResult.observe(this) { result ->
            if (result) {
                //Minimize keyboard and show message
                hideKeyboard(this, binding.btSave)
                setResult(StringConstants.RESULT_CODES.DELETE_EXPENSE_RESULT_OK)
                finish()
            } else {
                //Minimize keyboard and show message
                hideKeyboard(this, binding.btSave)
                setResult(StringConstants.RESULT_CODES.DELETE_EXPENSE_RESULT_FAILURE)
                finish()
            }
        }

        viewModel.deleteEarningResult.observe(this) { result ->
            if (result) {
                //Minimize keyboard and show message
                hideKeyboard(this, binding.btSave)
                setResult(StringConstants.RESULT_CODES.DELETE_EARNING_RESULT_OK)
                finish()
            } else {
                //Minimize keyboard and show message
                hideKeyboard(this, binding.btSave)
                setResult(StringConstants.RESULT_CODES.DELETE_EARNING_RESULT_FAILURE)
                finish()
            }
        }

        viewModel.deleteRecurringTransactionResult.observe(this) { result ->
            if (result) {
                //Minimize keyboard and show message
                hideKeyboard(this, binding.btSave)
                setResult(StringConstants.RESULT_CODES.DELETE_RECURRING_TRANSACTION_RESULT_OK)
                finish()
            } else {
                //Minimize keyboard and show message
                hideKeyboard(this, binding.btSave)
                setResult(StringConstants.RESULT_CODES.DELETE_RECURRING_TRANSACTION_RESULT_FAILURE)
                finish()
            }
        }

        binding.ivArrowUpGetPurchaseDateEdit.setOnClickListener {
            binding.etPaymentDateEdit.text = binding.etPurchaseDateEdit.text
        }

        viewModel.deleteInstallmentExpenseResult.observe(this){result ->
            if(result){
                setResult(StringConstants.RESULT_CODES.DELETE_INSTALLMENT_EXPENSE_RESULT_OK)
                finish()
            }else{
                setResult(StringConstants.RESULT_CODES.DELETE_INSTALLMENT_EXPENSE_RESULT_FAILURE)
                finish()
            }
        }
    }

    private fun setMaxLength(editText: EditText, maxLength: Int) {
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

    private fun hideKeyboard(context: Context, view: View) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun setColorBasedOnTheme() {
        when (this.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.ivPaymentDate.setImageResource(R.drawable.baseline_calendar_month_light)
                binding.ivPurchaseDateEdit.setImageResource(R.drawable.baseline_calendar_month_light)
                binding.ivArrowUpGetPurchaseDateEdit.setImageResource(R.drawable.arrow_up_light)
            }

            Configuration.UI_MODE_NIGHT_NO -> {
                binding.ivPaymentDate.setImageResource(R.drawable.baseline_calendar_month_dark)
                binding.ivPurchaseDateEdit.setImageResource(R.drawable.baseline_calendar_month_dark)
                binding.ivArrowUpGetPurchaseDateEdit.setImageResource(R.drawable.arrow_up_black)

            }

            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }
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

    private fun changeComponentsToEarningState(){
        binding.editExpenseToolbar.setTitle(getString(R.string.edit_earning))
        binding.tilPurchaseDateEdit.visibility = View.GONE
        binding.etPurchaseDateEdit.visibility = View.GONE
        binding.ivPurchaseDateEdit.visibility = View.GONE
        binding.ivArrowUpGetPurchaseDateEdit.visibility = View.GONE
        binding.tilPaymentDateEdit.hint = getString(R.string.add_date)
        binding.tilInstallments.visibility = View.GONE
        binding.etInstallments.visibility = View.GONE
        adapter.updateCategories(categoriesList.getEarningCategoryList().sortedBy { it.description })
    }

    private fun changeComponentsToRecurringTransactionState(transaction : Transaction){
        binding.tilPurchaseDateEdit.visibility = View.GONE
        binding.etPurchaseDateEdit.visibility = View.GONE
        binding.ivPurchaseDateEdit.visibility = View.GONE
        binding.ivArrowUpGetPurchaseDateEdit.visibility = View.GONE
        binding.ivPaymentDate.visibility = View.GONE
        binding.tilPaymentDateEdit.hint = getString(R.string.day)
        binding.etPaymentDateEdit.isFocusable = true
        binding.etPaymentDateEdit.isFocusableInTouchMode = true
        binding.etPaymentDateEdit.inputType = InputType.TYPE_CLASS_NUMBER
        binding.tilInstallments.visibility = View.GONE
        binding.etInstallments.visibility = View.GONE
        if(transaction.type == StringConstants.DATABASE.RECURRING_EXPENSE){
            binding.editExpenseToolbar.setTitle(getString(R.string.edit_recurring_expense))
            adapter.updateCategories(categoriesList.getExpenseCategoryList().sortedBy { it.description })
        }
        else if(transaction.type == StringConstants.DATABASE.RECURRING_EARNING){
            binding.editExpenseToolbar.setTitle(getString(R.string.edit_recurring_earning))
            adapter.updateCategories(categoriesList.getEarningCategoryList().sortedBy { it.description })
        }

    }

    private fun dialogDeleteInstallmentExpense(){
        val dialog = Dialogs.dialogModelOne(
            this,
            this,
            getString(R.string.delete_installment_expense),
            getString(R.string.delete_expense_dialog_message),
            getString(R.string.confirm)
        ){
            val expense = editingTransaction.toExpense()
            viewModel.deleteInstallmentExpense(expense)
        }
        dialog.show()
    }

    private fun dialogDeleteExpense(){
        val dialog = Dialogs.dialogModelOne(
            this,
            this,
            getString(R.string.delete_expense),
            getString(R.string.delete_expense_dialog_message),
            getString(R.string.confirm)
        ){
            val expense = editingTransaction.toExpense()
            viewModel.deleteExpense(expense)
        }
        dialog.show()
    }

    private fun dialogDeleteEarning(){
        val dialog = Dialogs.dialogModelOne(
            this,
            this,
            getString(R.string.delete_earning),
            getString(R.string.delete_earning_dialog_message),
            getString(R.string.confirm)
        ){
            val earning = editingTransaction.toEarning()
            viewModel.deleteEarning(earning)
        }
        dialog.show()
    }

    private fun dialogDeleteRecurringTransaction(type : String){
        var title = ""
        var subtitle = ""
        when(type){
            StringConstants.DATABASE.RECURRING_EXPENSE -> {
                title = getString(R.string.delete_recurring_expense)
                subtitle = getString(R.string.delete_expense_dialog_message)
            }
            StringConstants.DATABASE.RECURRING_EARNING -> {
                title = getString(R.string.delete_recurring_earning)
                subtitle = getString(R.string.delete_earning_dialog_message)
            }
        }

        val dialog = Dialogs.dialogModelOne(
            this,
            this,
            title,
            subtitle,
            getString(R.string.confirm)
        ){
            val recurringTransaction = editingTransaction.toRecurringTransaction()
            viewModel.deleteRecurringTransaction(recurringTransaction)
        }
        dialog.show()

    }

    private fun hasInternetConnection() : Boolean{
        return ConnectionFunctions.internetConnectionVerification(this)
    }

}