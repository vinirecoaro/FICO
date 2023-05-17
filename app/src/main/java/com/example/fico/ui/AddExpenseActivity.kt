package com.example.fico.ui

import android.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import com.example.fico.databinding.ActivityAddExpenseBinding
import com.example.fico.model.Expense
import com.example.fico.service.FirebaseAPI
import com.example.fico.ui.viewmodel.AddExpenseViewModel

class AddExpenseActivity : AppCompatActivity() {

    private val binding by lazy { ActivityAddExpenseBinding.inflate(layoutInflater) }
    private val categoryOptions = arrayOf("Comida", "Transporte", "Investimento", "Necessidade", "Remédio", "Entretenimento")
    private val viewModel by viewModels<AddExpenseViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setUpListeners()
        categoryListConfig()
    }

    private fun setUpListeners() {
        binding.btSave.setOnClickListener {
            finish()
        }
        binding.etCategory.setOnClickListener {
            binding.etCategory.setText(" ")
            binding.spCategoryOptions.visibility = View.VISIBLE
            binding.spCategoryOptions.performClick()
        }
        binding.spCategoryOptions.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    binding.etCategory.setText(categoryOptions[position])
                    binding.spCategoryOptions.visibility = View.GONE
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        binding.btSave.setOnClickListener {
            viewModel.addExpense(
                binding.etPrice.text.toString().toFloat(),
                binding.etDescription.text.toString(),
                binding.etCategory.text.toString(),
                binding.etDate.text.toString()
            )
            finish()
        }
    }

    private fun categoryListConfig(){
        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, categoryOptions)

    // Define o layout para usar quando a lista de opções aparecer
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)

    // Associa o adaptador ao Spinner
        binding.spCategoryOptions.adapter = adapter
    }
}