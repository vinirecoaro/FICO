package com.example.fico.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.example.fico.model.Expense
import com.example.fico.service.FirebaseAPI

class ExpenseListViewModel: ViewModel() {

    private val firebaseAPI = FirebaseAPI.instance
    private val _expensesLiveData = MutableLiveData<List<Expense>>()
    val expensesLiveData: LiveData<List<Expense>> = _expensesLiveData

    fun getExpenseList() {
        firebaseAPI.getExpenseList { expenses ->
            _expensesLiveData.value = expenses
        }
    }
}