package com.example.fico.presentation.activities

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.InputType
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.fico.DataStoreManager
import com.example.fico.R
import com.example.fico.components.dialogs.Dialogs
import com.example.fico.components.PersonalizedSnackBars
import com.example.fico.databinding.ActivityCreditCardConfigurationBinding
import com.example.fico.presentation.viewmodel.CreditCardConfigurationViewModel
import com.example.fico.utils.DateFunctions
import com.example.fico.utils.constants.StringConstants
import com.example.fico.utils.internet.ConnectionFunctions
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class CreditCardConfigurationActivity : AppCompatActivity() {

    private val binding by lazy{ActivityCreditCardConfigurationBinding.inflate(layoutInflater)}
    private val viewModel : CreditCardConfigurationViewModel by inject()
    private val sharedPref : SharedPreferences by inject()
    private val dataStore : DataStoreManager by inject()

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

        viewModel.setDefaultPaymentDateLiveData.observe(this) { result ->
            if (result) {
                Snackbar.make(
                    binding.llRegisterCreditCard,
                    getString(R.string.set_default_payment_date_success_message),
                    Snackbar.LENGTH_LONG
                ).show()
            }else{
                Snackbar.make(
                    binding.llRegisterCreditCard,
                    getString(R.string.set_default_payment_date_fail_message),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

        viewModel.paymentDateSwitchInitialStateLiveData.observe(this){ state ->
            if(state != null){
                binding.swtUseCreditCardAsDefault.isChecked = state
            }
        }

        binding.swtUseCreditCardAsDefault.setOnCheckedChangeListener{ _ , state ->
            viewModel.setPaymentDateSwitchInitialState(state)
        }
    }

    /*@RequiresApi(Build.VERSION_CODES.M)
    private fun setDefaultPaymentDateAlertDialog(){
        lifecycleScope.launch(Dispatchers.Main){

            val paymentDay = dataStore.getDefaultPaymentDay()
            val daysForClosingBill = dataStore.getDaysForClosingBill()

            val currentInfo = if(paymentDay != null && daysForClosingBill != null){
                "${getString(R.string.current_data)}:\n\n${getString(R.string.expiration)} - $paymentDay\n${getString(R.string.days_for_closing)} - $daysForClosingBill"
            }else{
                getString(R.string.default_day_default_message)
            }

            val dialog = Dialogs.dialogModelFour(
                this@CreditCardConfigurationActivity,
                this@CreditCardConfigurationActivity,
                binding.tvRegisterCreditCard,
                getString(R.string.default_payment_day),
                getString(R.string.expiration_day),
                InputType.TYPE_CLASS_NUMBER,
                getString(R.string.days_for_closing),
                InputType.TYPE_CLASS_NUMBER,
                getString(R.string.payment_day_info_message),
                currentInfo,
                getString(R.string.save),
                function = ::setDefaultPaymentDate
            )
            
            dialog.show()
        }
    }*/

    /*@RequiresApi(Build.VERSION_CODES.M)
    private fun setDefaultPaymentDate(expirationDay : String, daysForClosingBill : String){
        if(hasInternetConnection()){
            if (DateFunctions().isValidMonthDay(
                    expirationDay.toInt()
                )
            ) {
                with(sharedPref.edit()) {
                    putString(
                        StringConstants.DATABASE.PAYMENT_DAY,
                        expirationDay
                    )
                    putString(
                        StringConstants.DATABASE.DAYS_FOR_CLOSING_BILL,
                        daysForClosingBill
                    )
                    commit()
                }
                viewModel.setDefaultPaymentDate(
                    expirationDay,
                    daysForClosingBill
                )
            } else {
                Snackbar.make(
                    binding.llRegisterCreditCard,
                    getString(R.string.invalid_day),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }else{
            PersonalizedSnackBars.noInternetConnection(binding.tvRegisterCreditCard, this@CreditCardConfigurationActivity).show()
        }
    }*/

    private fun hasInternetConnection() : Boolean{
        return ConnectionFunctions.internetConnectionVerification(this)
    }
}