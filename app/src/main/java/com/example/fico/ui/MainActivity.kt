package com.example.fico.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import com.example.fico.R
import com.example.fico.databinding.ActivityAddExpenseBinding
import com.example.fico.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setUpListeners()
    }

    private fun setUpListeners(){
        binding.btAddExpenses.setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }
        binding.tvTotalExpensesValue.setOnClickListener {
            if (binding.tvTotalExpensesValue.inputType == InputType.TYPE_TEXT_VARIATION_PASSWORD) {
                binding.tvTotalExpensesValue.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.tvTotalExpensesValue.transformationMethod = null
                binding.tvTotalExpensesValue.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_visibility_24, 0)
            }
            //if(binding.tvTotalExpensesValue.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD){
            else{
                binding.tvTotalExpensesValue.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.tvTotalExpensesValue.transformationMethod = PasswordTransformationMethod()
                binding.tvTotalExpensesValue.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_visibility_off_24, 0)
            }
        }
    }

}