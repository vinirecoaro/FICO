package com.example.fico.presentation.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.fico.components.inputs.InputAdapters
import com.example.fico.components.inputs.InputFieldFunctions
import com.example.fico.components.inputs.InputValueHandle
import com.example.fico.databinding.ActivityAddCreditCardBinding
import com.example.fico.model.CreditCardColors
import com.example.fico.presentation.viewmodel.AddCreditCardViewModel
import org.koin.android.ext.android.inject

class AddCreditCardActivity : AppCompatActivity() {

    private val binding by lazy{ ActivityAddCreditCardBinding.inflate(layoutInflater)}
    private val viewModel : AddCreditCardViewModel by inject()
    lateinit var adapter : ArrayAdapter<CreditCardColors>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //Insert a back button on Navigation bar
        setSupportActionBar(binding.addCreditCardConfigurationToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = InputAdapters.colorAutoCompleteTextInputLayout(this, viewModel.colorOptions)

        initComponents()

        setUpListeners()

    }

    private fun initComponents(){
        binding.actvColors.setAdapter(
            adapter
        )
    }

    private fun setUpListeners(){
        binding.addCreditCardConfigurationToolbar.setNavigationOnClickListener {
            finish()
        }

        binding.actvColors.setOnItemClickListener { _, _, position, _ ->
            val selected = adapter.getItem(position) ?: return@setOnItemClickListener

            //Create string with circle and color name
            val spannable = InputValueHandle.circleColorfulWithText(binding.actvColors, selected.backgroundColor, selected.backgroundColorName)

            //Show credit card preview
            binding.cvCreditCardPreview.visibility = View.VISIBLE
            binding.llCreditCardPreview.setBackgroundColor(selected.backgroundColor)
            binding.tvCreditCardName.setTextColor(selected.textColor)

            //Enable save button
            binding.btCreditCardSave.visibility = View.VISIBLE

            binding.actvColors.setText(spannable, false)
        }

        binding.etCreditCardName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                if (text.isNotEmpty()) {
                   binding.tvCreditCardName.text = text
                }
            }
        })

        binding.etCreditCardExpirationDay.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                if (text.isNotEmpty()) {
                    var day = text.toIntOrNull()
                    val calendar = java.util.Calendar.getInstance()

                    // Obtem o ano e mês atuais
                    val year = calendar.get(java.util.Calendar.YEAR)
                    val month = calendar.get(java.util.Calendar.MONTH) // 0-based

                    // Define o dia no calendário, cuidando para não causar exceção
                    val maxDayOfMonth = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
                    if (day != null && day in 1..maxDayOfMonth) {
                        calendar.set(java.util.Calendar.DAY_OF_MONTH, day)
                        calendar.set(java.util.Calendar.MONTH, month)
                        calendar.set(java.util.Calendar.YEAR, year)

                        // Formata a data para exibição
                        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                        val formattedDate = dateFormat.format(calendar.time)

                        binding.tvPaymentDate.text = formattedDate
                    } else {
                        day = maxDayOfMonth
                        calendar.set(java.util.Calendar.DAY_OF_MONTH, day)
                        calendar.set(java.util.Calendar.MONTH, month)
                        calendar.set(java.util.Calendar.YEAR, year)

                        // Formata a data para exibição
                        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                        val formattedDate = dateFormat.format(calendar.time)
                        binding.etCreditCardExpirationDay.setText(day.toString())
                        binding.tvPaymentDate.text = formattedDate
                    }
                }
            }
        })

        binding.btCreditCardSave.setOnClickListener {
            it.isEnabled = false
            InputFieldFunctions.isFilled(
                this,
                binding.btCreditCardSave,
                binding.etCreditCardName,
                binding.etCreditCardExpirationDay,
                binding.etCreditCardClosingDay
            )
            it.isEnabled = true
        }

    }



}
