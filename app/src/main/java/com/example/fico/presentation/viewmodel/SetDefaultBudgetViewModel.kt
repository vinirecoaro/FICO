package com.example.fico.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.DataStoreManager
import com.example.fico.api.FirebaseAPI
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.math.BigDecimal
import java.math.RoundingMode

class SetDefaultBudgetViewModel(
    private val firebaseAPI : FirebaseAPI,
    private val dataStore : DataStoreManager
) : ViewModel() {

    private val _defaultBudgetResult = MutableLiveData<String?>()
    val defaultBudgetResult : LiveData<String?> = _defaultBudgetResult

    suspend fun setDefaultBudget(budget: String) : Deferred<Boolean> {
        return viewModelScope.async(Dispatchers.IO) {
            val value = budget.toFloat()
            val formattedBudget = "%.2f".format(value).replace(",",".")
                firebaseAPI.setDefaultBudget(formattedBudget)
        }
    }

    fun getDefaultBudget(){
        viewModelScope.async(Dispatchers.IO){
            var defaultBudget = dataStore.getDefaultBudget()
            defaultBudget = BigDecimal(defaultBudget).setScale(2,RoundingMode.HALF_UP).toString()
            _defaultBudgetResult.postValue(defaultBudget)
        }
    }
}