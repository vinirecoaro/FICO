package com.example.fico.presentation.activities

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.fico.R
import com.example.fico.presentation.compose.components.ComposeDialogs
import com.example.fico.databinding.ActivityCreditCardConfigurationBinding
import com.example.fico.model.CreditCard
import com.example.fico.model.CreditCardColors
import com.example.fico.presentation.viewmodel.CreditCardConfigurationViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
            viewModel.getCreditCardList()
        }

        viewModel.getCreditCardList.observe(this){ creditCardList ->
            if(creditCardList != null){
                ComposeDialogs.showComposeDialog(
                    composeView = binding.composeDialogHost,
                    items = creditCardList,
                    contextView = binding.root
                ) { selected ->
                    //TODO
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


}