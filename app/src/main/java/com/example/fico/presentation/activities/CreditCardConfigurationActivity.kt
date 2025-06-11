package com.example.fico.presentation.activities

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.fico.R
import com.example.fico.presentation.compose.components.ComposeDialogs
import com.example.fico.databinding.ActivityCreditCardConfigurationBinding
import com.example.fico.model.CreditCard
import com.example.fico.model.CreditCardColors
import com.example.fico.presentation.components.PersonalizedSnackBars
import com.example.fico.presentation.viewmodel.CreditCardConfigurationViewModel
import com.example.fico.utils.constants.StringConstants
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class CreditCardConfigurationActivity : AppCompatActivity() {

    private val binding by lazy{ActivityCreditCardConfigurationBinding.inflate(layoutInflater)}
    private val viewModel : CreditCardConfigurationViewModel by inject()
    private val startAddCreditCardActivityForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){result ->
        when(result.resultCode){
            StringConstants.RESULT_CODES.EDIT_CREDIT_CARD_RESULT_OK -> {
                PersonalizedSnackBars.successMessage(binding.root, getString(R.string.edit_credit_card_success_message)).show()
            }
            StringConstants.RESULT_CODES.EDIT_CREDIT_CARD_RESULT_FAILURE -> {
                PersonalizedSnackBars.failureMessage(binding.root, getString(R.string.edit_credit_card_fail_message)).show()
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.creditCardConfigurationToolbar.setTitle(getString(R.string.credit_card))
        binding.creditCardConfigurationToolbar.setTitleTextColor(Color.WHITE)

        //Insert a back button on Navigation bar
        setSupportActionBar(binding.creditCardConfigurationToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setUpListeners()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setUpListeners(){
        binding.creditCardConfigurationToolbar.setNavigationOnClickListener {
            finish()
        }

        binding.llRegisterCreditCard.setOnClickListener {
            startActivity(Intent(this, AddCreditCardActivity::class.java))
        }

        binding.llCreditCardList.setOnClickListener {
            viewModel.getCreditCardList()
        }

        viewModel.getCreditCardList.observe(this){ creditCardList ->
            if(creditCardList != null){
                ComposeDialogs.showComposeDialog(
                    composeView = binding.composeDialogHost,
                    items = creditCardList,
                    contextView = binding.root
                ) { selected ->
                    editCreditCard(selected)
                }
            }
        }

        viewModel.payWithCreditCardSwitchInitialState.observe(this){ state ->
            if(state != null){
                binding.swtUseCreditCardAsDefault.isChecked = state
            }
        }

        binding.swtUseCreditCardAsDefault.setOnCheckedChangeListener{ _ , state ->
            viewModel.setPaymentDateSwitchInitialState(state)
        }
    }

    private fun editCreditCard(creditCard : CreditCard){
        val intent = Intent(this, AddCreditCardActivity::class.java)
            .putExtra(StringConstants.CREDIT_CARD_CONFIG.CREDIT_CARD, creditCard)
            .putExtra(StringConstants.CREDIT_CARD_CONFIG.MODE, StringConstants.GENERAL.EDIT_MODE)
        startAddCreditCardActivityForResult.launch(intent)
    }

}