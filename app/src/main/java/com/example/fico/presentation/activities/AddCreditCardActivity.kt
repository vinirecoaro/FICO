package com.example.fico.presentation.activities

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.fico.R
import com.example.fico.components.PersonalizedSnackBars
import com.example.fico.components.inputs.InputAdapters
import com.example.fico.components.inputs.InputFieldFunctions
import com.example.fico.components.inputs.InputValueHandle
import com.example.fico.databinding.ActivityAddCreditCardBinding
import com.example.fico.model.CreditCardColors
import com.example.fico.presentation.viewmodel.AddCreditCardViewModel
import com.example.fico.utils.UiFunctions
import com.example.fico.utils.internet.ConnectionFunctions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class AddCreditCardActivity : AppCompatActivity() {

    private val binding by lazy{ ActivityAddCreditCardBinding.inflate(layoutInflater)}
    private val viewModel : AddCreditCardViewModel by inject()
    lateinit var adapter : ArrayAdapter<CreditCardColors>

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //Insert a back button on Navigation bar
        setSupportActionBar(binding.addCreditCardConfigurationToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = InputAdapters.colorAutoCompleteTextInputLayout(this, viewModel.getCreditCardColorOptions())

        initComponents()

        setUpListeners()

    }

    private fun initComponents(){
        binding.actvColors.setAdapter(
            adapter
        )
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setUpListeners(){
        binding.addCreditCardConfigurationToolbar.setNavigationOnClickListener {
            finish()
        }

        binding.actvColors.setOnItemClickListener { _, _, position, _ ->
            val selected = adapter.getItem(position) ?: return@setOnItemClickListener

            //Create string with circle and color name
            val spannable = InputValueHandle.circleColorfulWithText(binding.actvColors, selected.backgroundColor, selected.backgroundColorNameRes)

            //Show credit card preview
            binding.cvCreditCardPreview.visibility = View.VISIBLE
            binding.llCreditCardPreview.setBackgroundColor(selected.backgroundColor)
            binding.tvCreditCardName.setTextColor(selected.textColor)
            binding.tvPaymentDate.setTextColor(selected.textColor)
            binding.tvPaymentDateTitle.setTextColor(selected.textColor)

            //Save credit card colors on viewModel
            viewModel.setCreditCardColors(selected.backgroundColorNameRes, selected.backgroundColor, selected.textColor)

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

                    //Check and define max possible day on input field
                    if(day != null && day > 31){
                        binding.etCreditCardExpirationDay.setText(31.toString())
                    }

                    //Check and define payment date simulation on credit card preview
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
                        binding.tvPaymentDate.text = formattedDate
                    }

                }
            }
        })

        binding.btCreditCardSave.setOnClickListener {
            it.isEnabled = false
            if(InputFieldFunctions.isFilled(
                this,
                binding.btCreditCardSave,
                binding.etCreditCardName,
                binding.etCreditCardExpirationDay,
                binding.etCreditCardClosingDay
            )){
                if(ConnectionFunctions.internetConnectionVerification(this)){
                    lifecycleScope.launch{
                        viewModel.addCreditCard(
                            binding.etCreditCardName.text.toString(),
                            binding.etCreditCardExpirationDay.text.toString().toInt(),
                            binding.etCreditCardClosingDay.text.toString().toInt(),
                            viewModel.getCreditCardColors()
                        )
                    }
                }else{
                    PersonalizedSnackBars.noInternetConnection(binding.btCreditCardSave, this).show()
                }
            }
            it.isEnabled = true
        }

        viewModel.addCreditCardResult.observe(this){ result ->
            if(result){
                UiFunctions.clearEditText(binding.etCreditCardName, binding.etCreditCardExpirationDay, binding.etCreditCardClosingDay, binding.actvColors)
                UiFunctions.hideKeyboard(this, binding.root)
                binding.cvCreditCardPreview.visibility = View.GONE
                binding.btCreditCardSave.visibility = View.GONE
                PersonalizedSnackBars.successMessage(binding.main, getString(R.string.add_credit_card_success_message)).show()
            }else{
                PersonalizedSnackBars.failureMessage(binding.main, getString(R.string.add_credit_card_fail_message)).show()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, bottomInset)
            insets
        }


    }



}
