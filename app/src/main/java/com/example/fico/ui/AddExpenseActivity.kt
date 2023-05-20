package com.example.fico.ui

import android.R
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import com.example.fico.databinding.ActivityAddExpenseBinding
import com.example.fico.ui.viewmodel.AddExpenseViewModel
import java.text.DecimalFormat

class AddExpenseActivity : AppCompatActivity() {

    private val binding by lazy { ActivityAddExpenseBinding.inflate(layoutInflater) }
    private val categoryOptions = arrayOf("Comida", "Transporte", "Investimento", "Necessidade", "Remédio", "Entretenimento")
    private val viewModel by viewModels<AddExpenseViewModel>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setUpListeners()
        actvConfig()

        binding.etDate.setText(viewModel.getCurrentlyDate())
        binding.etDate.inputType = InputType.TYPE_NULL
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpListeners() {

        binding.btSave.setOnClickListener {
            val day = binding.etDate.text.toString().substring(0,2)
            val month = binding.etDate.text.toString().substring(3,5)
            val year = binding.etDate.text.toString().substring(6,10)
            val modifiedDate = "$year-$month-$day"

            val formatNum = DecimalFormat("#.##")
            val formatedNum = formatNum.format(binding.etPrice.text.toString().toFloat())
            viewModel.addExpense(
                formatedNum.toString(),
                binding.etDescription.text.toString(),
                binding.actvCategory.text.toString(),
                modifiedDate
            )
            finish()
        }

        binding.actvCategory.setOnClickListener {
            binding.actvCategory.showDropDown()
        }

        binding.etDate.setOnClickListener {
            binding.dpDateExpense.visibility = View.VISIBLE
            binding.dpDateExpense.setOnDateChangedListener{_, selectedYear,selectedMonth, selectedDay ->
                val selectedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                binding.etDate.setText(selectedDate)
            }
        }
        binding.etDate.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if(!hasFocus){
                binding.dpDateExpense.visibility = View.GONE
            }
        }
    }

    private fun actvConfig(){
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categoryOptions)
        binding.actvCategory.setAdapter(adapter)
    }


}