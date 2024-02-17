package com.example.fico.ui.viewmodel

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
import com.example.fico.R
import com.example.fico.api.FirebaseAPI
import com.example.fico.api.FormatValuesFromDatabase
import com.example.fico.model.InformationPerMonthExpense
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HomeViewModel : ViewModel() {

    private val firebaseAPI = FirebaseAPI.instance
    private val _expenseMonthsLiveData = MutableLiveData<List<String>>()
    val expenseMonthsLiveData: LiveData<List<String>> = _expenseMonthsLiveData
    private val _infoPerMonth = MutableLiveData<List<InformationPerMonthExpense>>()
    val infoPerMonthLiveData : LiveData<List<InformationPerMonthExpense>> = _infoPerMonth
    private val _infoPerMonthLabel = MutableLiveData<List<InformationPerMonthExpense>>()
    val infoPerMonthLabelLiveData : LiveData<List<InformationPerMonthExpense>> = _infoPerMonthLabel

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

    @RequiresApi(Build.VERSION_CODES.N)
    fun getAvailableNow(date: String, formatted : Boolean = true): kotlinx.coroutines.Deferred<String> {
        return viewModelScope.async(Dispatchers.IO) {
            val availableNow = firebaseAPI.getAvailableNow(date)
            if(availableNow == "---"){
                availableNow
            }else{
                if (formatted){
                    val availableNowFormatted = (NumberFormat.getCurrencyInstance().format(availableNow.toFloat()))
                    availableNowFormatted
                }else{
                    availableNow
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getMonthExpense(date: String, formatted : Boolean = true): kotlinx.coroutines.Deferred<String> {
        return viewModelScope.async(Dispatchers.IO) {
            val monthExpense = firebaseAPI.getMonthExpense(date)
            if(monthExpense == "---"){
                monthExpense
            }else{
                if (formatted){
                    val monthExpenseFormatted = (NumberFormat.getCurrencyInstance().format(monthExpense.toFloat()))
                    monthExpenseFormatted
                }else{
                    monthExpense
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getTotalExpense(): kotlinx.coroutines.Deferred<String> {
        return viewModelScope.async(Dispatchers.IO){
            val totalExpense = firebaseAPI.getTotalExpense().await().toFloat()
            val priceFormatted = (NumberFormat.getCurrencyInstance().format(totalExpense))
            priceFormatted.toString()}
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentlyDate() : String{
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        var formattedDate = currentDate.format(formatter)
        val month = formattedDate.substring(3, 5)
        val year = formattedDate.substring(6, 10)
        val date = "$year-$month"
        return date
    }

    fun getExpenseMonths() {
        val _expenseMonths = CompletableDeferred<List<String>>()
        viewModelScope.async {
            val expenseMonths = firebaseAPI.getExpenseMonths(false)
            val sortedExpenseMonths = expenseMonths.sortedByDescending{ it }
            val expenseMonthsFormatted : MutableList<String> = mutableListOf()
            for(expenseMonth in sortedExpenseMonths){
                expenseMonthsFormatted.add(FormatValuesFromDatabase().formatDateForFilterOnExpenseList(expenseMonth))
            }
            _expenseMonths.complete(expenseMonthsFormatted.toList())
            _expenseMonthsLiveData.value = _expenseMonths.await()
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
        viewModelScope.async {
            val infoPerMonthList = firebaseAPI.getInformationPerMonth().await().toList()
            val monthWithExpenses = mutableListOf<InformationPerMonthExpense>()
            for(infoPerMonth in infoPerMonthList){
                if (BigDecimal(infoPerMonth.monthExpense).setScale(2,RoundingMode.HALF_UP) != BigDecimal("0").setScale(2,RoundingMode.HALF_UP)){
                    monthWithExpenses.add(infoPerMonth)
                }
            }
            _infoPerMonth.value = monthWithExpenses
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
        val currentDate = FormatValuesFromDatabase().formatDateAbbreviated(getCurrentlyDate())
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

    fun clearChartEntriesAndLabels(){
        _infoPerMonth.value = emptyList()
        _infoPerMonthLabel.value = emptyList()
    }

}