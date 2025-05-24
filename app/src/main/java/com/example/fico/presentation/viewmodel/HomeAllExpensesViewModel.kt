package com.example.fico.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.DataStoreManager
import com.example.fico.api.FormatValuesFromDatabase
import com.example.fico.model.BarChartParams
import com.example.fico.model.InformationPerMonthExpense
import com.example.fico.presentation.fragments.home.HomeFragmentState
import com.example.fico.utils.DateFunctions
import com.github.mikephil.charting.data.BarEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.math.BigDecimal
import java.math.RoundingMode

class HomeAllExpensesViewModel(
    private val dataStore : DataStoreManager
) : ViewModel() {

    private val _infoPerMonthLabel = MutableLiveData<List<InformationPerMonthExpense>>()
    private val _isBlurred = MutableLiveData(true)
    val isBlurred : LiveData<Boolean> = _isBlurred
    private val _uiState = MutableStateFlow<HomeFragmentState<InfoForAllExpensesFragment>>(HomeFragmentState.Loading)
    val uiState : StateFlow<HomeFragmentState<InfoForAllExpensesFragment>> = _uiState.asStateFlow()

    fun getExpensesInfo(){
        viewModelScope.async(Dispatchers.IO){
            _uiState.value = HomeFragmentState.Loading

            val infoPerMonth = dataStore.getExpenseInfoPerMonth()
            val monthWithExpenses = getMonthsWithExpenses(infoPerMonth)

            if(monthWithExpenses.isEmpty()){
                _uiState.value = HomeFragmentState.Empty
            }else{
                val barChartParams = getExpenseBarChartParams(monthWithExpenses)
                val totalExpense = dataStore.getTotalExpense()
                val expensesPeriod = getExpensesPeriod(monthWithExpenses)

                val infoForAllExpensesFragment = InfoForAllExpensesFragment(
                    barChartParams,
                    totalExpense,
                    expensesPeriod
                )
                _uiState.value = HomeFragmentState.Success(infoForAllExpensesFragment
                )
            }
        }
    }

    private fun getMonthsWithExpenses(infoPerMonthList : List<InformationPerMonthExpense>) : List<InformationPerMonthExpense> {
        val monthWithExpenses = mutableListOf<InformationPerMonthExpense>()
        for(infoPerMonth in infoPerMonthList){
            if (BigDecimal(infoPerMonth.monthExpense).setScale(2,
                    RoundingMode.HALF_UP) != BigDecimal("0").setScale(2, RoundingMode.HALF_UP)){
                monthWithExpenses.add(infoPerMonth)
            }
        }
        return monthWithExpenses
    }

    private fun getExpenseBarChartParams(monthWithExpenses : List<InformationPerMonthExpense>) : BarChartParams{

            val barChartEntries : ArrayList<BarEntry> = arrayListOf()
            val formattedInfoPerMonthLabel = mutableListOf<InformationPerMonthExpense>()
            val barChartMonthLabels : MutableSet<String> = mutableSetOf()
            val barChartExpenseLabels : MutableSet<String> = mutableSetOf()

            val sortedMonthWithExpensesList = monthWithExpenses.sortedBy { it.date }

            _infoPerMonthLabel.postValue(sortedMonthWithExpensesList)

            var i = 0f
            for (infoPerMonth in sortedMonthWithExpensesList){

                //Create entries
                val monthExpense = infoPerMonth.monthExpense.toFloat()
                barChartEntries.add(BarEntry(i, monthExpense))
                i += 1f

                // Create labels
                formattedInfoPerMonthLabel.add(
                    InformationPerMonthExpense(
                        FormatValuesFromDatabase().formatMonthAbbreviatedWithDash(infoPerMonth.date),
                        infoPerMonth.availableNow,
                        infoPerMonth.budget,
                        FormatValuesFromDatabase().price(infoPerMonth.monthExpense)
                    )
                )
            }

            for(infoPerMonthLabel in formattedInfoPerMonthLabel){
                barChartMonthLabels.add(infoPerMonthLabel.date)
                barChartExpenseLabels.add(infoPerMonthLabel.monthExpense)
            }

            val barChartParams = BarChartParams(barChartEntries,barChartMonthLabels, barChartExpenseLabels)

            return barChartParams

        }

    private fun getExpensesPeriod(monthWithExpenses : List<InformationPerMonthExpense>) : String{
        val firstMonth = monthWithExpenses.first().date
        val lastMonth = monthWithExpenses.last().date
        val firstMonthFormatted = FormatValuesFromDatabase().formatMonthAbbreviatedWithBar(firstMonth)
        val lastMonthFormatted = FormatValuesFromDatabase().formatMonthAbbreviatedWithBar(lastMonth)
        return "($firstMonthFormatted - $lastMonthFormatted)"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentDatePositionBarChart() : Int{
        val currentDate = DateFunctions().getCurrentlyDateYearMonthToDatabase()
        if(_infoPerMonthLabel.value != null){
            _infoPerMonthLabel.value!!.forEachIndexed { index, informationPerMonthExpense ->
                if(informationPerMonthExpense.date == currentDate){
                    return if(index < 1){
                        -1
                    }else{
                        index-1
                    }
                }
            }
            return _infoPerMonthLabel.value!!.size - 1
        }else{
            return 0
        }

    }

    fun changeBlurState(){
        if(_isBlurred.value != null){
            _isBlurred.postValue(!_isBlurred.value!!)
        }
    }

    class InfoForAllExpensesFragment(
        val barChartParams : BarChartParams,
        val totalExpense : String,
        val expensesPeriod : String
    )

}