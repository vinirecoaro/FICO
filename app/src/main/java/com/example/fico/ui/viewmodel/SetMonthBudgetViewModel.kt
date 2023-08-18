package com.example.fico.ui.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.example.fico.model.Expense
import com.example.fico.service.FirebaseAPI
import java.time.LocalTime

class SetMonthBudgetViewModel : ViewModel() {

    private val firebaseAPI = FirebaseAPI.instance

    fun setUpBudget(budget: String, date: String){
        firebaseAPI.setUpBudget(budget, date)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addExpense(price: String, description: String, category: String, date: String){
        val expense = Expense(price, description, category, date)
        val timeNow = LocalTime.now()
        val inputTime = "${timeNow.hour}-${timeNow.minute}-${timeNow.second}"
        firebaseAPI.addExpense(expense, inputTime)
    }

}