package com.example.fico.ui.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.model.Expense
import com.example.fico.service.FirebaseAPI
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
    suspend fun addExpense2(price: String, description: String, category: String, date: String, installment : Boolean, nOfInstallments: Int = 0) : Deferred<Boolean>{
        return viewModelScope.async(Dispatchers.IO){
            val expenseList = generateExpenseList(price, description, category, date, installment, nOfInstallments)

            val updatedTotalExpense = calculateUpdatedTotalExpense(price, installment).await()

            firebaseAPI.addExpense2(expenseList, installment, nOfInstallments = nOfInstallments, updatedTotalExpense)
        }
    }

    private fun generateRandomAddress(size: Int): String {
        val allowedCharacters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
        val random = Random(System.currentTimeMillis())
        val randomSequence = StringBuilder(size)

        for (i in 0 until size) {
            val index = random.nextInt(allowedCharacters.length)
            randomSequence.append(allowedCharacters[index])
        }

        return randomSequence.toString()
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun generateExpenseList(price: String, description: String, category: String, date: String, installment : Boolean, nOfInstallments: Int) : MutableList<Pair<Expense, String>>{

        val expense = Expense("",price, description, category, date)
        val expenseList : MutableList<Pair<Expense, String>> = mutableListOf()
        val randonNum = generateRandomAddress(5)

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


        if(installment){
            //Generate installment price
            val divisor = BigDecimal(nOfInstallments)
            val denominator = BigDecimal(price)
            val installmentPrice = denominator.divide(divisor, 8, RoundingMode.HALF_UP)
            val correction = BigDecimal("100")
            val installmentPriceFormatted = installmentPrice.divide(correction)
            val formatedNum = installmentPriceFormatted.setScale(8, RoundingMode.HALF_UP)
            val formattedNumString = formatedNum.toString().replace(",",".")

            // Format Number of installments to expenseId
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

                val formattedExpense = formatExpenseToInstallmentExpense(Expense("", formattedNumString, expense.description, expense.category, expense.date), i)
                val expenseId = "${formattedExpense.date}-${inputTime}-${randonNum}-Parcela-$currentInstallment-${nOfInstallmentsFormatted}"

                val newPair = Pair(formattedExpense,expenseId)

                expenseList.add(newPair)

            }
        }else{
            val bigNum = BigDecimal(expense.price)
            val priceFormatted = bigNum.setScale(8, RoundingMode.HALF_UP)
            val expenseId = "${expense.date}-${inputTime}-${randonNum}"
            val formattedExpense = Expense("", priceFormatted.toString(), expense.description, expense.category, expense.date)

            val newPair = Pair(formattedExpense, expenseId)
            expenseList.add(newPair)
        }

        return expenseList
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun calculateUpdatedTotalExpense(expensePrice : String, installment : Boolean): Deferred<String> {
        var updatedTotalExpense : BigDecimal
        val updatedTotalExpenseString = CompletableDeferred<String>()
         viewModelScope.async(Dispatchers.IO){
             if (installment){
                 val currentTotalExpense = firebaseAPI.getTotalExpense().await()
                 val bigNumCurrentTotalExpense = BigDecimal(currentTotalExpense)
                 val denominator = BigDecimal(expensePrice)
                 val correction = BigDecimal("100")
                 val priceFormatted = denominator.divide(correction)
                 updatedTotalExpense = bigNumCurrentTotalExpense.add(priceFormatted).setScale(8, RoundingMode.HALF_UP)
                 updatedTotalExpenseString.complete(updatedTotalExpense.toString())
             }else{
                 val currentTotalExpense = firebaseAPI.getTotalExpense().await()
                 val bigNumCurrentTotalExpense = BigDecimal(currentTotalExpense)
                 val bigNumExpensePrice = BigDecimal(expensePrice)
                 updatedTotalExpense = bigNumCurrentTotalExpense.add(bigNumExpensePrice).setScale(8, RoundingMode.HALF_UP)
                 updatedTotalExpenseString.complete(updatedTotalExpense.toString())
             }
         }
        return updatedTotalExpenseString
    }

    private suspend fun getInformationPerMonth() =
        viewModelScope.async(Dispatchers.IO){
            val infoPerMonth = firebaseAPI.getInformationPerMonth()

    }

}