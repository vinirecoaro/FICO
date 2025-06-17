package com.example.fico.presentation.activities

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.fico.R
import com.example.fico.databinding.ActivityCreditCardBinding
import com.example.fico.presentation.components.PersonalizedSnackBars
import com.example.fico.presentation.components.inputs.InputAdapters
import com.example.fico.presentation.components.inputs.InputFieldFunctions
import com.example.fico.presentation.components.inputs.InputValueHandle
import com.example.fico.model.CreditCard
import com.example.fico.model.CreditCardColors
import com.example.fico.presentation.components.dialogs.Dialogs
import com.example.fico.presentation.viewmodel.CreditCardViewModel
import com.example.fico.utils.UiFunctions
import com.example.fico.utils.constants.StringConstants
import com.example.fico.utils.internet.ConnectionFunctions
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class CreditCardActivity : AppCompatActivity() {

    private val binding by lazy{ ActivityCreditCardBinding.inflate(layoutInflater)}
    private val viewModel : CreditCardViewModel by inject()
    lateinit var adapter : ArrayAdapter<CreditCardColors>
    private lateinit var setCardAsDefaultOptionGlobal : MenuItem

    @RequiresApi(Build.VERSION_CODES.M)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val intent = intent
        if(intent != null){
            val activityMode = intent.getStringExtra(StringConstants.CREDIT_CARD_CONFIG.MODE)
            if (activityMode != null) {
                viewModel.setActivityMode(activityMode)
            }
            if(activityMode == StringConstants.GENERAL.EDIT_MODE){
                val creditCard = intent.getSerializableExtra(StringConstants.CREDIT_CARD_CONFIG.CREDIT_CARD) as? CreditCard
                if(creditCard != null){
                    val defaultCreditCardId = intent.getStringExtra(StringConstants.DATABASE.DEFAULT_CREDIT_CARD_ID)
                    viewModel.setDefaultCreditCardId(defaultCreditCardId!!)
                    if(defaultCreditCardId == creditCard.id){
                        binding.ivDefaultCardIcon.visibility = View.VISIBLE
                    }
                    viewModel.setEditingCreditCard(creditCard)
                    fillFieldsWithCreditCardInfo(creditCard)
                    showCreditCardPreview(creditCard)
                    changeComponentsToEditMode()
                }else{
                    setResult(StringConstants.RESULT_CODES.EDIT_CREDIT_CARD_RESULT_FAILURE)
                    finish()
                }
            }
        }

        //Insert a back button on Navigation bar
        setSupportActionBar(binding.addCreditCardToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = InputAdapters.colorAutoCompleteTextInputLayout(this, viewModel.getCreditCardColorOptions())

        initComponents()

        setUpListeners()

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.add_credit_card_menu, menu)
        return true
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete_credit_card -> {
                if(ConnectionFunctions.internetConnectionVerification(this)){
                    dialogDeleteCreditCard()
                }else{
                    PersonalizedSnackBars.noInternetConnection(binding.root, this).show()
                }
                return true
            }
            R.id.set_credit_card_as_default -> {
                if(ConnectionFunctions.internetConnectionVerification(this)){
                    dialogSetCardAsDefault()
                }else{
                    PersonalizedSnackBars.noInternetConnection(binding.root, this).show()
                }
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {

        if(viewModel.getActivityMode() == StringConstants.GENERAL.EDIT_MODE){
            if(menu != null){
                //Delete Icon
                val deleteCreditCardIcon = menu.findItem(R.id.delete_credit_card)
                deleteCreditCardIcon.isVisible = true

                //Set default credit card option
                val setCardAsDefaultOption = menu.findItem(R.id.set_credit_card_as_default)
                setCardAsDefaultOptionGlobal = setCardAsDefaultOption
                val editingCreditCard = viewModel.getEditingCreditCard()
                val defaultCreditCardId = viewModel.getDefaultCreditCardId()
                if(defaultCreditCardId != editingCreditCard.id){
                    setCardAsDefaultOption.isVisible = true
                }
            }
        }

        return super.onPrepareOptionsMenu(menu)
    }

    private fun initComponents(){
        binding.actvColors.setAdapter(
            adapter
        )
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setUpListeners(){
        binding.addCreditCardToolbar.setNavigationOnClickListener {
            finish()
        }

        binding.actvColors.setOnItemClickListener { _, _, position, _ ->
            val selected = adapter.getItem(position) ?: return@setOnItemClickListener

            //Create string with circle and color name
            val spannable = InputValueHandle.circleColorfulWithText(binding.actvColors, selected.background, selected.labelRes)

            //Show credit card preview
            binding.cvCreditCardPreview.visibility = View.VISIBLE
            binding.llCreditCardPreview.setBackgroundColor(selected.background)
            binding.tvCreditCardName.setTextColor(selected.text)
            binding.tvPaymentDate.setTextColor(selected.text)
            binding.tvPaymentDateTitle.setTextColor(selected.text)

            //Save credit card colors on viewModel
            viewModel.setCreditCardColors(selected)

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

                    if(day != null){
                        if(day > 31){
                            binding.etCreditCardExpirationDay.setText(31.toString())
                        }
                        binding.tvPaymentDate.text = paymentDatePreview(day)
                    }
                }
            }
        })

        binding.etCreditCardClosingDay.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                if (text.isNotEmpty()) {
                    var day = text.toIntOrNull()

                    if(day != null){
                        if(day > 31){
                            binding.etCreditCardClosingDay.setText(31.toString())
                        }
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
                            if(viewModel.getActivityMode() == StringConstants.GENERAL.ADD_MODE){
                                viewModel.addCreditCard(
                                    binding.etCreditCardName.text.toString(),
                                    binding.etCreditCardExpirationDay.text.toString().toInt(),
                                    binding.etCreditCardClosingDay.text.toString().toInt(),
                                    viewModel.getCreditCardColors()
                                )
                            }else if(viewModel.getActivityMode() == StringConstants.GENERAL.EDIT_MODE){
                                viewModel.editCreditCard(
                                    viewModel.getEditingCreditCard().id,
                                    binding.etCreditCardName.text.toString(),
                                    binding.etCreditCardExpirationDay.text.toString().toInt(),
                                    binding.etCreditCardClosingDay.text.toString().toInt(),
                                    viewModel.getCreditCardColors()
                                )
                            }
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

        viewModel.editCreditCardResult.observe(this){ result ->
            if(result){
                setResult(StringConstants.RESULT_CODES.EDIT_CREDIT_CARD_RESULT_OK)
                finish()
            }else{
                setResult(StringConstants.RESULT_CODES.EDIT_CREDIT_CARD_RESULT_FAILURE)
                finish()
            }
        }

        viewModel.deleteCreditCardResult.observe(this){ result ->
            if(result){
                setResult(StringConstants.RESULT_CODES.DELETE_CREDIT_CARD_RESULT_OK)
                finish()
            }else{
                setResult(StringConstants.RESULT_CODES.DELETE_CREDIT_CARD_RESULT_FAILURE)
                finish()
            }
        }

        viewModel.setCreditCardAsDefaultResult.observe(this){ result ->
            if(result){
                PersonalizedSnackBars.successMessage(binding.root, getString(R.string.set_default_credit_card_success_message)).show()
                changeComponentsToDefaultCreditCard()
            }else{
                PersonalizedSnackBars.failureMessage(binding.root, getString(R.string.set_default_credit_card_fail_message)).show()
            }
        }
    }

    private fun paymentDatePreview(day : Int) : String{

        var innerDay = day
        //Check and define payment date simulation on credit card preview
        val calendar = java.util.Calendar.getInstance()

        // Obtem o ano e mês atuais
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH) // 0-based

        // Define o dia no calendário, cuidando para não causar exceção
        val maxDayOfMonth = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
        if (innerDay in 1..maxDayOfMonth) {
            calendar.set(java.util.Calendar.DAY_OF_MONTH, innerDay)
            calendar.set(java.util.Calendar.MONTH, month)
            calendar.set(java.util.Calendar.YEAR, year)

            // Formata a data para exibição
            val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            val formattedDate = dateFormat.format(calendar.time)

            return formattedDate
        } else {
            innerDay = maxDayOfMonth
            calendar.set(java.util.Calendar.DAY_OF_MONTH, innerDay)
            calendar.set(java.util.Calendar.MONTH, month)
            calendar.set(java.util.Calendar.YEAR, year)

            // Formata a data para exibição
            val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
            val formattedDate = dateFormat.format(calendar.time)
            return formattedDate
        }
    }

    private fun showCreditCardPreview(creditCard : CreditCard){
        binding.cvCreditCardPreview.visibility = View.VISIBLE
        binding.tvCreditCardName.text = creditCard.nickName
        binding.llCreditCardPreview.setBackgroundColor(creditCard.colors.background)
        binding.tvCreditCardName.setTextColor(creditCard.colors.text)
        binding.tvPaymentDate.setTextColor(creditCard.colors.text)
        binding.tvPaymentDateTitle.setTextColor(creditCard.colors.text)
        binding.tvPaymentDate.text = paymentDatePreview(creditCard.expirationDay)
    }

    private fun fillFieldsWithCreditCardInfo(creditCard : CreditCard){
        binding.etCreditCardName.setText(creditCard.nickName)
        binding.etCreditCardExpirationDay.setText(creditCard.expirationDay.toString())
        binding.etCreditCardClosingDay.setText(creditCard.closingDay.toString())
        viewModel.setCreditCardColors(creditCard.colors)
        val spannable = InputValueHandle.circleColorfulWithText(binding.actvColors, creditCard.colors.background, creditCard.colors.labelRes)
        binding.actvColors.setText(spannable, false)
    }

    private fun changeComponentsToEditMode(){
        binding.addCreditCardToolbar.title = getString(R.string.edit_credit_card_actv_title)
        binding.btCreditCardSave.setText(R.string.save)
        binding.btCreditCardSave.visibility = View.VISIBLE
    }

    private fun dialogDeleteCreditCard(){
        val dialog = Dialogs.dialogModelOne(
            this,
            this,
            getString(R.string.delete_credit_card),
            getString(R.string.delete_credit_card_dialog_message),
            getString(R.string.confirm)
        ){
            lifecycleScope.launch {
                val creditCard = viewModel.getEditingCreditCard()
                viewModel.deleteCreditCard(creditCard)
            }
        }
        dialog.show()
    }

    private fun dialogSetCardAsDefault(){
        val dialog = Dialogs.dialogModelOne(
            this,
            this,
            getString(R.string.default_card),
            getString(R.string.set_default_credit_card_message),
            getString(R.string.confirm)
        ){
            lifecycleScope.launch {
                val creditCard = viewModel.getEditingCreditCard()
                viewModel.setCreditCardAsDefault(creditCard.id)
            }
        }
        dialog.show()
    }

    private fun changeComponentsToDefaultCreditCard(){
        setCardAsDefaultOptionGlobal.isVisible = false
        binding.ivDefaultCardIcon.visibility = View.VISIBLE
    }

}
