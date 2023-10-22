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
    suspend fun saveEditExpense(expense:Expense, price: String, description: String, category: String, date: String, installmentExpense : Boolean = false, nOfInstallments: Int = 1) =
        viewModelScope.async(Dispatchers.IO){
        val day = expense.date.substring(0, 2)
        val month = expense.date.substring(3, 5)
        val year = expense.date.substring(6, 10)
        val modifiedDate = "$year-$month-$day"
        val expencePrice = "-${expense.price.replace("R$ ","").replace(",",".")}"
        val oldExpense = Expense(expense.id, expencePrice, expense.description,expense.category,modifiedDate)
        val newExpense = Expense(id = "", price, description, category, date)
        val timeNow = LocalTime.now()
        val inputTime = "${timeNow.hour}-${timeNow.minute}-${timeNow.second}"

        if (!installmentExpense){
            firebaseAPI.editExpense(oldExpense,newExpense,inputTime)
        }else{
            firebaseAPI.editExpense(oldExpense,newExpense,inputTime,installmentExpense = true, nOfInstallments)
        }

    }
}