package com.example.fico.api

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewModelScope
import com.example.fico.model.Expense
import com.example.fico.model.InformationPerMonthExpense
import com.example.fico.util.constants.AppConstants
import kotlinx.coroutines.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

class ArrangeDataToUpdateToDatabase {

    private val firebaseAPI = FirebaseAPI.instance

    @RequiresApi(Build.VERSION_CODES.N)
    fun calculateUpdatedTotalExpense(expensePrice : String, nOfInstallments: Int, operation : String, viewModelScope : CoroutineScope): Deferred<String> {
        var updatedTotalExpense : BigDecimal
        val updatedTotalExpenseString = CompletableDeferred<String>()
        viewModelScope.async(Dispatchers.IO){

            val currentTotalExpense = firebaseAPI.getTotalExpense().await()
            val bigNumCurrentTotalExpense = BigDecimal(currentTotalExpense)
            val expensePriceBigNum = BigDecimal(expensePrice).multiply(BigDecimal(nOfInstallments))

            when (operation) {
                AppConstants.OPERATIONS.SUM -> {
                    updatedTotalExpense = bigNumCurrentTotalExpense.add(expensePriceBigNum).setScale(8, RoundingMode.HALF_UP)
                    updatedTotalExpenseString.complete(updatedTotalExpense.toString())
                }
                AppConstants.OPERATIONS.SUB -> {
                    updatedTotalExpense = bigNumCurrentTotalExpense.subtract(expensePriceBigNum).setScale(8, RoundingMode.HALF_UP)
                    updatedTotalExpenseString.complete(updatedTotalExpense.toString())
                }
                else -> {
                    updatedTotalExpenseString.complete("0")
                }
            }
        }
        return updatedTotalExpenseString
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun expenseList(expense : Expense, installment : Boolean, nOfInstallments: Int) : MutableList<Pair<Expense, String>>{

        val expenseList : MutableList<Pair<Expense, String>> = mutableListOf()
        val randonNum = generateRandomAddress(5)

        val inputTime = FormatValuesToDatabase().timeNow()

        if(installment){

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

                val formattedExpense = formatExpenseToInstallmentExpense(Expense("", expense.price, expense.description, expense.category, expense.date), i)
                val expenseId = "${formattedExpense.date}-${inputTime}-${randonNum}-Parcela-$currentInstallment-${nOfInstallmentsFormatted}"

                val newPair = Pair(formattedExpense,expenseId)

                expenseList.add(newPair)

            }
        }else{
            val expenseId = "${expense.date}-${inputTime}-${randonNum}"
            val formattedExpense = Expense("", expense.price, expense.description, expense.category, expense.date)

            val newPair = Pair(formattedExpense, expenseId)
            expenseList.add(newPair)
        }

        return expenseList
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

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun informationPerMonth(expense : Expense, nOfInstallments: Int, viewModelScope: CoroutineScope) : Deferred<MutableList<InformationPerMonthExpense>> =
        viewModelScope.async(Dispatchers.IO){
            val currentInformationPerMonth = firebaseAPI.getInformationPerMonth().await()
            val newInformationPerMonth = mutableListOf<InformationPerMonthExpense>()
            val defaultBudget = BigDecimal(firebaseAPI.getDefaultBudget().await())
            val defaultBudgetString = defaultBudget.setScale(8, RoundingMode.HALF_UP).toString()

            for(i in 0 until nOfInstallments){
                val date = updateInstallmenteExpenseDate(expense, i)
                val existDate = currentInformationPerMonth.any{it.date == date}
                if(!existDate){

                    val updatedAvailableNow = defaultBudget.subtract(BigDecimal(expense.price)).setScale(8, RoundingMode.HALF_UP).toString()

                    val monthInfo = InformationPerMonthExpense(
                        date,
                        updatedAvailableNow,
                        defaultBudgetString,
                        expense.price
                    )

                    newInformationPerMonth.add(monthInfo)

                }else{
                    val currentMonthInfo = currentInformationPerMonth.find {it.date == date}
                    val currentAvailableNow = BigDecimal(currentMonthInfo!!.availableNow)
                    val currentMonthExpense = BigDecimal(currentMonthInfo.monthExpense)

                    val updatedAvailableNow = currentAvailableNow.subtract(BigDecimal(expense.price)).setScale(8, RoundingMode.HALF_UP).toString()
                    val updatedMonthExpense = currentMonthExpense.add(BigDecimal(expense.price)).setScale(8, RoundingMode.HALF_UP).toString()

                    val monthInfo = InformationPerMonthExpense(
                        date,
                        updatedAvailableNow,
                        defaultBudgetString,
                        updatedMonthExpense
                    )

                    newInformationPerMonth.add(monthInfo)

                }
            }

            return@async newInformationPerMonth
        }

    private fun updateInstallmenteExpenseDate(expense: Expense, iteraction: Int): String {
        val month = expense.date.substring(5, 7).toInt()
        var newMonth = month + iteraction
        var year = expense.date.substring(0, 4).toInt()
        var sumYear: Int = 0
        if (newMonth > 12) {
            if (newMonth % 12 == 0) {
                sumYear = newMonth / 12 - 1
                newMonth -= 12 * sumYear
            } else {
                sumYear = newMonth / 12
                newMonth -= 12 * sumYear
            }
            year += sumYear
        }
        var newMonthFormatted = newMonth.toString()
        if (newMonth < 10) {
            newMonthFormatted = "0$newMonth"
        }

        return "$year-$newMonthFormatted"
    }

}