package com.example.fico.ui.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.service.FirebaseAPI
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.text.DecimalFormat

class SetDefaultBudgetViewModel : ViewModel() {

    private val firebaseAPI = FirebaseAPI.instance

    suspend fun setDefaultBudget(budget: String) : Deferred<Boolean> {
        return viewModelScope.async(Dispatchers.IO) {
            val value = budget.toFloat()
            val formattedBudget = "%.2f".format(value)
                firebaseAPI.setDefaultBudget(formattedBudget)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun checkIfExistDefaultBudget() : Deferred<Boolean> {
        return viewModelScope.async(Dispatchers.IO){
            firebaseAPI.checkIfExistDefaultBudget()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun getDefaultBudget():Deferred<String>{
        return viewModelScope.async(Dispatchers.IO){
            firebaseAPI.getDefaultBudget()
        }
    }
}