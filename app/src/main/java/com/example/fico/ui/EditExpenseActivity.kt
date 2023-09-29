package com.example.fico.ui

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.example.fico.R
import com.example.fico.databinding.ActivityEditExpenseBinding
import com.example.fico.model.Expense
import com.example.fico.ui.viewmodel.EditExpenseViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DecimalFormat

class EditExpenseActivity : AppCompatActivity() {

    val binding by lazy { ActivityEditExpenseBinding.inflate(layoutInflater) }
    val viewModel by viewModels<EditExpenseViewModel>()
    private val categoryOptions = arrayOf("Comida", "Transporte", "Investimento", "Necessidade", "Remédio", "Entretenimento")

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle("Editar gasto")
        val intent = intent
        if(intent != null){
            val expense = intent.getParcelableExtra<Expense>("expense")
            if(expense != null){
                binding.etPrice.setText(expense.price.replace("R$ ","").replace(",","."))
                binding.etDescription.setText(expense.description)
                binding.actvCategory.setText(expense.category)
                binding.etDate.setText(expense.date)
            }
        }
        setUpListeners()
        actvConfig()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpListeners(){
        binding.btSave.setOnClickListener{
            lifecycleScope.launch(Dispatchers.IO){
                if(verifyFields(binding.etPrice, binding.etDescription, binding.actvCategory, binding.etDate)){
                    val day = binding.etDate.text.toString().substring(0, 2)
                    val month = binding.etDate.text.toString().substring(3, 5)
                    val year = binding.etDate.text.toString().substring(6, 10)
                    val modifiedDate = "$year-$month-$day"
                    val formatNum = DecimalFormat("#.##")
                    val formattedNum = formatNum.format(binding.etPrice.text.toString().toFloat())
                    val expense = intent.getParcelableExtra<Expense>("expense")
                    viewModel.saveEditExpense(
                        expense!!,
                        formattedNum.toString(),
                        binding.etDescription.text.toString(),
                        binding.actvCategory.text.toString(),
                        modifiedDate)
                }
            }
        }

        binding.actvCategory.setOnClickListener {
            binding.actvCategory.showDropDown()
            binding.dpDateExpense.visibility = View.GONE
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
}