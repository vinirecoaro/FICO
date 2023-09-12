package com.example.fico.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.example.fico.databinding.ActivitySetDefaultBudgetBinding
import com.example.fico.ui.viewmodel.SetDefaultBudgetViewModel
import com.google.android.material.snackbar.Snackbar

class SetDefaultBudgetActivity : AppCompatActivity() {

    private val binding by lazy{ActivitySetDefaultBudgetBinding.inflate(layoutInflater)}
    private val viewModel by viewModels<SetDefaultBudgetViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setUpListeners()
    }

    private fun setUpListeners(){
        binding.ivInfoMoney.setOnClickListener {
            val snackbar = Snackbar.make(it, "Definir qual será o limite de gasto para o mês atual e os meses seguintes", Snackbar.LENGTH_LONG)
            snackbar.show()
        }
    }
}