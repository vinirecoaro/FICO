package com.example.fico.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.fico.databinding.ActivityAddExpenseBinding

class AddExpenseActivity : AppCompatActivity() {

    private val binding by lazy { ActivityAddExpenseBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

   /* private fun formatValue(){
        private val inputMask = MaskedTextChangedListener.installOn(
            binding.etValue,
            "[R$] [9]{1,3}.[9]{2}",
            object : MaskedTextChangedListener.ValueListener {
                override fun onTextChanged(maskedText: String, extractedValue: String) {
                    // Faça algo com o valor formatado aqui
                }
            }
        )
        inputMask.placeholder = "R$ 0.00"
    }*/
}