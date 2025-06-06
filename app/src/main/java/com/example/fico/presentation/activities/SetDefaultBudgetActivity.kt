package com.example.fico.presentation.activities

import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.example.fico.R
import com.example.fico.components.PersonalizedSnackBars
import com.example.fico.databinding.ActivitySetDefaultBudgetBinding
import com.example.fico.presentation.viewmodel.SetDefaultBudgetViewModel
import com.example.fico.utils.internet.ConnectionFunctions
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.text.DecimalFormat
import java.text.NumberFormat

class SetDefaultBudgetActivity : AppCompatActivity() {

    private val binding by lazy{ActivitySetDefaultBudgetBinding.inflate(layoutInflater)}
    private val viewModel : SetDefaultBudgetViewModel by inject()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.setDefaultBudgetToolbar.title = getString(R.string.default_budget_activity_title)
        binding.setDefaultBudgetToolbar.setTitleTextColor(Color.WHITE)

        //Insert a back button on Navigation bar
        setSupportActionBar(binding.setDefaultBudgetToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setUpListeners()
        viewModel.getDefaultBudget()
        setColorBasedOnTheme()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setUpListeners(){
        binding.ivInfoMoney.setOnClickListener {
            val snackbar = Snackbar.make(it, getString(R.string.default_budget_tip_message), Snackbar.LENGTH_LONG)
            snackbar.show()
        }

        binding.btSave.setOnClickListener {
            binding.btSave.isEnabled = false

            if(hasInternetConnection()){
                lifecycleScope.launch {
                    val regex = Regex("[\\d,.]+")
                    val justNumber = regex.find(binding.etAvailablePerMonth.text.toString())
                    val formatNum = DecimalFormat("#.##")
                    val numClean = justNumber!!.value.replace(",","").replace(".","").toFloat()
                    val formatedNum = formatNum.format(numClean/100).replace(",",".")
                    val formattedNumString = formatedNum

                    if(binding.etAvailablePerMonth.text.toString() != ""){
                        viewModel.setDefaultBudget(formattedNumString)
                    }
                }
            }else{
                PersonalizedSnackBars.noInternetConnection(binding.btSave, this).show()
            }
            binding.btSave.isEnabled = true
        }

        binding.etAvailablePerMonth.onFocusChangeListener = View.OnFocusChangeListener{ _, hasFocus ->
            if(hasFocus){
                binding.etAvailablePerMonth.setText("")
            }
        }

        binding.etAvailablePerMonth.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                if (!text.isEmpty()) {
                    val parsed = text.replace("[^\\d]".toRegex(), "").toLong()
                    val formatted = (NumberFormat.getCurrencyInstance().format(parsed / 100.0))
                    binding.etAvailablePerMonth.removeTextChangedListener(this)
                    binding.etAvailablePerMonth.setText(formatted)
                    binding.etAvailablePerMonth.setSelection(formatted.length)
                    binding.etAvailablePerMonth.addTextChangedListener(this)
                }
            }
        })

        binding.setDefaultBudgetToolbar.setNavigationOnClickListener {
            finish()
        }

        viewModel.getDefaultBudgetResult.observe(this){ defaultBudget ->
            if(defaultBudget != null){
                binding.etAvailablePerMonth.setText(defaultBudget)
            }
        }

        viewModel.setDefaultBudgetResult.observe(this){ result ->
            if(result){
                viewModel.getDefaultBudget()
                Snackbar.make(binding.btSave, getString(R.string.change_default_budget_success_message),Snackbar.LENGTH_LONG).show()
            }
            else{
                Snackbar.make(binding.btSave, getString(R.string.change_default_budget_failure_message),Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun setColorBasedOnTheme(){
        when (this.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.ivInfoMoney.setImageResource(R.drawable.baseline_info_light)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                binding.ivInfoMoney.setImageResource(R.drawable.baseline_info_dark)
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }
    }

    private fun hasInternetConnection() : Boolean{
        return ConnectionFunctions.internetConnectionVerification(this)
    }

}