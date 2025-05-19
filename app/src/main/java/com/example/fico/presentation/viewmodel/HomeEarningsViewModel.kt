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

    private val _uiState = MutableStateFlow<HomeFragmentState<InfoForEarningFragment>>(HomeFragmentState.Loading)
    val uiState : StateFlow<HomeFragmentState<InfoForEarningFragment>> = _uiState.asStateFlow()
    private val earningPerCategoryPaletteColors = listOf(
        Color.rgb(0,109,44),
        Color.rgb(35, 139, 69),
        Color.rgb(65, 171, 93),
        Color.rgb(116, 196, 118),
        Color.rgb(161, 217, 155),
    )

    @RequiresApi(Build.VERSION_CODES.O)
    fun getEarningsInfo(date : String = DateFunctions().getCurrentDate(false)){
        _uiState.value = HomeFragmentState.Loading
        viewModelScope.async(Dispatchers.IO){
            val earningList = dataStore.getEarningsList()
            if(earningList.isEmpty()){
                _uiState.value = HomeFragmentState.Empty
            }else{
                if(checkIfMonthHasInfo(date, earningList)){
                    val totalEarningOfMonthAndEarningMonths = totalEarningOfMonthAndEarningMonths(date, earningList)
                    val earningMonths = totalEarningOfMonthAndEarningMonths.second
                    val totalEarningOfMonth = totalEarningOfMonthAndEarningMonths.first
                    val topFiveEarningByCategoryList = getCategoriesWithMoreExpense(date, earningList)
                    val infoForEarningFragment = InfoForEarningFragment(
                        date,
                        earningMonths,
                        totalEarningOfMonth,
                        topFiveEarningByCategoryList
                    )
                    _uiState.value = HomeFragmentState.Success(infoForEarningFragment)
                }
                else{
                    val lastMonthWithInfo = earningList.maxByOrNull { it.date }!!.date
                    val totalEarningOfMonthAndEarningMonths = totalEarningOfMonthAndEarningMonths(lastMonthWithInfo, earningList)
                    val earningMonths = totalEarningOfMonthAndEarningMonths.second
                    val totalEarningOfMonth = totalEarningOfMonthAndEarningMonths.first
                    val topFiveEarningByCategoryList = getCategoriesWithMoreExpense(lastMonthWithInfo, earningList)
                    val infoForEarningFragment = InfoForEarningFragment(
                        lastMonthWithInfo,
                        earningMonths,
                        totalEarningOfMonth,
                        topFiveEarningByCategoryList
                    )
                    _uiState.value = HomeFragmentState.Success(infoForEarningFragment)
                }
            }
        }
    }

    private fun checkIfMonthHasInfo(date : String, earningList : List<Earning>) : Boolean{
        val hasInfoMonth = earningList.any { DateFunctions().YYYYmmDDtoYYYYmm(it.date) == DateFunctions().YYYYmmDDtoYYYYmm(date) }
        return hasInfoMonth
    }

    private fun totalEarningOfMonthAndEarningMonths(date : String, earningList : List<Earning>) : Pair<String, List<String>>{
        var totalEarningOfMonth = BigDecimal(0)
        val earningMonths = mutableListOf<String>()
        earningList.forEach { earning ->
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
        val formattedTotalEarningOfMonth = NumberFormat.getCurrencyInstance().format(totalEarningOfMonth.toFloat())
        val result = Pair(formattedTotalEarningOfMonth,earningMonths)
        return result
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCategoriesWithMoreExpense(date : String, earningList : List<Earning>) : List<Pair<String, Double>>{
        val earningListFromMonth = earningList.filter { DateFunctions().YYYYmmDDtoYYYYmm(it.date) == DateFunctions().YYYYmmDDtoYYYYmm(date) }
        val topFiveEarningByCategoryList = earningListFromMonth
            .groupBy { it.category }
            .mapValues { entry ->
                entry.value.sumOf { it.value.toDouble() }
            }.toList().sortedByDescending { it.second }
        if(topFiveEarningByCategoryList.size < 5){
            return topFiveEarningByCategoryList
        }else{
            val topFive = topFiveEarningByCategoryList.take(5)
            return topFive
        }
    }

    fun getPieChartCategoriesColors() : List<Int>{
        return earningPerCategoryPaletteColors
    }

    data class InfoForEarningFragment(
        var month : String,
        var earningMonths : List<String>,
        var totalEarningOfMonth : String,
        var topFiveEarningByCategoryList : List<Pair<String, Double>>
    )

}