package com.example.fico.ui.activities.expense

import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.example.fico.databinding.ActivityEditExpenseBinding
import com.example.fico.model.Expense
import com.example.fico.ui.viewmodel.EditExpenseViewModel
import com.example.fico.api.FormatValuesFromDatabase
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat

class EditExpenseActivity : AppCompatActivity() {

    val binding by lazy { ActivityEditExpenseBinding.inflate(layoutInflater) }
    val viewModel by viewModels<EditExpenseViewModel>()
    private val categoryOptions = arrayOf("Comida", "Transporte", "Investimento", "Necessidade", "Remédio", "Entretenimento")

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.editExpenseToolbar.setTitle("Editar Gasto")
        binding.editExpenseToolbar.setTitleTextColor(Color.WHITE)

        val intent = intent
        if(intent != null){
            val expense = intent.getParcelableExtra<Expense>("expense")
            if(expense != null){
                val lenght = expense.id.length
                //Verify if is a installment expense
                if(lenght == 41){
                    binding.etInstallments.visibility = View.VISIBLE

                    val priceFormatted = FormatValuesFromDatabase().installmentExpensePrice(expense.price, expense.id)
                    val description = FormatValuesFromDatabase().installmentExpenseDescription(expense.description)
                    val nOfInstallment = FormatValuesFromDatabase().installmentExpenseNofInstallment(expense.id)
                    val initialDate = FormatValuesFromDatabase().installmentExpenseInitialDate(expense.id, expense.date)

                    binding.etPrice.setText(priceFormatted)
                    binding.etDescription.setText(description)
                    binding.actvCategory.setText(expense.category)
                    binding.etInstallments.setText(nOfInstallment)
                    binding.etDate.setText(initialDate)
                }else{

                    val priceFormatted = FormatValuesFromDatabase().price(expense.price)

                    binding.etPrice.setText(priceFormatted)
                    binding.etDescription.setText(expense.description)
                    binding.actvCategory.setText(expense.category)
                    binding.etDate.setText(expense.date)
                }
            }
        }

        //Insert a back button on Navigation bar
        setSupportActionBar(binding.editExpenseToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setUpListeners()
        actvConfig()

        setMaxLength(binding.etInstallments,3)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpListeners(){
        binding.btSave.setOnClickListener{
            binding.btSave.isEnabled = false
            val expense = intent.getParcelableExtra<Expense>("expense")
            lifecycleScope.launch(Dispatchers.Main){
                if(binding.etInstallments.visibility == View.GONE){
                    if(verifyFields(binding.etPrice, binding.etDescription, binding.actvCategory, binding.etDate)){
                        if(viewModel.saveEditExpense(
                            expense!!,
                            binding.etPrice.text.toString(),
                            binding.etDescription.text.toString(),
                            binding.actvCategory.text.toString(),
                            binding.etDate.text.toString(),
                            false
                        ).await()){
                            hideKeyboard(this@EditExpenseActivity, binding.btSave)
                            Toast.makeText(this@EditExpenseActivity, "Gasto alterado com sucesso", Toast.LENGTH_LONG).show()
                        }
                        //delay necessary to return to Expense list with value updated
                        delay(250)
                        finish()
                    }
                }else if(binding.etInstallments.visibility == View.VISIBLE){
                    if(verifyFields(binding.etPrice, binding.etDescription, binding.actvCategory,binding. etInstallments ,binding.etDate)){
                        if(binding.etInstallments.text.toString() != "0"){
                            if(viewModel.saveEditExpense(
                                    expense!!,
                                    binding.etPrice.text.toString(),
                                    binding.etDescription.text.toString(),
                                    binding.actvCategory.text.toString(),
                                    binding.etDate.text.toString(),
                                    true,
                                    binding.etInstallments.text.toString().toInt()
                            ).await()){
                                hideKeyboard(this@EditExpenseActivity,binding.btSave)
                                Toast.makeText(this@EditExpenseActivity, "Gasto alterado com sucesso", Toast.LENGTH_LONG).show()
                            }
                            //delay necessary to return to Expense list with value updated
                            delay(250)
                            finish()
                        }else{
                            Toast.makeText(this@EditExpenseActivity, "O número de parcelas não pode ser 0", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
            binding.btSave.isEnabled = true

        }

        binding.etPrice.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.dpDateExpense.visibility = View.GONE
                binding.etPrice.setText("")
            }
        }

        binding.etDescription.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.dpDateExpense.visibility = View.GONE
                binding.etDescription.setText("")
            }
        }

        binding.actvCategory.setOnClickListener{
            binding.dpDateExpense.visibility = View.GONE
            binding.actvCategory.showDropDown()
        }

        binding.etInstallments.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.dpDateExpense.visibility = View.GONE
                binding.etInstallments.setText("")
            }
        }

        binding.ivDate.setOnClickListener{
            binding.dpDateExpense.visibility = View.VISIBLE
            binding.dpDateExpense.setOnDateChangedListener { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                binding.etDate.setText(selectedDate)
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

        binding.editExpenseToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun actvConfig() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categoryOptions)
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

    private fun hideKeyboard(context: Context, view: View){
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken,0)
    }

}