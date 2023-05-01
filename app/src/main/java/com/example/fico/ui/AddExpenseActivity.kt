package com.example.fico.ui

import android.R
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.example.fico.databinding.ActivityAddExpenseBinding

class AddExpenseActivity : AppCompatActivity() {

    private val binding by lazy { ActivityAddExpenseBinding.inflate(layoutInflater) }
    private val categoryOptions = arrayOf("Comida", "Transporte", "Investimento", "Necessidade", "Remédio", "Entretenimento")

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
        // Define o que acontece quando o EditText é clicado
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
                    // Define o texto do EditText como a opção selecionada
                    binding.etCategory.setText(categoryOptions[position])
                    // Esconde o Spinner
                    binding.spCategoryOptions.visibility = View.GONE
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Faz nada
                }
            }
    }

    private fun categoryListConfig(){
        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, categoryOptions)

    // Define o layout para usar quando a lista de opções aparecer
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

    // Associa o adaptador ao Spinner
        binding.spCategoryOptions.adapter = adapter
    }
}