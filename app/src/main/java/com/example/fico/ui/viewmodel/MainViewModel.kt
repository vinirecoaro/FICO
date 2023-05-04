package com.example.fico.ui.viewmodel

import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.widget.TextView
import androidx.lifecycle.ViewModel
import com.example.fico.R

class MainViewModel : ViewModel() {

    fun ShowHideValue(text: TextView){
        if (text.inputType == InputType.TYPE_TEXT_VARIATION_PASSWORD) {
            text.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            text.transformationMethod = null
            text.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_visibility_24, 0)
        }
        //if(binding.tvTotalExpensesValue.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD){
        else{
            text.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
            text.transformationMethod = PasswordTransformationMethod()
            text.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_visibility_off_24, 0)
        }
    }
}