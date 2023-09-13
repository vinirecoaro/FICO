package com.example.fico.ui

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.example.fico.databinding.ActivitySetDefaultBudgetBinding
import com.example.fico.ui.viewmodel.SetDefaultBudgetViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
                viewModel.setDefaultBudget(binding.etAvailablePerMonth.text.toString())
            }
        }
        binding.etAvailablePerMonth.setOnClickListener {
            binding.etAvailablePerMonth.setText("")
        }
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