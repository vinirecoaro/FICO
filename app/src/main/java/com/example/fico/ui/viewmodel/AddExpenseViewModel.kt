package com.example.fico.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.fico.model.Expense
import com.example.fico.service.FirebaseAPI

class AddExpenseViewModel : ViewModel() {

    private val firebaseAPI = FirebaseAPI.instance

    fun addExpense(price: Float, description: String, category: String, date: String){
        val expense = Expense(price, description, category, date)
        firebaseAPI.addExpense(expense)
    }
}