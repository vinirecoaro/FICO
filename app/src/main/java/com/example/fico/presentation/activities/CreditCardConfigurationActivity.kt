package com.example.fico.presentation.activities

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.fico.R
import com.example.fico.presentation.compose.components.ComposeDialogs
import com.example.fico.databinding.ActivityCreditCardConfigurationBinding
import com.example.fico.model.CreditCard
import com.example.fico.model.CreditCardColors
import com.example.fico.presentation.viewmodel.CreditCardConfigurationViewModel
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject

class CreditCardConfigurationActivity : AppCompatActivity() {

    private val binding by lazy{ActivityCreditCardConfigurationBinding.inflate(layoutInflater)}
    private val viewModel : CreditCardConfigurationViewModel by inject()

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
            val mockCreditCards = listOf(
                CreditCard(
                    id = "1",
                    nickName = "Nubank",
                    expirationDay = 20,
                    closingDay = 10,
                    colors = CreditCardColors(
                        backgroundColorNameRes = android.R.color.holo_blue_dark,
                        backgroundColor = Color.rgb(100, 0, 0),
                        textColor = 0xFFFFFFFF.toInt()
                    )
                ),
                CreditCard(
                    id = "2",
                    nickName = "Bradescofhgfdhgfhfghgfhgfgfhfg",
                    expirationDay = 20,
                    closingDay = 5,
                    colors = CreditCardColors(
                        backgroundColorNameRes = android.R.color.holo_red_dark,
                        backgroundColor = Color.rgb(0, 0, 100),
                        textColor = 0xFFFFFFFF.toInt()
                    )
                ),
                CreditCard(
                    id = "3",
                    nickName = "C6 Bank",
                    expirationDay = 20,
                    closingDay = 15,
                    colors = CreditCardColors(
                        backgroundColorNameRes = android.R.color.holo_green_dark,
                        backgroundColor = Color.rgb(255, 152, 0),
                        textColor = 0xFFFFFFFF.toInt()
                    )
                )
            )



            ComposeDialogs.showComposeDialog(
                composeView = binding.composeDialogHost,
                items = mockCreditCards,
                contextView = binding.root
            ) { selected ->
                Toast.makeText(this, "Selecionado: $selected", Toast.LENGTH_SHORT).show()
            }
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

        viewModel.payWithCreditCardSwitchInitialState.observe(this){ state ->
            if(state != null){
                binding.swtUseCreditCardAsDefault.isChecked = state
            }
        }

        binding.swtUseCreditCardAsDefault.setOnCheckedChangeListener{ _ , state ->
            viewModel.setPaymentDateSwitchInitialState(state)
        }
    }


}