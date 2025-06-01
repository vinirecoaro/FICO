package com.example.fico.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.DataStoreManager
import com.example.fico.api.FormatValuesFromDatabase
import com.example.fico.model.Earning
import com.example.fico.model.InformationPerMonthExpense
import com.example.fico.presentation.fragments.home.HomeFragmentState
import com.github.mikephil.charting.data.Entry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.math.BigDecimal

class HomeAllBalanceViewModel(
    private val dataStore : DataStoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeFragmentState<InfoForAllBalanceFragment>>(HomeFragmentState.Loading)
    val uiState : StateFlow<HomeFragmentState<InfoForAllBalanceFragment>> = _uiState.asStateFlow()
    private var lineChartYAxisVisible = false

    fun getBalanceInfo(){
        viewModelScope.async(Dispatchers.IO){
            _uiState.value = HomeFragmentState.Loading
            val earningList = dataStore.getEarningsList()
            val expenseMonths = dataStore.getExpenseInfoPerMonth()
            val existExpenseMonthWithExpense = expenseMonths.any { BigDecimal(it.monthExpense) >= BigDecimal(0.009) }
            if(earningList.isEmpty() && !existExpenseMonthWithExpense){
                _uiState.value = HomeFragmentState.Empty
            }else{
                val chartInfo = fetchChartInfo(earningList, expenseMonths)
                val infoForAllBalanceFragment = InfoForAllBalanceFragment(
                    chartInfo.first,
                    chartInfo.second
                )
                _uiState.value = HomeFragmentState.Success(infoForAllBalanceFragment)
            }

        }
    }

    private fun fetchChartInfo(earningList: List<Earning>, expenseMonths : List<InformationPerMonthExpense>) : Pair<List<Entry>,List<String>> {
        val monthEarningList = getMonthEarningList(earningList)
        val monthBalanceList = mutableListOf<ValuePerMonth>()
        monthBalanceList.addAll(monthEarningList)
        expenseMonths.forEach { expenseMonthInfo ->
            if(BigDecimal(expenseMonthInfo.monthExpense) >= BigDecimal(0.009)){
                val balanceMonthInfo = monthBalanceList.find { it.month == expenseMonthInfo.date }
                if(balanceMonthInfo != null){
                    val totalBalance = BigDecimal(balanceMonthInfo.value).subtract(BigDecimal(expenseMonthInfo.monthExpense))
                    balanceMonthInfo.value = totalBalance.toString()
                }else{
                    monthBalanceList.add(ValuePerMonth(expenseMonthInfo.date, BigDecimal(expenseMonthInfo.monthExpense).negate().toString()))
                }
            }
        }

        val sortedMonthBalanceList = monthBalanceList.sortedBy { it.month }

        val xyEntries = mutableListOf<Entry>()
        val yLabels = mutableListOf<String>()

        sortedMonthBalanceList.forEachIndexed { index, valuePerMonth ->
            xyEntries.add(Entry(index.toFloat(), BigDecimal(valuePerMonth.value).toFloat()))
            val monthLabel = FormatValuesFromDatabase().formatMonthAbbreviatedWithDash(valuePerMonth.month)
            yLabels.add(monthLabel)
        }

        return Pair(xyEntries, yLabels)
    }

    private fun getMonthEarningList(earningList: List<Earning>) : List<ValuePerMonth>{
        val monthEarningList = mutableListOf<ValuePerMonth>()
        earningList.forEach { earning ->
            val earningMonth = earning.date.substring(0,7)
            val earningMonthInfo = monthEarningList.find { it.month == earningMonth}
            if(earningMonthInfo != null){
                val totalEarning = BigDecimal(earningMonthInfo.value).add(BigDecimal(earning.value))
                earningMonthInfo.value = totalEarning.toString()
            }else{
                monthEarningList.add(ValuePerMonth(earningMonth, earning.value))
            }
        }
        return monthEarningList
    }

    fun getLineChartYAxisVisible() : Boolean{
        return lineChartYAxisVisible
    }

    fun setLineChartYAxisVisible(state : Boolean){
        lineChartYAxisVisible = state
    }

    data class ValuePerMonth(
        var month : String,
        var value : String
    )

    data class InfoForAllBalanceFragment(
        var xyEntries : List<Entry>,
        var yLabels : List<String>
    )
}