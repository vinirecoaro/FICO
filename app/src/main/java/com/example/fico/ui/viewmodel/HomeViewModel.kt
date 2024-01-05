package com.example.fico.ui.viewmodel

import android.os.Build
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.R
import com.example.fico.service.FirebaseAPI
import com.google.firebase.inject.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.CompletableFuture

class HomeViewModel : ViewModel() {

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

    @RequiresApi(Build.VERSION_CODES.N)
    fun getAvailableNow(date: String, formatted : Boolean = true): kotlinx.coroutines.Deferred<String> {
        return viewModelScope.async(Dispatchers.IO) {
            val availableNow = firebaseAPI.getAvailableNow(date)
            if(availableNow == "---"){
                availableNow
            }else{
                if (formatted){
                    val availableNowFormatted = (NumberFormat.getCurrencyInstance().format(availableNow.toFloat()))
                    availableNowFormatted
                }else{
                    availableNow
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getMonthExpense(date: String, formatted : Boolean = true): kotlinx.coroutines.Deferred<String> {
        return viewModelScope.async(Dispatchers.IO) {
            val monthExpense = firebaseAPI.getMonthExpense(date)
            if(monthExpense == "---"){
                monthExpense
            }else{
                if (formatted){
                    val monthExpenseFormatted = (NumberFormat.getCurrencyInstance().format(monthExpense.toFloat()))
                    monthExpenseFormatted
                }else{
                    monthExpense
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getTotalExpense(): kotlinx.coroutines.Deferred<String> {
        return viewModelScope.async(Dispatchers.IO){
            val totalExpense = firebaseAPI.getTotalExpense().await().toFloat()
            val priceFormatted = (NumberFormat.getCurrencyInstance().format(totalExpense))
            priceFormatted.toString()}
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentlyDate() : String{
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        var formattedDate = currentDate.format(formatter)
        val month = formattedDate.substring(3, 5)
        val year = formattedDate.substring(6, 10)
        val date = "$year-$month"
        return date
    }

}