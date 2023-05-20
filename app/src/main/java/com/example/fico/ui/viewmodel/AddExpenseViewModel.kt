package com.example.fico.ui.viewmodel

import android.app.Instrumentation
import android.os.Build
import android.os.SystemClock
import android.view.MotionEvent
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.example.fico.model.Expense
import com.example.fico.service.FirebaseAPI
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AddExpenseViewModel : ViewModel() {

    private val firebaseAPI = FirebaseAPI.instance

    fun addExpense(price: String, description: String, category: String, date: String){
        val expense = Expense(price, description, category, date)
        firebaseAPI.addExpense(expense)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentlyDate() : String{
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return currentDate.format(formatter)
    }


}