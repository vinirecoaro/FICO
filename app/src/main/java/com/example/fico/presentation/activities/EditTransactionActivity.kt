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
import com.example.fico.databinding.ActivityEditExpenseBinding
import com.example.fico.model.Expense
import com.example.fico.presentation.viewmodel.EditTransactionViewModel
import com.example.fico.api.FormatValuesFromDatabase
import com.example.fico.presentation.adapters.CategoryListAdapter
import com.example.fico.presentation.interfaces.OnCategorySelectedListener
import com.example.fico.utils.constants.CategoriesList
import com.example.fico.utils.constants.StringConstants
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class EditTransactionActivity : AppCompatActivity(), OnCategorySelectedListener {

    val binding by lazy { ActivityEditExpenseBinding.inflate(layoutInflater) }
    private val viewModel : EditTransactionViewModel by inject()
    private lateinit var adapter: CategoryListAdapter
    private var expenseIdLength = 0
    lateinit var editingExpense : Expense
    private val categoriesList : CategoriesList by inject()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setColorBasedOnTheme()

        binding.editExpenseToolbar.setTitle("Editar Gasto")
        binding.editExpenseToolbar.setTitleTextColor(Color.WHITE)

        //Create category chooser
        adapter =
            CategoryListAdapter(categoriesList.getExpenseCategoryList().sortedBy { it.description }, this)
        binding.rvCategory.adapter = adapter

        val intent = intent
        if (intent != null) {
            val expense = intent.getParcelableExtra<Expense>("expense")
            if (expense != null) {
                editingExpense = expense
                expenseIdLength = expense.id.length
                //Verify if is a installment expense
                if (expenseIdLength == 41) {
                    binding.tilInstallments.visibility = View.VISIBLE
                    binding.tilPaymentDateEdit.hint = getString(R.string.payment_date_field_installment_hint)

                    val priceFormatted = FormatValuesFromDatabase().installmentExpensePrice(
                        expense.price,
                        expense.id
                    )
                    val description =
                        FormatValuesFromDatabase().installmentExpenseDescription(expense.description)
                    val nOfInstallment =
                        FormatValuesFromDatabase().installmentExpenseNofInstallment(expense.id)
                    val initialDate = FormatValuesFromDatabase().installmentExpenseInitialDate(
                        expense.id,
                        expense.paymentDate
                    )

                    binding.etPrice.setText(priceFormatted)
                    binding.etDescription.setText(description)
                    binding.actvCategory.setText(expense.category)
                    binding.etInstallments.setText(nOfInstallment)
                    binding.etPaymentDateEdit.setText(initialDate)
                    binding.etPurchaseDateEdit.setText(expense.purchaseDate)
                } else {

                    val priceFormatted = FormatValuesFromDatabase().price(expense.price)

                    binding.etPrice.setText(priceFormatted)
                    binding.etDescription.setText(expense.description)
                    binding.actvCategory.setText(expense.category)
                    binding.etPaymentDateEdit.setText(expense.paymentDate)
                    binding.etPurchaseDateEdit.setText(expense.purchaseDate)
                }
            }
            if (expense != null) {
                adapter.selectCategory(expense.category)
            }
        }

        //Insert a back button on Navigation bar
        setSupportActionBar(binding.editExpenseToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setUpListeners()

        setMaxLength(binding.etInstallments, 3)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpListeners() {
        binding.btSave.setOnClickListener {
            binding.btSave.isEnabled = false
            val expense = intent.getParcelableExtra<Expense>("expense")
            lifecycleScope.launch(Dispatchers.Main) {
                // Verify if is commom expense
                if (binding.tilInstallments.visibility == View.GONE) {
                    if (verifyFields(
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
                    if (verifyFields(
                            binding.etPrice,
                            binding.etDescription,
                            binding.actvCategory,
                            binding.etInstallments,
                            binding.etPaymentDateEdit,
                            binding.etPurchaseDateEdit
                        )
                    ) {
                        if (binding.etInstallments.text.toString() != "0") {
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
                                "O número de parcelas não pode ser 0",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
            binding.btSave.isEnabled = true

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
                .setTitleText("Escolha a Data")
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.edit_expense_menu, menu)
        val deleteMenuItem = menu?.findItem(R.id.edit_expense_menu_delete)
        // Check if expense is installment
        if (expenseIdLength == 41) {
            deleteMenuItem!!.isVisible = true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.edit_expense_menu_delete -> {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Apagar gasto")
                    .setMessage("Prosseguir com a exclusão deste gasto?")
                    .setPositiveButton("Confirmar") { dialog, which ->
                        viewModel.deleteInstallmentExpense(editingExpense)
                    }
                    .show()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun verifyFields(vararg text: EditText): Boolean {
        for (i in text) {
            if (i.text.toString() == "" || i == null) {
                Snackbar.make(binding.btSave, "Preencher o campo ${i.hint}", Snackbar.LENGTH_LONG)
                    .show()
                return false
            }
        }
        return true
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

}