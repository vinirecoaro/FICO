package com.example.fico.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.DataStoreManager
import com.example.fico.api.FirebaseAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.math.BigDecimal
import java.math.RoundingMode

class SetDefaultBudgetViewModel(
    private val firebaseAPI : FirebaseAPI,
    private val dataStore : DataStoreManager
) : ViewModel() {

    private val _getDefaultBudgetResult = MutableLiveData<String?>()
    val getDefaultBudgetResult : LiveData<String?> = _getDefaultBudgetResult
    private val _setDefaultBudgetResult = MutableLiveData<Boolean>()
    val setDefaultBudgetResult : LiveData<Boolean> = _setDefaultBudgetResult

   /* suspend fun setDefaultBudget(budget: String) : Deferred<Boolean> {
        return viewModelScope.async(Dispatchers.IO) {
            val value = budget.toFloat()
            val formattedBudget = "%.2f".format(value).replace(",",".")
                firebaseAPI.setDefaultBudget(formattedBudget)
        }
    }*/

    suspend fun setDefaultBudget(budget: String){
        viewModelScope.async(Dispatchers.IO){
            firebaseAPI.setDefaultBudget(budget).fold(
                onSuccess = {
                    //TODO update dataStore
                    _setDefaultBudgetResult.postValue(true)
                },
                onFailure = {
                    _setDefaultBudgetResult.postValue(false)
                }
            )
        }
    }

    fun getDefaultBudget(){
        viewModelScope.async(Dispatchers.IO){
            var defaultBudget = dataStore.getDefaultBudget()
            defaultBudget = BigDecimal(defaultBudget).setScale(2,RoundingMode.HALF_UP).toString()
            _getDefaultBudgetResult.postValue(defaultBudget)
        }
    }
}