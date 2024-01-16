package com.example.fico.ui.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.model.Expense
import com.example.fico.api.FirebaseAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.time.LocalTime

class SetMonthBudgetViewModel : ViewModel() {

    private val firebaseAPI = FirebaseAPI.instance

    fun setUpBudget(budget: String, date: String){
        viewModelScope.async (Dispatchers.IO){
            firebaseAPI.setUpBudget(budget, date)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun addExpense(price: String, description: String, category: String, date: String)
    = viewModelScope.async{
        val expense = Expense("",price, description, category, date)
        val timeNow = LocalTime.now()
        var hour = timeNow.hour.toString()
        var minute = timeNow.minute.toString()
        var second = timeNow.second.toString()
        if(timeNow.hour < 10){
            hour = "0${timeNow.hour}"
        }
        if(timeNow.minute < 10){
            minute = "0${timeNow.minute}"
        }
        if(timeNow.second < 10){
            second = "0${timeNow.second}"
        }
        val inputTime = "${hour}-${minute}-${second}"
        firebaseAPI.addExpense2(expense, inputTime)
    }

}