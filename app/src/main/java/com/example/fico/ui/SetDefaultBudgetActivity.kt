package com.example.fico.ui

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
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
                if(viewModel.setDefaultBudget(binding.etAvailablePerMonth.text.toString()).await()){
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