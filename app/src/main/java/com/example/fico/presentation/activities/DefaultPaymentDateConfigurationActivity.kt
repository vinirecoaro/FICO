package com.example.fico.presentation.activities

import android.app.Dialog
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Bundle
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
            val builder = MaterialAlertDialogBuilder(this@DefaultPaymentDateConfigurationActivity)
            builder.setTitle(getString(R.string.default_payment_day))

            val inflater = LayoutInflater.from(this@DefaultPaymentDateConfigurationActivity)
            val dialogView = inflater.inflate(R.layout.select_date_for_alert_dialog, null)

            val etExpirationDay = dialogView.findViewById<TextInputEditText>(R.id.et_expiration_day_default_payment_date_dialog)
            val etDaysForClosing = dialogView.findViewById<TextInputEditText>(R.id.et_days_for_closing_default_payment_date_dialog)
            val tvPaymentDay = dialogView.findViewById<TextView>(R.id.tv_payment_day_al)
            val paymentDay = dataStore.getDefaultPaymentDay()
            val daysForClosingBill = dataStore.getDaysForClosingBill()

            if(paymentDay != null && daysForClosingBill != null){
                val text = "Dados Atuais:\n\n${getString(R.string.expiration)} - $paymentDay\n${getString(R.string.days_for_closing)} - $daysForClosingBill"
                tvPaymentDay.text = text
            }else{
                val text = getString(R.string.default_day_default_message)
                tvPaymentDay.text = text
            }

            builder.setView(dialogView)

            builder.setPositiveButton(getString(R.string.save)){dialog, which ->

                if(hasInternetConnection()){
                    if (verifyFields(
                            etExpirationDay,
                            etDaysForClosing
                        )
                    ) {
                        if (DateFunctions().isValidMonthDay(
                                etExpirationDay.text.toString().toInt()
                            )
                        ) {
                            with(sharedPref.edit()) {
                                putString(
                                    StringConstants.DATABASE.PAYMENT_DAY,
                                    etExpirationDay.text.toString()
                                )
                                putString(
                                    StringConstants.DATABASE.DAYS_FOR_CLOSING_BILL,
                                    etDaysForClosing.text.toString()
                                )
                                commit()
                            }
                            viewModel.setDefaultPaymentDate(
                                etExpirationDay.text.toString(),
                                etDaysForClosing.text.toString()
                            )
                        } else {
                            Snackbar.make(
                                binding.llDefineDefaultDay,
                                getString(R.string.invalid_day),
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    }
                }else{
                    noInternetConnectionSnackBar()
                }
            }
            val dialog = builder.create()

            dialog.setOnShowListener {
                dialog.getButton(Dialog.BUTTON_POSITIVE).setTextColor(getAlertDialogTextButtonColor())
            }

            dialog.show()
        }
    }

    private fun getAlertDialogTextButtonColor() : Int{
        val typedValue = TypedValue()
        val theme: Resources.Theme = this.theme
        theme.resolveAttribute(R.attr.alertDialogTextButtonColor, typedValue, true)
        val colorOnSurfaceVariant = ContextCompat.getColor(this, typedValue.resourceId)
        return colorOnSurfaceVariant
    }

    private fun verifyFields(vararg text: EditText): Boolean {
        for (i in text) {
            if (i.text.toString() == "" || i == null) {
                Snackbar.make(
                    binding.tvDefineDefaultDay, "Preencher o campo ${i.hint}", Snackbar.LENGTH_LONG
                ).show()
                return false
            }
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun noInternetConnectionSnackBar(){
        Snackbar.make(
            binding.defaultPaymentDateConfigurationToolbar,
            getString(R.string.without_network_connection),
            Snackbar.LENGTH_LONG
        )
            .setBackgroundTint(resources.getColor(android.R.color.holo_red_dark, theme))
            .setActionTextColor(resources.getColor(android.R.color.white, theme))
            .show()
    }

    private fun hasInternetConnection() : Boolean{
        return ConnectionFunctions().internetConnectionVerification(this)
    }
}