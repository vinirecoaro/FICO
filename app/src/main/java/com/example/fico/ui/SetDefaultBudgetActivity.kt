package com.example.fico.ui

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.example.fico.databinding.ActivitySetDefaultBudgetBinding
import com.example.fico.ui.viewmodel.SetDefaultBudgetViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.NumberFormat

class SetDefaultBudgetActivity : AppCompatActivity() {

    private val binding by lazy{ActivitySetDefaultBudgetBinding.inflate(layoutInflater)}
    private val viewModel by viewModels<SetDefaultBudgetViewModel>()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setUpListeners()
        getDefaultBudget()
    }

    private fun setUpListeners(){
        binding.ivInfoMoney.setOnClickListener {
            val snackbar = Snackbar.make(it, "Definir qual será o limite de gasto para o mês atual e os meses seguintes", Snackbar.LENGTH_LONG)
            snackbar.show()
        }
        binding.btSave.setOnClickListener {
            lifecycleScope.launch {

                val regex = Regex("[\\d,.]+")
                val justNumber = regex.find(binding.etAvailablePerMonth.text.toString())
                val formatNum = DecimalFormat("#.##")
                val numClean = justNumber!!.value.replace(",","").replace(".","").toFloat()
                val formatedNum = formatNum.format(numClean/100)
                val formattedNumString = formatedNum.toString().replace(",",".")

                if(viewModel.setDefaultBudget(formattedNumString).await() && binding.etAvailablePerMonth.text.toString() != ""){
                    val snackbar = Snackbar.make(it, "Default Budget definido com sucesso",Snackbar.LENGTH_LONG)
                    snackbar.show()
                    Handler().postDelayed({
                        finish()
                    }, 1300)
                }else{
                    val snackbar = Snackbar.make(it, "Falha ao definir o Default Budget",Snackbar.LENGTH_LONG)
                    snackbar.show()
                    Handler().postDelayed({
                        finish()
                    }, 1300)
                }
            }

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

    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun getDefaultBudget() = lifecycleScope.launch(Dispatchers.Main){
        val existBudget = viewModel.checkIfExistDefaultBudget().await()
        if(existBudget){
            val defaultBudget = viewModel.getDefaultBudget().await()
            binding.etAvailablePerMonth.setText(defaultBudget)
        }
    }

}