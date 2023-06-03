package com.example.fico.ui.viewmodel

import android.os.Build
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.example.fico.R
import com.example.fico.service.FirebaseAPI
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainViewModel : ViewModel() {

    private val firebaseAPI = FirebaseAPI.instance

    fun ShowHideValue(text: TextView){
        if (text.inputType == InputType.TYPE_TEXT_VARIATION_PASSWORD) {
            text.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            text.transformationMethod = null
            text.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_visibility_24, 0)
        }
        else{
            text.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
            text.transformationMethod = PasswordTransformationMethod()
            text.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_visibility_off_24, 0)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentYearMonth() : String{
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val formatedDate = currentDate.format(formatter)
        val month = formatedDate.toString().substring(3,5)
        val year = formatedDate.toString().substring(6,10)
        return "$year-$month"
    }

    fun returnTotalExpense(textView : TextView){
        firebaseAPI.returnTotalExpense(textView)
    }

    fun returnAvailableNow(textView : TextView, date: String){
        firebaseAPI.returnAvailableNow(textView, date)
    }

    fun returnMonthExpense(textView : TextView, date: String){
        firebaseAPI.returnMonthExpense(textView, date)
    }

}