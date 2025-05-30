package com.example.fico.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.DataStoreManager
import com.example.fico.api.FormatValuesFromDatabase
import com.example.fico.api.FormatValuesToDatabase
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
            val totalEarningOfMonthAndEarningMonths = getTotalEarningOfMonthAndEarningMonths(date, earningList)
            val earningMonths = totalEarningOfMonthAndEarningMonths.second
            val balanceMonths = getBalanceMonths(earningMonths, expenseMonths)
            val existExpenseMonthWithExpense = expenseMonths.any { BigDecimal(it.monthExpense) >= BigDecimal(0.009) }
            if(earningList.isEmpty() && existExpenseMonthWithExpense){
                _uiState.value = HomeFragmentState.Empty
            }else{
                if(checkIfEarningMonthHasInfo(date, earningList) || checkIfExpenseMonthHasInfo(date, expenseMonths)){
                    val totalEarningOfMonth = totalEarningOfMonthAndEarningMonths.first
                    val formattedTotalEarningOfMonth = NumberFormat.getCurrencyInstance().format(totalEarningOfMonth.toFloat())
                    val totalExpenseOfMonth = getMonthExpense(date, expenseMonths)
                    val formattedTotalExpenseOfMonth = NumberFormat.getCurrencyInstance().format(totalExpenseOfMonth.toFloat())
                    val infoForEarningFragment = InfoForBalanceFragment(
                        date,
                        balanceMonths,
                        formattedTotalEarningOfMonth,
                        formattedTotalExpenseOfMonth
                    )
                    _uiState.value = HomeFragmentState.Success(infoForEarningFragment)
                }
                else{
                    val lastMonthWithInfo = FormatValuesToDatabase().formatDateFromFilterToDatabaseForInfoPerMonth(balanceMonths.maxByOrNull { FormatValuesToDatabase().formatDateFromFilterToDatabaseForInfoPerMonth(it) }!!)
                    val totalEarningOfLastMonth = getTotalEarningOfMonthAndEarningMonths(lastMonthWithInfo, earningList)
                    val totalEarningOfMonth = totalEarningOfLastMonth.first
                    val formattedTotalEarningOfMonth = NumberFormat.getCurrencyInstance().format(totalEarningOfMonth.toFloat())
                    val totalExpenseOfMonth = getMonthExpense(lastMonthWithInfo, expenseMonths)
                    val formattedTotalExpenseOfMonth = NumberFormat.getCurrencyInstance().format(totalExpenseOfMonth.toFloat())
                    val infoForEarningFragment = InfoForBalanceFragment(
                        lastMonthWithInfo,
                        balanceMonths,
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
        val hasInfoMonth = expenseMonthsList.any {
            DateFunctions().YYYYmmDDtoYYYYmm(it.date) == DateFunctions().YYYYmmDDtoYYYYmm(date) &&
            BigDecimal(it.monthExpense) >= BigDecimal(0.009)
        }
        return hasInfoMonth
    }

    private fun getTotalEarningOfMonthAndEarningMonths(date : String, earningList : List<Earning>) : Pair<String, List<String>>{
        var totalEarningOfMonth = BigDecimal(0)
        val earningMonths = mutableListOf<String>()
        val sortedEarningList = earningList.sortedBy { it.date }
        sortedEarningList.forEach { earning ->
            //Total earning of month
            if(DateFunctions().YYYYmmDDtoYYYYmm(earning.date) == DateFunctions().YYYYmmDDtoYYYYmm(date)){
                val earningValue = BigDecimal(earning.value)
                totalEarningOfMonth = totalEarningOfMonth.add(earningValue)
            }
            //Earning months
            val formattedDate = FormatValuesFromDatabase().formatDateForFilterOnExpenseList(DateFunctions().YYYYmmDDtoYYYYmm(earning.date))
            if(!earningMonths.contains(formattedDate)){
                earningMonths.add(formattedDate)
            }
        }
        val result = Pair(totalEarningOfMonth.toString(),earningMonths)
        return result
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

    private fun getBalanceMonths(earningMonths: List<String>, expenseMonths : List<InformationPerMonthExpense>) : List<String> {
        val balanceMonths = mutableSetOf<String>()
        earningMonths.forEach { earningMonth ->
            balanceMonths.add(earningMonth)
        }
        expenseMonths.forEach { expenseMonthInfo ->
            if(BigDecimal(expenseMonthInfo.monthExpense) >= BigDecimal(0.009)){
                val formattedMonth = FormatValuesFromDatabase().formatDateForFilterOnExpenseList(expenseMonthInfo.date)
                balanceMonths.add(formattedMonth)
            }
        }
        val sortedBalanceMonths = balanceMonths.sortedBy { FormatValuesToDatabase().formatDateFromFilterToDatabaseForInfoPerMonth(it) }
        return sortedBalanceMonths
    }

    data class InfoForBalanceFragment(
        var month : String,
        var balanceMonths : List<String>,
        var totalEarningOfMonth : String,
        var totalExpenseOfMonth : String
    )

}