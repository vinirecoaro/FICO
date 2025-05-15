package com.example.fico.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.DataStoreManager
import com.example.fico.model.Earning
import com.example.fico.model.InformationPerMonthExpense
import com.example.fico.presentation.fragments.home.HomeFragmentState
import com.example.fico.utils.DateFunctions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.math.BigDecimal
import java.text.NumberFormat

class HomeEarningsViewModel(
    private val dataStore : DataStoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeFragmentState<String>>(HomeFragmentState.Loading)
    val uiState : StateFlow<HomeFragmentState<String>> = _uiState.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    fun getEarningsInfo(date : String = DateFunctions().getCurrentDate(false)){
        _uiState.value = HomeFragmentState.Loading
        viewModelScope.async(Dispatchers.IO){
            val earningList = dataStore.getEarningsList()
            val totalEarningOfMonth = totalEarningOfMonth(date, earningList)
            _uiState.value = HomeFragmentState.Success(totalEarningOfMonth)
        }
    }

    private fun totalEarningOfMonth(date : String, earningList : List<Earning>) : String{
        var totalEarning = BigDecimal(0)
        earningList.forEach { earning ->
            if(DateFunctions().YYYYmmDDtoYYYYmm(earning.date) == DateFunctions().YYYYmmDDtoYYYYmm(date)){
                val earningValue = BigDecimal(earning.value)
                totalEarning = totalEarning.add(earningValue)
            }
        }
        return totalEarning.toString()
    }

}