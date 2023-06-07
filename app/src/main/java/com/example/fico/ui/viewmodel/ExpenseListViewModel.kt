package com.example.fico.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.example.fico.model.Expense
import com.example.fico.service.FirebaseAPI

class ExpenseListViewModel: ViewModel() {

    private val firebaseAPI = FirebaseAPI.instance
    private val expenses : MutableList<Expense> = mutableListOf()

    fun getExpenseList(recyclerView: RecyclerView){
        firebaseAPI.getExpenseList(recyclerView, expenses)
    }
}