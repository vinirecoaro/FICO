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

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentYearMonth() : String{
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val formatedDate = currentDate.format(formatter)
        val month = formatedDate.toString().substring(3,5)
        val year = formatedDate.toString().substring(6,10)
        return "$year-$month"
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getAvailableNow(date: String): kotlinx.coroutines.Deferred<String> {
        return viewModelScope.async(Dispatchers.IO) {
            firebaseAPI.getAvailableNow(date)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getMonthExpense(date: String): kotlinx.coroutines.Deferred<String> {
        return viewModelScope.async(Dispatchers.IO) {
            firebaseAPI.getMonthExpense(date)
        }
    }


    @RequiresApi(Build.VERSION_CODES.N)
    fun getTotalExpense(): kotlinx.coroutines.Deferred<String> {
        return viewModelScope.async(Dispatchers.IO){
            firebaseAPI.getTotalExpense() }
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