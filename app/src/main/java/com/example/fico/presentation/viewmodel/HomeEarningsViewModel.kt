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
                    val formattedTotalEarningOfMonth = NumberFormat.getCurrencyInstance().format(totalEarningOfMonth.toFloat())
                    val topFiveEarningByCategoryList = getCategoriesWithMoreExpense(date, earningList)
                    val relativeResultComparedWithLastMonth = getRelativeResultComparedWithLastMonth(
                        date,
                        totalEarningOfMonth,
                        earningList
                    )
                    val infoForEarningFragment = InfoForEarningFragment(
                        date,
                        relativeResultComparedWithLastMonth,
                        earningMonths,
                        formattedTotalEarningOfMonth,
                        topFiveEarningByCategoryList
                    )
                    _uiState.value = HomeFragmentState.Success(infoForEarningFragment)
                }
                else{
                    val lastMonthWithInfo = earningList.maxByOrNull { it.date }!!.date
                    val totalEarningOfMonthAndEarningMonths = totalEarningOfMonthAndEarningMonths(lastMonthWithInfo, earningList)
                    val earningMonths = totalEarningOfMonthAndEarningMonths.second
                    val totalEarningOfMonth = totalEarningOfMonthAndEarningMonths.first
                    val formattedTotalEarningOfMonth = NumberFormat.getCurrencyInstance().format(totalEarningOfMonth.toFloat())
                    val topFiveEarningByCategoryList = getCategoriesWithMoreExpense(lastMonthWithInfo, earningList)
                    val relativeResultComparedWithLastMonth = getRelativeResultComparedWithLastMonth(
                        lastMonthWithInfo,
                        totalEarningOfMonth,
                        earningList
                    )
                    val infoForEarningFragment = InfoForEarningFragment(
                        lastMonthWithInfo,
                        relativeResultComparedWithLastMonth,
                        earningMonths,
                        formattedTotalEarningOfMonth,
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getRelativeResultComparedWithLastMonth(
        date : String,
        totalMonthEarning : String,
        earningList : List<Earning>)
    : Pair<String,String>{
        val formatter = DateTimeFormatter.ofPattern(if (date.length == 7) "yyyy-MM" else "yyyy-MM-dd")
        val yearMonth = YearMonth.parse(date, formatter)
        val month = yearMonth.monthValue
        val beforeMonth = month.minus(1)
        var totalEarningLastMonth = BigDecimal(0)
        val formatterEarningDate = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val existEarningBeforeMonth = earningList.any{ earning ->
            val earningDate = YearMonth.parse(earning.date, formatterEarningDate)
            val earningMonth = earningDate.monthValue
            earningMonth == beforeMonth
        }
        if(existEarningBeforeMonth){
            earningList.forEach { earning ->
                val earningDate = YearMonth.parse(earning.date, formatterEarningDate)
                val earningMonth = earningDate.monthValue
                if(earningMonth == beforeMonth){
                    totalEarningLastMonth = totalEarningLastMonth.add(BigDecimal(earning.value))
                }
            }
            val result = BigDecimal(totalMonthEarning)
            val result1 = result.divide(totalEarningLastMonth, 8, RoundingMode.HALF_UP)
            val result2 = result1.subtract(BigDecimal(1))
            val result3 = result2.multiply(BigDecimal(100))
            if(result3.toFloat() > 0){
                val resultFormatted = result3.setScale(0,RoundingMode.HALF_UP).toString()
                return Pair(resultFormatted, StringConstants.HOME_FRAGMENT.INCREASE)
            }else if(result3.toFloat() < 0){
                val resultFormatted = result3.multiply(BigDecimal(-1)).setScale(0,RoundingMode.HALF_UP).toString()
                return Pair(resultFormatted, StringConstants.HOME_FRAGMENT.DECREASE)
            }else{
                return Pair(result.toString(), StringConstants.HOME_FRAGMENT.EQUAL)
            }
        }else{
            return Pair(StringConstants.GENERAL.ZERO_STRING, StringConstants.HOME_FRAGMENT.NO_BEFORE_MONTH)
        }
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
        val result = Pair(totalEarningOfMonth.toString(),earningMonths)
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
        var relativeResult : Pair<String, String>,
        var earningMonths : List<String>,
        var totalEarningOfMonth : String,
        var topFiveEarningByCategoryList : List<Pair<String, Double>>
    )

}