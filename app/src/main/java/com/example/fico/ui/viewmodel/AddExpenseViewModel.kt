package com.example.fico.ui.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.model.Expense
import com.example.fico.service.FirebaseAPI
import kotlinx.coroutines.*
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.CompletableFuture

class AddExpenseViewModel : ViewModel() {

    private val firebaseAPI = FirebaseAPI.instance

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun addExpense(price: String, description: String, category: String, date: String)=
        viewModelScope.async(Dispatchers.IO){
        val expense = Expense("", price, description, category, date)
        val timeNow = LocalTime.now()
        val inputTime = "${timeNow.hour}-${timeNow.minute}-${timeNow.second}"
        firebaseAPI.addExpense(expense, inputTime)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun checkIfExistsDateOnDatabse(date: String): Deferred<Boolean> {
        return viewModelScope.async(Dispatchers.IO){
            firebaseAPI.checkIfExistsDateOnDatabse(date)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentlyDate() : String{
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return currentDate.format(formatter)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun addInstallmentsExpense(price: String, description: String, category: String, date: String, nOfInstallments: Int)=
        viewModelScope.async(Dispatchers.IO){
            val expense = Expense("",price, description, category, date)
            val timeNow = LocalTime.now()
            var hour = timeNow.hour.toString()
            var minute = timeNow.minute.toString()
            var second = timeNow.second.toString()
            if(timeNow.hour < 10){
                hour = "0${timeNow.hour}"
            }
            if(timeNow.minute < 10){
                minute = "0${timeNow.minute}"
            }
            if(timeNow.second < 10){
                second = "0${timeNow.second}"
            }
            val inputTime = "${hour}-${minute}-${second}"
            firebaseAPI.addInstallmentExpense(expense,inputTime,nOfInstallments)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun checkIfExistDefaultBudget() : Deferred<Boolean> {
        return viewModelScope.async(Dispatchers.IO){
            firebaseAPI.checkIfExistDefaultBudget()
        }
    }

    suspend fun setDefaultBudget(budget: String) : Deferred<Boolean> {
        return viewModelScope.async(Dispatchers.IO) {
            val formatNum = DecimalFormat("#.##")
            val formattedBudget = formatNum.format(budget.toFloat()).toString().replace(",",".")
            firebaseAPI.setDefaultBudget(formattedBudget)
        }
    }


}