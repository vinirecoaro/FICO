package com.example.fico.presentation.activities

import android.app.Dialog
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.fico.DataStoreManager
import com.example.fico.R
import com.example.fico.components.Dialogs
import com.example.fico.components.PersonalizedSnackBars
import com.example.fico.databinding.ActivityDefaultPaymentDateConfigurationBinding
import com.example.fico.presentation.viewmodel.DefaultPaymentDateConfigurationViewModel
import com.example.fico.utils.DateFunctions
import com.example.fico.utils.constants.StringConstants
import com.example.fico.utils.internet.ConnectionFunctions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class DefaultPaymentDateConfigurationActivity : AppCompatActivity() {

    private val binding by lazy{ActivityDefaultPaymentDateConfigurationBinding.inflate(layoutInflater)}
    private val viewModel : DefaultPaymentDateConfigurationViewModel by inject()
    private val sharedPref : SharedPreferences by inject()
    private val dataStore : DataStoreManager by inject()

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.defaultPaymentDateConfigurationToolbar.setTitle(getString(R.string.default_payment_date))
        binding.defaultPaymentDateConfigurationToolbar.setTitleTextColor(Color.WHITE)

        //Insert a back button on Navigation bar
        setSupportActionBar(binding.defaultPaymentDateConfigurationToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setUpListeners()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setUpListeners(){
        binding.defaultPaymentDateConfigurationToolbar.setNavigationOnClickListener {
            finish()
        }

        binding.llDefineDefaultDay.setOnClickListener {
            setDefaultPaymentDateAlertDialog()
        }

        viewModel.setDefaultPaymentDateLiveData.observe(this) { result ->
            if (result) {
                Snackbar.make(
                    binding.llDefineDefaultDay,
                    getString(R.string.set_default_payment_date_success_message),
                    Snackbar.LENGTH_LONG
                ).show()
            }else{
                Snackbar.make(
                    binding.llDefineDefaultDay,
                    getString(R.string.set_default_payment_date_fail_message),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

        viewModel.paymentDateSwitchInitialStateLiveData.observe(this){ state ->
            if(state != null){
                binding.swtPaymentDateState.isChecked = state
            }
        }

        binding.swtPaymentDateState.setOnCheckedChangeListener{ _ , state ->
            viewModel.setPaymentDateSwitchInitialState(state)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setDefaultPaymentDateAlertDialog(){
        lifecycleScope.launch(Dispatchers.Main){

            val paymentDay = dataStore.getDefaultPaymentDay()
            val daysForClosingBill = dataStore.getDaysForClosingBill()

            val currentInfo = if(paymentDay != null && daysForClosingBill != null){
                "Dados Atuais:\n\n${getString(R.string.expiration)} - $paymentDay\n${getString(R.string.days_for_closing)} - $daysForClosingBill"
            }else{
                getString(R.string.default_day_default_message)
            }

            val dialog = Dialogs.dialogModelFour(
                this@DefaultPaymentDateConfigurationActivity,
                this@DefaultPaymentDateConfigurationActivity,
                binding.tvDefineDefaultDay,
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
    }

    @RequiresApi(Build.VERSION_CODES.M)
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
                    binding.llDefineDefaultDay,
                    getString(R.string.invalid_day),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }else{
            PersonalizedSnackBars.noInternetConnection(binding.tvDefineDefaultDay, this@DefaultPaymentDateConfigurationActivity).show()
        }
    }

    private fun hasInternetConnection() : Boolean{
        return ConnectionFunctions().internetConnectionVerification(this)
    }
}