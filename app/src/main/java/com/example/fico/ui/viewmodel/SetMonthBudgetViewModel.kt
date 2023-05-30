package com.example.fico.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.fico.service.FirebaseAPI

class SetMonthBudgetViewModel : ViewModel() {

    private val firebaseAPI = FirebaseAPI.instance

    fun setUpBudget(budget: String, date: String){
        firebaseAPI.setUpBudget(budget, date)
    }

}