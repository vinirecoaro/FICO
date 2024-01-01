package com.example.fico.ui.activities.expense

import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.lifecycleScope
import com.example.fico.databinding.ActivityEditExpenseBinding
import com.example.fico.model.Expense
import com.example.fico.ui.viewmodel.EditExpenseViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
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

                    val regex = Regex("[\\d,.]+")
                    val expenseInstallment = BigDecimal(regex.find(expense.price)!!.value.replace(",","."))
                    val nOfInstallment = BigDecimal(expense.id.substring(38,41))
                    val price = expenseInstallment.multiply(nOfInstallment)
                    val priceFormatted = (NumberFormat.getCurrencyInstance().format(price))
                    binding.etPrice.setText(priceFormatted)
                    binding.etDescription.setText(expense.description.split(" Parcela")[0])
                    binding.actvCategory.setText(expense.category)
                    binding.etInstallments.setText(expense.id.substring(38,41).toInt().toString())
                    binding.etDate.setText(returnInitialDate(expense.id, expense.date))
                }else{
                    val regex = Regex("[\\d,.]+")
                    val justNumber = BigDecimal(regex.find(expense.price)!!.value.replace(",","."))
                    val priceFormatted = NumberFormat.getCurrencyInstance().format(justNumber)
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
            lifecycleScope.launch(Dispatchers.Main){
                if(binding.etInstallments.visibility == View.GONE){
                    if(verifyFields(binding.etPrice, binding.etDescription, binding.actvCategory, binding.etDate)){
                        val day = binding.etDate.text.toString().substring(0, 2)
                        val month = binding.etDate.text.toString().substring(3, 5)
                        val year = binding.etDate.text.toString().substring(6, 10)
                        val modifiedDate = "$year-$month-$day"

                        val regex = Regex("[\\d,.]+")
                        val justNumber = regex.find(binding.etPrice.text.toString())
                        val correction = BigDecimal("100")
                        val numClean = BigDecimal(justNumber!!.value.replace(",","").replace(".",""))
                        val formatedNum = numClean.divide(correction)
                        val formattedNumString = formatedNum.toString().replace(",",".")

                        val expense = intent.getParcelableExtra<Expense>("expense")
                        viewModel.saveEditExpense(
                            expense!!,
                            formattedNumString,
                            binding.etDescription.text.toString(),
                            binding.actvCategory.text.toString(),
                            modifiedDate).await()
                        //delay necessary to return to Expense list with value updated
                        delay(250)
                        finish()
                    }
                }else if(binding.etInstallments.visibility == View.VISIBLE){
                    if(verifyFields(binding.etPrice, binding.etDescription, binding.actvCategory,binding. etInstallments ,binding.etDate)){
                        val day = binding.etDate.text.toString().substring(0, 2)
                        val month = binding.etDate.text.toString().substring(3, 5)
                        val year = binding.etDate.text.toString().substring(6, 10)
                        val modifiedDate = "$year-$month-$day"

                        val regex = Regex("[\\d,.]+")
                        val justNumber = regex.find(binding.etPrice.text.toString())
                        val divisor = BigDecimal(binding.etInstallments.text.toString())
                        val denominator = BigDecimal(justNumber!!.value
                            .replace(",","")
                            .replace(".",""))
                        val installmentPrice = denominator.divide(divisor, 8, RoundingMode.HALF_UP)
                        val correction = BigDecimal("100")
                        val installmentPriceFormatted = installmentPrice.divide(correction)
                        val formatedNum = installmentPriceFormatted.setScale(8, RoundingMode.HALF_UP)
                        val formattedNumString = formatedNum.toString().replace(",",".")

                        val expense = intent.getParcelableExtra<Expense>("expense")
                        viewModel.saveEditInstallmentExpense(
                            formattedNumString,
                            binding.etDescription.text.toString(),
                            binding.actvCategory.text.toString(),
                            modifiedDate,
                            binding.etInstallments.text.toString().toInt()).await()
                        viewModel.deleteOldInstallmentExpense(expense!!).await()
                        viewModel.updateTotalExpenseAfterEditInstallmentExpense(expense).await()
                        //delay necessary to return to Expense list with value updated
                        delay(250)
                        finish()
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

    private fun returnInitialDate(id: String, date: String) : String{
        val currentInstallment = id.substring(35,37).toInt().toString()
        var day = date.substring(0, 2)
        val month = date.substring(3, 5)
        val year = date.substring(6, 10)
        var initialYear = year.toInt() - (currentInstallment.toInt()/12)
        var initialMonth = 1
        var initialMonthString = ""

        val restAfterDivideTwelve = currentInstallment.toInt()%12

        if(month.toInt() < restAfterDivideTwelve){
            initialMonth = 12 + (month.toInt() - restAfterDivideTwelve) + 1
            initialMonthString = initialMonth.toString()
            initialYear -= 1
        }else if(month.toInt() > restAfterDivideTwelve){
            initialMonth = month.toInt() - restAfterDivideTwelve + 1
            initialMonthString = initialMonth.toString()
        }
        if(day.toInt() < 10){
            day = "0${day.toInt()}"
        }
        if(initialMonth < 10){
            initialMonthString = "0${initialMonth}"
        }

        return "${day}/${initialMonthString}/${initialYear}"
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

}