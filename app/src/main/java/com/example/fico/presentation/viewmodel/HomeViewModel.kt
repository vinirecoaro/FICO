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
import com.example.fico.api.FirebaseAPI
import com.example.fico.api.FormatValuesFromDatabase
import com.example.fico.api.FormatValuesToDatabase
import com.example.fico.model.BarChartParams
import com.example.fico.model.InformationPerMonthExpense
import com.example.fico.presentation.fragments.home.HomeExpensesFragmentState
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

@RequiresApi(Build.VERSION_CODES.N)
class HomeViewModel(
    private val firebaseAPI : FirebaseAPI,
    private val dataStore : DataStoreManager
) : ViewModel() {

    private val _expenseMonthsLiveData = MutableLiveData<List<String>>()
    val expenseMonthsLiveData: LiveData<List<String>> = _expenseMonthsLiveData
    private val _infoPerMonthLabel = MutableLiveData<List<InformationPerMonthExpense>>()
    private val _totalExpense = MutableLiveData<String>()
    val totalExpenseLiveData : LiveData<String> = _totalExpense
    private val _informationPerMonth = MutableLiveData<List<InformationPerMonthExpense>>()
    val informationPerMonthLiveData : LiveData<List<InformationPerMonthExpense>> = _informationPerMonth
    private val _expensePerCategory = MutableLiveData<List<Pair<String, Double>>>()
    val expensePerCategory : LiveData<List<Pair<String, Double>>> = _expensePerCategory
    private val pieChartPaletteColors = listOf(
        Color.rgb(50, 111, 168),
        Color.rgb(168, 83, 50),
        Color.rgb(0,150,0),
        Color.rgb(168, 135, 50),
        Color.rgb(107, 50, 168)
    )
    private val _uiState = MutableStateFlow<HomeExpensesFragmentState<Pair<List<InformationPerMonthExpense>, List<InformationPerMonthExpense>>>>(
        HomeExpensesFragmentState.Loading)
    val uiState : StateFlow<HomeExpensesFragmentState<Pair<List<InformationPerMonthExpense>, List<InformationPerMonthExpense>>>> = _uiState.asStateFlow()
    private val _isBlurred = MutableLiveData<Boolean>(true)
    val isBlurred : LiveData<Boolean> = _isBlurred
    private val _isFirstLoad = MutableLiveData<Boolean>(true)
    val isFirstLoad : LiveData<Boolean> = _isFirstLoad
    private val _expenseBarChartParams = MutableLiveData<BarChartParams>(BarChartParams.empty())
    val expenseBarChartParams : LiveData<BarChartParams> = _expenseBarChartParams

    fun changeBlurState(){
        if(_isBlurred.value != null){
            _isBlurred.postValue(!_isBlurred.value!!)
        }
    }

    fun getAvailableNow(date : String, formatted: Boolean = true) : Deferred<String>{
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

    fun getMonthExpense(date : String, formatted: Boolean = true) : Deferred<String>{
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

    fun getTotalExpense(){
        viewModelScope.async(Dispatchers.IO){
            val totalExpense = dataStore.getTotalExpense()
            val price = totalExpense.toFloat()
            val priceFormatted = NumberFormat.getCurrencyInstance().format(price)
            _totalExpense.postValue(priceFormatted)
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
        }
    }

    fun getPieChartCategoriesColors() : List<Int>{
        return pieChartPaletteColors
    }

    fun getExpenseBarChartParams(){

        _uiState.value = HomeExpensesFragmentState.Loading

        viewModelScope.async(Dispatchers.IO){

            val barChartEntries : ArrayList<BarEntry> = arrayListOf()
            val formattedInfoPerMonthLabel = mutableListOf<InformationPerMonthExpense>()
            val barChartMonthLabels : MutableSet<String> = mutableSetOf()
            val barChartExpenseLabels : MutableSet<String> = mutableSetOf()

            try {
                val infoPerMonthList = dataStore.getExpenseInfoPerMonth()
                val monthWithExpenses = mutableListOf<InformationPerMonthExpense>()
                for(infoPerMonth in infoPerMonthList){
                    if (BigDecimal(infoPerMonth.monthExpense).setScale(2,RoundingMode.HALF_UP) != BigDecimal("0").setScale(2,RoundingMode.HALF_UP)){
                        monthWithExpenses.add(infoPerMonth)
                    }
                }
                if(monthWithExpenses.isEmpty()){
                    _uiState.value = HomeExpensesFragmentState.Empty
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

                    _uiState.value = HomeExpensesFragmentState.Success(Pair(formattedInfoPerMonthLabel, formattedInfoPerMonthLabel))
                }

            }catch (error : Exception){
                _uiState.value = HomeExpensesFragmentState.Error(error.message.toString())
            }
        }
    }

}