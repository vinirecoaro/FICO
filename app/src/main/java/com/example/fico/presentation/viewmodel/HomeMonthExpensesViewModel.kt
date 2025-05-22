package com.example.fico.presentation.viewmodel

import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.example.fico.DataStoreManager
import com.example.fico.api.FormatValuesFromDatabase
import com.example.fico.api.FormatValuesToDatabase
import com.example.fico.model.BarChartParams
import com.example.fico.model.Earning
import com.example.fico.model.InformationPerMonthExpense
import com.example.fico.presentation.fragments.home.HomeFragmentState
import com.example.fico.utils.DateFunctions
import com.github.mikephil.charting.data.BarEntry
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat

class HomeMonthExpensesViewModel(
    private val dataStore : DataStoreManager
) : ViewModel() {

    private val pieChartPaletteColors = listOf(
        Color.rgb(203, 24, 29),
        Color.rgb(217, 72, 1),
        Color.rgb(241,105,19),
        Color.rgb(253, 141, 60),
        Color.rgb(253, 174, 107)
    )
    private val _uiState = MutableStateFlow<HomeFragmentState<InfoForExpensesFragment>>(HomeFragmentState.Loading)
    val uiState : StateFlow<HomeFragmentState<InfoForExpensesFragment>> = _uiState.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    fun getExpensesInfo(date : String = DateFunctions().getCurrentlyDateYearMonthToDatabase()){
        viewModelScope.async(Dispatchers.IO){
            _uiState.value = HomeFragmentState.Loading
            val expenseMonths = getExpenseMonths().await()
            if(expenseMonths.isEmpty()){
                _uiState.value = HomeFragmentState.Empty
            }else{
                val expenseMonthsFormatted = mutableListOf<String>()
                expenseMonths.forEach { month ->
                    expenseMonthsFormatted.add(FormatValuesFromDatabase().formatDateForFilterOnExpenseList(month))
                }
                if(checkIfHasCurrentMonth(date, expenseMonths)){
                    val monthExpense = getMonthExpense(date).await()
                    val availableNow = getAvailableNow(date).await()
                    val categoriesWithMoreExpense = getCategoriesWithMoreExpense(date).await()
                    val infoForExpensesFragment = InfoForExpensesFragment(
                        date,
                        expenseMonthsFormatted,
                        monthExpense,
                        availableNow,
                        categoriesWithMoreExpense
                    )
                    _uiState.value = HomeFragmentState.Success(infoForExpensesFragment)
                }else{
                    val lastMonthWithInfo = expenseMonths.maxByOrNull{it}!!
                    val monthExpense = getMonthExpense(lastMonthWithInfo).await()
                    val availableNow = getAvailableNow(lastMonthWithInfo).await()
                    val categoriesWithMoreExpense = getCategoriesWithMoreExpense(lastMonthWithInfo).await()
                    val infoForExpensesFragment = InfoForExpensesFragment(
                        lastMonthWithInfo,
                        expenseMonthsFormatted,
                        monthExpense,
                        availableNow,
                        categoriesWithMoreExpense
                    )
                    _uiState.value = HomeFragmentState.Success(infoForExpensesFragment)
                }
            }
        }
    }

    private fun getAvailableNow(date : String) : Deferred<String> {
        return viewModelScope.async(Dispatchers.IO){
            var availableNowString = "---"
            val informationPerMonthExpense = dataStore.getExpenseInfoPerMonth()
            val informationPerMonthExpenseFromDate = informationPerMonthExpense.find { it.date == date }
            if(informationPerMonthExpenseFromDate != null){
                availableNowString = informationPerMonthExpenseFromDate.availableNow
            }
            availableNowString
        }
    }

    private fun getMonthExpense(date : String) : Deferred<String> {
        return viewModelScope.async(Dispatchers.IO){
            var monthExpense = "---"
            val informationPerMonthExpense = dataStore.getExpenseInfoPerMonth()
            val informationPerMonthExpenseFromDate = informationPerMonthExpense.find { it.date == date }
            if(informationPerMonthExpenseFromDate != null){
                monthExpense = informationPerMonthExpenseFromDate.monthExpense
            }
            monthExpense
        }
    }

    private fun getExpenseMonths() : Deferred<List<String>> {
        return viewModelScope.async(Dispatchers.IO){
            val expenseMonths = dataStore.getExpenseMonths()
            val sortedExpenseMonths = expenseMonths.sortedBy { it }
            sortedExpenseMonths
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCategoriesWithMoreExpense(date : String = DateFunctions().getCurrentlyDateYearMonthToDatabase()) : Deferred<List<Pair<String, Double>>>{
        return viewModelScope.async(Dispatchers.IO){
            val expenseList = dataStore.getExpenseList()
            val expenseListFromMonth = expenseList.filter { FormatValuesToDatabase().expenseDateForInfoPerMonth(it.paymentDate) == date }
            val sumTotalExpenseByCategory = expenseListFromMonth
                .groupBy { it.category }
                .mapValues { entry ->
                    entry.value.sumOf { it.price.toDouble() }
                }.toList().sortedByDescending { it.second }
            if(sumTotalExpenseByCategory.size < 5){
                sumTotalExpenseByCategory
            }else{
                val topFive = sumTotalExpenseByCategory.take(5)
                topFive
            }
        }
    }

    fun getPieChartCategoriesColors() : List<Int>{
        return pieChartPaletteColors
    }

    data class InfoForExpensesFragment(
        var month : String,
        var expenseMonths : List<String>,
        var monthExpense : String,
        var availableNow : String,
        var topFiveExpenseByCategoryList : List<Pair<String, Double>>
    ){
        fun availableNowFormattedToLocalCurrency(): String? {
            return NumberFormat.getCurrencyInstance().format(this.availableNow.toFloat())
        }

        fun monthExpenseFormattedToLocalCurrency(): String? {
            return NumberFormat.getCurrencyInstance().format(this.monthExpense.toFloat())
        }
    }

    private fun checkIfHasCurrentMonth(date : String, expenseMonths : List<String>) : Boolean{
        val hasCurrentMonth = expenseMonths.any { it == DateFunctions().YYYYmmDDtoYYYYmm(date) }
        return hasCurrentMonth
    }

}