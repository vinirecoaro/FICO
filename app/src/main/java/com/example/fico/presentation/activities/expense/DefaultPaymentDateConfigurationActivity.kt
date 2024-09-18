package com.example.fico.presentation.activities.expense

import android.app.Dialog
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.fico.R
import com.example.fico.databinding.ActivityDefaultPaymentDateConfigurationBinding
import com.example.fico.presentation.viewmodel.DefaultPaymentDateConfigurationViewModel
import com.example.fico.shared.constants.StringConstants
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import org.koin.android.ext.android.inject

class DefaultPaymentDateConfigurationActivity : AppCompatActivity() {

    private val binding by lazy{ActivityDefaultPaymentDateConfigurationBinding.inflate(layoutInflater)}
    private val viewModel : DefaultPaymentDateConfigurationViewModel by inject()
    private val sharedPref : SharedPreferences by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.defaultPaymentDateConfigurationToolbar.setTitle(getString(R.string.default_payment_date))

        //Insert a back button on Navigation bar
        setSupportActionBar(binding.defaultPaymentDateConfigurationToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setUpListeners()
    }

    private fun setUpListeners(){
        binding.defaultPaymentDateConfigurationToolbar.setNavigationOnClickListener {
            finish()
        }
        binding.llDefineDefaultDay.setOnClickListener {
            setDefaultPaymentDateAlertDialog()
        }

        viewModel.setDefaultBudgetLiveData.observe(this) { result ->
            if (result) {
                Snackbar.make(
                    binding.llDefineDefaultDay,
                    getString(R.string.payment_day_definition_success_message),
                    Snackbar.LENGTH_LONG
                ).show()
            }else{
                Snackbar.make(
                    binding.llDefineDefaultDay,
                    getString(R.string.payment_day_definition_fail_message),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setDefaultPaymentDateAlertDialog(){
        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle(getString(R.string.default_payment_date))

        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.select_date_for_alert_dialog, null)

        val etDate = dialogView.findViewById<TextInputEditText>(R.id.et_payment_day_ad)
        val tvPaymentDay = dialogView.findViewById<TextView>(R.id.tv_payment_day_al)
        val paymentDay = sharedPref.getString(StringConstants.DATABASE.PAYMENT_DAY, null)

        if(paymentDay != null){
            val text = "${getString(R.string.default_day)} $paymentDay"
            tvPaymentDay.text = text
        }else{
            val text = getString(R.string.default_day_default_message)
            tvPaymentDay.text = text
        }

        builder.setView(dialogView)

        builder.setPositiveButton(getString(R.string.save)){dialog, which ->

            if(etDate.text.isNullOrEmpty()){
                Snackbar.make(binding.llDefineDefaultDay, getString(R.string.type_the_day), Snackbar.LENGTH_LONG).show()
            }else if (etDate.text.toString().toInt() > 31 ||etDate.text.toString().toInt() <= 0){
                Snackbar.make(binding.llDefineDefaultDay, getString(R.string.invalid_day), Snackbar.LENGTH_LONG).show()
            }else{
                with(sharedPref.edit()){
                    putString(StringConstants.DATABASE.PAYMENT_DAY, etDate.text.toString())
                    commit()
                }
                viewModel.setDefaultPaymentDay(etDate.text.toString())
            }
        }
        val dialog = builder.create()

        dialog.setOnShowListener {
            dialog.getButton(Dialog.BUTTON_POSITIVE).setTextColor(getAlertDialogTextButtonColor())
        }

        dialog.show()
    }

    private fun getAlertDialogTextButtonColor() : Int{
        val typedValue = TypedValue()
        val theme: Resources.Theme = this.theme
        theme.resolveAttribute(R.attr.alertDialogTextButtonColor, typedValue, true)
        val colorOnSurfaceVariant = ContextCompat.getColor(this, typedValue.resourceId)
        return colorOnSurfaceVariant
    }
}