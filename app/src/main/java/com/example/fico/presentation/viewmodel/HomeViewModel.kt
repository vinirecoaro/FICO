package com.example.fico.presentation.viewmodel

import android.os.Build
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.example.fico.DataStoreManager
import com.example.fico.R
import com.example.fico.api.FirebaseAPI
import com.example.fico.api.FormatValuesFromDatabase
import com.example.fico.model.InformationPerMonthExpense
import com.example.fico.util.constants.DateFunctions
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
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
    private val _infoPerMonth = MutableLiveData<List<InformationPerMonthExpense>>()
    val infoPerMonthLiveData : LiveData<List<InformationPerMonthExpense>> = _infoPerMonth
    private val _infoPerMonthLabel = MutableLiveData<List<InformationPerMonthExpense>>()
    val infoPerMonthLabelLiveData : LiveData<List<InformationPerMonthExpense>> = _infoPerMonthLabel
    private val _totalExpense = MutableLiveData<String>()
    val totalExpenseLiveData : LiveData<String> = _totalExpense
    private val _informationPerMonth = MutableLiveData<List<InformationPerMonthExpense>>()
    val informationPerMonthLiveData : LiveData<List<InformationPerMonthExpense>> = _informationPerMonth

    init{
        getInfoPerMonth()
    }

    fun ShowHideValue(text: TextView){
        if (text.inputType == InputType.TYPE_TEXT_VARIATION_PASSWORD) {
            text.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            text.transformationMethod = null
            text.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_visibility_24, 0)
        }
        else{
            text.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
            text.transformationMethod = PasswordTransformationMethod()
            text.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_visibility_off_24, 0)
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

    fun getInfoPerMonth(){
       viewModelScope.async(Dispatchers.IO){
           val infoPerMonthList = dataStore.getExpenseInfoPerMonth()
           val monthWithExpenses = mutableListOf<InformationPerMonthExpense>()
           for(infoPerMonth in infoPerMonthList){
               if (BigDecimal(infoPerMonth.monthExpense).setScale(2,RoundingMode.HALF_UP) != BigDecimal("0").setScale(2,RoundingMode.HALF_UP)){
                   monthWithExpenses.add(infoPerMonth)
               }
           }
           _infoPerMonth.postValue(monthWithExpenses)
       }
    }

    fun formatInfoPerMonthToLabel(){
        val formattedInfoPerMonth = mutableListOf<InformationPerMonthExpense>()
        for(infoPerMonth in _infoPerMonth.value!!){
            formattedInfoPerMonth.add(
                InformationPerMonthExpense(
                    FormatValuesFromDatabase().formatDateAbbreviated(infoPerMonth.date),
                    infoPerMonth.availableNow,
                    infoPerMonth.budget,
                    FormatValuesFromDatabase().price(infoPerMonth.monthExpense)
                )
            )
        }
        _infoPerMonthLabel.value = formattedInfoPerMonth
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentDatePositionBarChart() : Int{
        val currentDate = FormatValuesFromDatabase().formatDateAbbreviated(DateFunctions().getCurrentlyDateYearMonthToDatabase())
        _infoPerMonthLabel.value?.forEachIndexed { index, informationPerMonthExpense ->
            if(informationPerMonthExpense.date == currentDate){
                if(index < 1){
                    return -1
                }else{
                    return index-1
                }
            }
        }
        return _infoPerMonthLabel.value!!.size-1
    }

}