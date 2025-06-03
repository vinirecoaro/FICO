package com.example.fico.presentation.viewmodel

import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.DataStoreManager
import com.example.fico.api.FormatValuesFromDatabase
import com.example.fico.api.FormatValuesToDatabase
import com.example.fico.model.Earning
import com.example.fico.model.InformationPerMonthExpense
import com.example.fico.model.ValuePerMonth
import com.example.fico.presentation.fragments.home.HomeFragmentState
import com.example.fico.utils.DateFunctions
import com.example.fico.utils.constants.StringConstants
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

class HomeMonthBalanceViewModel(
    private val dataStore : DataStoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeFragmentState<InfoForMonthBalanceFragment>>(HomeFragmentState.Loading)
    val uiState : StateFlow<HomeFragmentState<InfoForMonthBalanceFragment>> = _uiState.asStateFlow()
    private val earningPerCategoryPaletteColors = listOf(
        Color.rgb(255,0, 0),
        Color.rgb(0, 255, 0)
    )

    @RequiresApi(Build.VERSION_CODES.O)
    fun getMonthBalanceInfo(date : String = DateFunctions().getCurrentDate(false)){
        _uiState.value = HomeFragmentState.Loading
        viewModelScope.async(Dispatchers.IO){
            val earningList = dataStore.getEarningsList()
            val expenseMonthInfoList = dataStore.getExpenseInfoPerMonth()
            val totalEarningOfMonthAndEarningMonths = getTotalEarningOfMonthAndEarningMonths(date, earningList)
            val earningMonths = totalEarningOfMonthAndEarningMonths.second
            val balanceMonths = getBalanceMonths(earningMonths, expenseMonthInfoList)
            val existExpenseMonthWithExpense = expenseMonthInfoList.any { BigDecimal(it.monthExpense) >= BigDecimal(0.009) }
            if(earningList.isEmpty() && !existExpenseMonthWithExpense){
                _uiState.value = HomeFragmentState.Empty
            }else{
                if(checkIfEarningMonthHasInfo(date, earningList) || checkIfExpenseMonthHasInfo(date, expenseMonthInfoList)){
                    val totalEarningOfMonth = totalEarningOfMonthAndEarningMonths.first
                    val formattedTotalEarningOfMonth = NumberFormat.getCurrencyInstance().format(totalEarningOfMonth.toFloat())
                    val totalExpenseOfMonth = getMonthExpense(date, expenseMonthInfoList)
                    val formattedTotalExpenseOfMonth = NumberFormat.getCurrencyInstance().format(totalExpenseOfMonth.toFloat())
                    val monthBalance = getMonthBalance(totalEarningOfMonth, totalExpenseOfMonth)
                    val formattedMonthBalance = Pair(NumberFormat.getCurrencyInstance().format(monthBalance.first.toFloat()), monthBalance.second)
                    val chartInfo = getInfoForCashFlowChart(totalExpenseOfMonth, totalEarningOfMonth)
                    val infoForEarningFragment = InfoForMonthBalanceFragment(
                        date,
                        formattedMonthBalance,
                        balanceMonths,
                        formattedTotalEarningOfMonth,
                        formattedTotalExpenseOfMonth,
                        chartInfo
                    )
                    _uiState.value = HomeFragmentState.Success(infoForEarningFragment)
                }
                else{
                    val lastMonthWithInfo = FormatValuesToDatabase().formatDateFromFilterToDatabaseForInfoPerMonth(balanceMonths.maxByOrNull { FormatValuesToDatabase().formatDateFromFilterToDatabaseForInfoPerMonth(it) }!!)
                    val totalEarningOfLastMonth = getTotalEarningOfMonthAndEarningMonths(lastMonthWithInfo, earningList)
                    val totalEarningOfMonth = totalEarningOfLastMonth.first
                    val formattedTotalEarningOfMonth = NumberFormat.getCurrencyInstance().format(totalEarningOfMonth.toFloat())
                    val totalExpenseOfMonth = getMonthExpense(lastMonthWithInfo, expenseMonthInfoList)
                    val formattedTotalExpenseOfMonth = NumberFormat.getCurrencyInstance().format(totalExpenseOfMonth.toFloat())
                    val monthBalance = getMonthBalance(totalEarningOfMonth, totalExpenseOfMonth)
                    val formattedMonthBalance = Pair(NumberFormat.getCurrencyInstance().format(monthBalance.first.toFloat()), monthBalance.second)
                    val chartInfo = getInfoForCashFlowChart(totalExpenseOfMonth, totalEarningOfMonth)
                    val infoForEarningFragment = InfoForMonthBalanceFragment(
                        lastMonthWithInfo,
                        formattedMonthBalance,
                        balanceMonths,
                        formattedTotalEarningOfMonth,
                        formattedTotalExpenseOfMonth,
                        chartInfo
                    )
                    _uiState.value = HomeFragmentState.Success(infoForEarningFragment)
                }
            }
        }
    }
    
    private fun getMonthBalance(totalEarning : String, totalExpense : String) : Pair<String,String>{
        val totalBalance = BigDecimal(totalEarning).subtract(BigDecimal(totalExpense)).setScale(2, RoundingMode.HALF_UP)
        val totalBalanceString = totalBalance.toString()
        return if(totalBalance < BigDecimal(0)){
            Pair(totalBalanceString, StringConstants.GENERAL.NEGATIVE_NUMBER)
        }else if(totalBalance > BigDecimal(0)){
            Pair(totalBalanceString, StringConstants.GENERAL.POSITIVE_NUMBER)
        }else{
            Pair(totalBalanceString, StringConstants.GENERAL.ZERO_STRING)
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

    private fun getInfoForCashFlowChart(monthExpense : String , monthEarning : String): List<Pair<String, Double>> {
        val chartInfo = mutableListOf<Pair<String,Double>>()
        chartInfo.add(Pair(StringConstants.DATABASE.EXPENSE, monthExpense.toDouble()))
        chartInfo.add(Pair(StringConstants.DATABASE.EARNING, monthEarning.toDouble()))
        return chartInfo
    }

    fun getPieChartTransactionsColors() : List<Int>{
        return earningPerCategoryPaletteColors
    }

    data class InfoForMonthBalanceFragment(
        var month : String,
        var monthBalance : Pair<String,String>,
        var balanceMonths : List<String>,
        var totalEarningOfMonth : String,
        var totalExpenseOfMonth : String,
        var chartInfo : List<Pair<String, Double>>
    )

}