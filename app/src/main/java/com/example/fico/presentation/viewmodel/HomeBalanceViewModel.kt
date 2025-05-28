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
import com.example.fico.utils.constants.StringConstants
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class HomeBalanceViewModel(
    private val dataStore : DataStoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeFragmentState<InfoForBalanceFragment>>(HomeFragmentState.Loading)
    val uiState : StateFlow<HomeFragmentState<InfoForBalanceFragment>> = _uiState.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    fun getBalanceInfo(date : String = DateFunctions().getCurrentDate(false)){
        _uiState.value = HomeFragmentState.Loading
        viewModelScope.async(Dispatchers.IO){
            val earningList = dataStore.getEarningsList()
            val expenseMonths = dataStore.getExpenseInfoPerMonth()
            if(earningList.isEmpty()){
                _uiState.value = HomeFragmentState.Empty
            }else{
                if(checkIfEarningMonthHasInfo(date, earningList) || checkIfExpenseMonthHasInfo(date, expenseMonths)){
                    val totalEarningOfMonth = totalEarningOfMonth(date, earningList)
                    val formattedTotalEarningOfMonth = NumberFormat.getCurrencyInstance().format(totalEarningOfMonth.toFloat())
                    val totalExpenseOfMonth = getMonthExpense(date, expenseMonths)
                    val formattedTotalExpenseOfMonth = NumberFormat.getCurrencyInstance().format(totalExpenseOfMonth.toFloat())

                    val infoForEarningFragment = InfoForBalanceFragment(
                        formattedTotalEarningOfMonth,
                        formattedTotalExpenseOfMonth
                    )
                    _uiState.value = HomeFragmentState.Success(infoForEarningFragment)
                }
                else{
                    val lastMonthWithInfo = earningList.maxByOrNull { it.date }!!.date
                    val totalEarningOfMonth = totalEarningOfMonth(lastMonthWithInfo, earningList)
                    val formattedTotalEarningOfMonth = NumberFormat.getCurrencyInstance().format(totalEarningOfMonth.toFloat())
                    val totalExpenseOfMonth = getMonthExpense(date, expenseMonths)
                    val formattedTotalExpenseOfMonth = NumberFormat.getCurrencyInstance().format(totalExpenseOfMonth.toFloat())

                    val infoForEarningFragment = InfoForBalanceFragment(
                        formattedTotalEarningOfMonth,
                        formattedTotalExpenseOfMonth
                    )
                    _uiState.value = HomeFragmentState.Success(infoForEarningFragment)
                }
            }
        }
    }

    private fun checkIfEarningMonthHasInfo(date : String, earningList : List<Earning>) : Boolean{
        val hasInfoMonth = earningList.any { DateFunctions().YYYYmmDDtoYYYYmm(it.date) == DateFunctions().YYYYmmDDtoYYYYmm(date) }
        return hasInfoMonth
    }

    private fun checkIfExpenseMonthHasInfo(date : String, expenseMonthsList : List<InformationPerMonthExpense>) : Boolean{
        val hasInfoMonth = expenseMonthsList.any { DateFunctions().YYYYmmDDtoYYYYmm(it.date) == DateFunctions().YYYYmmDDtoYYYYmm(date) }
        return hasInfoMonth
    }

    private fun totalEarningOfMonth(date : String, earningList : List<Earning>) : String{
        var totalEarningOfMonth = BigDecimal(0)
        earningList.forEach { earning ->
            //Total earning of month
            if(DateFunctions().YYYYmmDDtoYYYYmm(earning.date) == DateFunctions().YYYYmmDDtoYYYYmm(date)){
                val earningValue = BigDecimal(earning.value)
                totalEarningOfMonth = totalEarningOfMonth.add(earningValue)
            }
        }
        return totalEarningOfMonth.toString()
    }

    private fun getMonthExpense(date : String, expenseMonthsList : List<InformationPerMonthExpense>) : String {
            var monthExpense = "0"
            var dateFormatted = date
            if(date.length != 7){
                dateFormatted = DateFunctions().YYYYmmDDtoYYYYmm(date)
            }
            val informationPerMonthExpenseFromDate = expenseMonthsList.find { it.date == dateFormatted }
            if(informationPerMonthExpenseFromDate != null){
                monthExpense = informationPerMonthExpenseFromDate.monthExpense
            }
            return monthExpense
    }

    data class InfoForBalanceFragment(
        var totalEarningOfMonth : String,
        var totalExpenseOfMonth : String
    )

}