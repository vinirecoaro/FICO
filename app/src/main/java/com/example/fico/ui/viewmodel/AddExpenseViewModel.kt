package com.example.fico.ui.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.model.Expense
import com.example.fico.service.FirebaseAPI
import com.example.fico.service.constants.AppConstants
import kotlinx.coroutines.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

class AddExpenseViewModel : ViewModel() {

    private val firebaseAPI = FirebaseAPI.instance

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun addExpense(price: String, description: String, category: String, date: String) : Deferred<Boolean> {
        return viewModelScope.async(Dispatchers.IO){
            val expense = Expense("", price, description, category, date)
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
            firebaseAPI.addExpense(expense, inputTime)
        }
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
    suspend fun addInstallmentsExpense(price: String, description: String, category: String, date: String, nOfInstallments: Int) : Deferred<Boolean>{
        return viewModelScope.async(Dispatchers.IO){
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
    }


    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun checkIfExistDefaultBudget() : Deferred<Boolean> {
        return viewModelScope.async(Dispatchers.IO){
            firebaseAPI.checkIfExistDefaultBudget()
        }
    }

    suspend fun setDefaultBudget(budget: String) : Deferred<Boolean> {
        return viewModelScope.async(Dispatchers.IO) {
            val bigNum = BigDecimal(budget)
            val formattedBudget = bigNum.setScale(8, RoundingMode.HALF_UP).toString()
            firebaseAPI.setDefaultBudget(formattedBudget)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addExpense2(price: String, description: String, category: String, date: String){

        val expense = Expense("", price, description, category, date)
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

        val updates = mutableMapOf<String, Any>()
        val expenseId = generateRandomAddress(5)
        val bigNum = BigDecimal(expense.price)
        val priceFormatted = bigNum.setScale(8, RoundingMode.HALF_UP)

        updates["${expense.date}-${inputTime}-${expenseId}/${AppConstants.DATABASE.PRICE}"] = priceFormatted.toString()
        updates["${expense.date}-${inputTime}-${expenseId}/${AppConstants.DATABASE.DESCRIPTION}"] = expense.description
        updates["${expense.date}-${inputTime}-${expenseId}/${AppConstants.DATABASE.DATE}"] = expense.date
        updates["${expense.date}-${inputTime}-${expenseId}/${AppConstants.DATABASE.CATEGORY}"] = expense.category

        firebaseAPI.updateExpenseList2(updates)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addInstallmentsExpense2(price: String, description: String, category: String, date: String, nOfInstallments: Int) {
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

        val updates = mutableMapOf<String, Any>()
        val expenseId = generateRandomAddress(5)
        val bigNum = BigDecimal(expense.price)
        val priceFormatted = bigNum.setScale(8, RoundingMode.HALF_UP)

        var nOfInstallmentsFormatted = nOfInstallments.toString()
        if(nOfInstallments < 10){
            nOfInstallmentsFormatted = "00$nOfInstallmentsFormatted"
        }else if(nOfInstallments < 100){
            nOfInstallmentsFormatted = "0$nOfInstallmentsFormatted"
        }

        for(i in 0 until nOfInstallments){

            var currentInstallment = "${i+1}"
            if(i+1 < 10){
                currentInstallment = "00$currentInstallment"
            }else if(i+1 < 100){
                currentInstallment = "0$currentInstallment"
            }

            val formattedExpense = formatExpenseToInstallmentExpense(expense, i)

            updates["${formattedExpense.date}-${inputTime}-${expenseId}-Parcela-$currentInstallment-${nOfInstallmentsFormatted}/${AppConstants.DATABASE.PRICE}"] = priceFormatted.toString()
            updates["${formattedExpense.date}-${inputTime}-${expenseId}-Parcela-$currentInstallment-${nOfInstallmentsFormatted}/${AppConstants.DATABASE.DESCRIPTION}"] = formattedExpense.description
            updates["${formattedExpense.date}-${inputTime}-${expenseId}-Parcela-$currentInstallment-${nOfInstallmentsFormatted}/${AppConstants.DATABASE.DATE}"] = formattedExpense.date
            updates["${formattedExpense.date}-${inputTime}-${expenseId}-Parcela-$currentInstallment-${nOfInstallmentsFormatted}/${AppConstants.DATABASE.CATEGORY}"] = formattedExpense.category
        }

        firebaseAPI.updateExpenseList2(updates)
    }

    private fun generateRandomAddress(size: Int): String {
        val caracteresPermitidos = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
        val random = Random(System.currentTimeMillis())
        val sequenciaAleatoria = StringBuilder(size)

        for (i in 0 until size) {
            val index = random.nextInt(caracteresPermitidos.length)
            sequenciaAleatoria.append(caracteresPermitidos[index])
        }

        return sequenciaAleatoria.toString()
    }

    private fun formatExpenseToInstallmentExpense(expense : Expense, installmentNumber : Int) : Expense{
        val month = expense.date.substring(5,7).toInt()
        var newMonth = month + installmentNumber
        var year = expense.date.substring(0,4).toInt()
        var sumYear : Int = 0
        var day = expense.date.substring(8,10).toInt()
        var newDescription = expense.description + " Parcela ${installmentNumber+1}"
        if(newMonth > 12 ){
            if(newMonth % 12 == 0){
                sumYear = newMonth/12 - 1
                newMonth -= 12*sumYear
            }else{
                sumYear = newMonth/12
                newMonth -= 12*sumYear
            }
            if(newMonth == 2){
                if (day > 28){
                    day = 28
                }
            }
            year += sumYear
        }
        var newMonthFormatted = newMonth.toString()
        if(newMonth < 10){
            newMonthFormatted = "0$newMonth"
        }
        var dayFormatted = day.toString()
        if(day < 10){
            dayFormatted = "0$day"
        }

        val date = "$year-$newMonthFormatted-$dayFormatted"
        val newExpense = Expense("",expense.price, newDescription, expense.category, date)

        return newExpense
    }

}