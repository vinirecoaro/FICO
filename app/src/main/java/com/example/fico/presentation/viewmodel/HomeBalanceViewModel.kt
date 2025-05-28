package com.example.fico.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.DataStoreManager
import com.example.fico.model.Earning
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
            if(earningList.isEmpty()){
                _uiState.value = HomeFragmentState.Empty
            }else{
                if(checkIfMonthHasInfo(date, earningList)){
                    val totalEarningOfMonth = totalEarningOfMonth(date, earningList)
                    val formattedTotalEarningOfMonth = NumberFormat.getCurrencyInstance().format(totalEarningOfMonth.toFloat())
                    val totalExpenseOfMonth = getMonthExpense(date).await()
                    val formattedTotalExpenseOfMonth = NumberFormat.getCurrencyInstance().format(totalExpenseOfMonth.toFloat())
                    val relativeResultComparedWithLastMonth = getRelativeResultComparedWithLastMonth(
                        date,
                        totalEarningOfMonth,
                        earningList
                    )
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
                    val totalExpenseOfMonth = getMonthExpense(date).await()
                    val formattedTotalExpenseOfMonth = NumberFormat.getCurrencyInstance().format(totalExpenseOfMonth.toFloat())
                    val relativeResultComparedWithLastMonth = getRelativeResultComparedWithLastMonth(
                        lastMonthWithInfo,
                        totalEarningOfMonth,
                        earningList
                    )
                    val infoForEarningFragment = InfoForBalanceFragment(
                        formattedTotalEarningOfMonth,
                        formattedTotalExpenseOfMonth
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
            val result = BigDecimal(totalMonthEarning).divide(totalEarningLastMonth, 8, RoundingMode.HALF_UP).subtract(BigDecimal(1)).multiply(BigDecimal(100))
            if(result.toFloat() > 0){
                val resultFormatted = result.setScale(0,RoundingMode.HALF_UP).toString()
                return Pair(resultFormatted, StringConstants.HOME_FRAGMENT.INCREASE)
            }else if(result.toFloat() < 0){
                val resultFormatted = result.multiply(BigDecimal(-1)).setScale(0,RoundingMode.HALF_UP).toString()
                return Pair(resultFormatted, StringConstants.HOME_FRAGMENT.DECREASE)
            }else{
                return Pair(result.toString(), StringConstants.HOME_FRAGMENT.EQUAL)
            }
        }else{
            return Pair(StringConstants.GENERAL.ZERO_STRING, StringConstants.HOME_FRAGMENT.NO_BEFORE_MONTH)
        }
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

    private fun getMonthExpense(date : String) : Deferred<String> {
        return viewModelScope.async(Dispatchers.IO){
            var dateFormatted = date
            if(date.length != 7){
                dateFormatted = DateFunctions().YYYYmmDDtoYYYYmm(date)
            }
            var monthExpense = "0"
            val informationPerMonthExpense = dataStore.getExpenseInfoPerMonth()
            val informationPerMonthExpenseFromDate = informationPerMonthExpense.find { it.date == dateFormatted }
            if(informationPerMonthExpenseFromDate != null){
                monthExpense = informationPerMonthExpenseFromDate.monthExpense
            }
            monthExpense
        }
    }

    data class InfoForBalanceFragment(
        /*var month : String,
        var relativeResult : Pair<String, String>,
        var earningMonths : List<String>,*/
        var totalEarningOfMonth : String,
        var totalExpenseOfMonth : String,
        /*var topFiveEarningByCategoryList : List<Pair<String, Double>>*/
    )

}