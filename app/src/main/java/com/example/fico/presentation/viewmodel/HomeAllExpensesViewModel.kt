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
import java.text.NumberFormat

class HomeAllExpensesViewModel(
    private val dataStore : DataStoreManager
) : ViewModel() {

    private val _infoPerMonthLabel = MutableLiveData<List<InformationPerMonthExpense>>()
    private val _totalExpense = MutableLiveData<String>()
    val totalExpenseLiveData : LiveData<String> = _totalExpense
    private val _informationPerMonth = MutableLiveData<List<InformationPerMonthExpense>>()
    val informationPerMonthLiveData : LiveData<List<InformationPerMonthExpense>> = _informationPerMonth
    private val _isBlurred = MutableLiveData<Boolean>(true)
    val isBlurred : LiveData<Boolean> = _isBlurred
    private val _expenseBarChartParams = MutableLiveData<BarChartParams>(BarChartParams.empty())
    val expenseBarChartParams : LiveData<BarChartParams> = _expenseBarChartParams
    private val _uiState = MutableStateFlow<HomeFragmentState<Pair<List<InformationPerMonthExpense>, List<InformationPerMonthExpense>>>>(HomeFragmentState.Loading)
    val uiState : StateFlow<HomeFragmentState<Pair<List<InformationPerMonthExpense>, List<InformationPerMonthExpense>>>> = _uiState.asStateFlow()

    fun changeBlurState(){
        if(_isBlurred.value != null){
            _isBlurred.postValue(!_isBlurred.value!!)
        }
    }

    fun getTotalExpense(){
        viewModelScope.async(Dispatchers.IO){
            val totalExpense = dataStore.getTotalExpense()
            val price = totalExpense.toFloat()
            val priceFormatted = NumberFormat.getCurrencyInstance().format(price)
            _totalExpense.postValue(priceFormatted)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentDatePositionBarChart() : Int{
        val currentDate = FormatValuesFromDatabase().formatDateAbbreviated(DateFunctions().getCurrentlyDateYearMonthToDatabase())
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

    fun getExpenseBarChartParams(){

        _uiState.value = HomeFragmentState.Loading

        viewModelScope.async(Dispatchers.IO){

            val barChartEntries : ArrayList<BarEntry> = arrayListOf()
            val formattedInfoPerMonthLabel = mutableListOf<InformationPerMonthExpense>()
            val barChartMonthLabels : MutableSet<String> = mutableSetOf()
            val barChartExpenseLabels : MutableSet<String> = mutableSetOf()

            try {
                val infoPerMonthList = dataStore.getExpenseInfoPerMonth()
                val monthWithExpenses = mutableListOf<InformationPerMonthExpense>()
                for(infoPerMonth in infoPerMonthList){
                    if (BigDecimal(infoPerMonth.monthExpense).setScale(2,
                            RoundingMode.HALF_UP) != BigDecimal("0").setScale(2, RoundingMode.HALF_UP)){
                        monthWithExpenses.add(infoPerMonth)
                    }
                }
                if(monthWithExpenses.isEmpty()){
                    _uiState.value = HomeFragmentState.Empty
                }else{
                    val sortedMonthWithExpensesList = monthWithExpenses.sortedBy { it.date }

                    var i = 0f
                    for (infoPerMonth in sortedMonthWithExpensesList){

                        //Create entries
                        val monthExpense = infoPerMonth.monthExpense.toFloat()
                        barChartEntries.add(BarEntry(i, monthExpense))
                        i += 1f

                        // Create labels
                        formattedInfoPerMonthLabel.add(
                            InformationPerMonthExpense(
                                FormatValuesFromDatabase().formatDateAbbreviated(infoPerMonth.date),
                                infoPerMonth.availableNow,
                                infoPerMonth.budget,
                                FormatValuesFromDatabase().price(infoPerMonth.monthExpense)
                            )
                        )
                    }

                    _infoPerMonthLabel.postValue(formattedInfoPerMonthLabel)

                    for(infoPerMonthLabel in formattedInfoPerMonthLabel){
                        barChartMonthLabels.add(infoPerMonthLabel.date)
                        barChartExpenseLabels.add(infoPerMonthLabel.monthExpense)
                    }

                    val barChartParams = BarChartParams(barChartEntries,barChartMonthLabels, barChartExpenseLabels)

                    _expenseBarChartParams.postValue(barChartParams)

                    _uiState.value = HomeFragmentState.Success(Pair(formattedInfoPerMonthLabel, formattedInfoPerMonthLabel))
                }

            }catch (error : Exception){
                _uiState.value = HomeFragmentState.Error(error.message.toString())
            }
        }
    }

}