package com.example.fico.ui.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.example.fico.model.Expense
import com.example.fico.service.FirebaseAPI
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.CompletableFuture

class AddExpenseViewModel : ViewModel() {

    private val firebaseAPI = FirebaseAPI.instance
    private val usedAddress = mutableSetOf<String>()

    @RequiresApi(Build.VERSION_CODES.N)
    fun addExpense(price: String, description: String, category: String, date: String){
        val expense = Expense(price, description, category, date)
        firebaseAPI.addExpense(expense)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun checkIfExistsDateOnDatabse(date: String): CompletableFuture<Boolean> {
        return firebaseAPI.checkIfExistsDateOnDatabse(date)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentlyDate() : String{
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return currentDate.format(formatter)
    }



}