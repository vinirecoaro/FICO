package com.example.fico.ui.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.model.Expense
import com.example.fico.service.FirebaseAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.time.LocalTime

class EditExpenseViewModel : ViewModel() {

    val firebaseAPI = FirebaseAPI.instance

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun saveEditExpense(expense:Expense, price: String, description: String, category: String, date: String) =
        viewModelScope.async(Dispatchers.IO){
        val oldExpense = expense
        val newExpense = Expense(id = "", price, description, category, date)
        val timeNow = LocalTime.now()
        val inputTime = "${timeNow.hour}-${timeNow.minute}-${timeNow.second}"
        firebaseAPI.editExpense(oldExpense,newExpense,inputTime)
    }
}