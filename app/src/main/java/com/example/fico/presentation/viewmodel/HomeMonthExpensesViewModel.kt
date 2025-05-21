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

    private val _expenseMonthsLiveData = MutableLiveData<List<String>>()
    val expenseMonthsLiveData: LiveData<List<String>> = _expenseMonthsLiveData
    private val _expensePerCategory = MutableLiveData<List<Pair<String, Double>>>()
    val expensePerCategory : LiveData<List<Pair<String, Double>>> = _expensePerCategory
    private val pieChartPaletteColors = listOf(
        Color.rgb(203, 24, 29),
        Color.rgb(217, 72, 1),
        Color.rgb(241,105,19),
        Color.rgb(253, 141, 60),
        Color.rgb(253, 174, 107)
    )
    private val _uiState = MutableStateFlow<HomeFragmentState<Pair<List<InformationPerMonthExpense>, List<InformationPerMonthExpense>>>>(HomeFragmentState.Loading)
    val uiState : StateFlow<HomeFragmentState<Pair<List<InformationPerMonthExpense>, List<InformationPerMonthExpense>>>> = _uiState.asStateFlow()


    fun getAvailableNow(date : String, formatted: Boolean = true) : Deferred<String> {
        return viewModelScope.async(Dispatchers.IO){
            var availableNowString = "---"
            val informationPerMonthExpense = dataStore.getExpenseInfoPerMonth()
            val informationPerMonthExpenseFromDate = informationPerMonthExpense.find { it.date == date }
            if(informationPerMonthExpenseFromDate != null){
                if(formatted){
                    availableNowString = NumberFormat.getCurrencyInstance().format(informationPerMonthExpenseFromDate.availableNow.toFloat())
                    availableNowString
                }else{
                    availableNowString = informationPerMonthExpenseFromDate.availableNow
                    availableNowString
                }
            }
            availableNowString
        }
    }

    fun getMonthExpense(date : String, formatted: Boolean = true) : Deferred<String> {
        return viewModelScope.async(Dispatchers.IO){
            var monthExpense = "---"
            val informationPerMonthExpense = dataStore.getExpenseInfoPerMonth()
            val informationPerMonthExpenseFromDate = informationPerMonthExpense.find { it.date == date }
            if(informationPerMonthExpenseFromDate != null){
                if(formatted){
                    monthExpense = NumberFormat.getCurrencyInstance().format(informationPerMonthExpenseFromDate.monthExpense.toFloat())
                    monthExpense
                }else{
                    monthExpense = informationPerMonthExpenseFromDate.monthExpense
                    monthExpense
                }
            }
            monthExpense
        }
    }

    fun getExpenseMonths(){
        viewModelScope.async {
            val expenseMonths = dataStore.getExpenseMonths()
            val sortedExpenseMonths = expenseMonths.sortedBy { it }
            val expenseMonthsFormatted = mutableListOf<String>()
            sortedExpenseMonths.forEach { month ->
                expenseMonthsFormatted.add(FormatValuesFromDatabase().formatDateForFilterOnExpenseList(month))
            }
            _expenseMonthsLiveData.postValue(expenseMonthsFormatted)
        }
    }

    fun getCurrentMonthPositionOnList(date : String) : Int{
        val expenseMonthsList = _expenseMonthsLiveData.value
        expenseMonthsList?.let{
            val position = it.indexOf(date)
            return if (position != -1){
                position
            }else{
                RecyclerView.NO_POSITION
            }
        }
        return RecyclerView.NO_POSITION
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCategoriesWithMoreExpense(date : String = DateFunctions().getCurrentlyDateYearMonthToDatabase()){
        viewModelScope.async(Dispatchers.IO){
            val expenseList = dataStore.getExpenseList()
            val expenseListFromMonth = expenseList.filter { FormatValuesToDatabase().expenseDateForInfoPerMonth(it.paymentDate) == date }
            val sumTotalExpenseByCategory = expenseListFromMonth
                .groupBy { it.category }
                .mapValues { entry ->
                    entry.value.sumOf { it.price.toDouble() }
                }.toList().sortedByDescending { it.second }
            if(sumTotalExpenseByCategory.size < 5){
                _expensePerCategory.postValue(sumTotalExpenseByCategory)
            }else{
                val topFive = sumTotalExpenseByCategory.take(5)
                _expensePerCategory.postValue(topFive)
            }
            _uiState.value = HomeFragmentState.Success(Pair(emptyList(), emptyList()))
        }
    }

    fun getPieChartCategoriesColors() : List<Int>{
        return pieChartPaletteColors
    }

}