package com.example.fico.api

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.fico.domain.model.Expense
import com.example.fico.domain.model.InformationPerMonthExpense
import kotlinx.coroutines.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import kotlin.collections.HashSet

class ArrangeDataToUpdateToDatabase {

    private val firebaseAPI = FirebaseAPI.instance

    @RequiresApi(Build.VERSION_CODES.N)
    fun calculateUpdatedTotalExpense(expensePrice : String, expenseNOfInstallments: Int, viewModelScope : CoroutineScope, oldExpensePrice : String = "0", oldExpenseNOfInstallments : Int = 1): Deferred<String> {
        var updatedTotalExpense : BigDecimal
        val updatedTotalExpenseString = CompletableDeferred<String>()
        viewModelScope.async(Dispatchers.IO){

            val expenseNOfInstallmentsBigNum = BigDecimal(expenseNOfInstallments)
            val oldExpenseNOfInstallmentsBigNum = BigDecimal(oldExpenseNOfInstallments)
            val currentTotalExpense = firebaseAPI.getTotalExpense().await()
            val bigNumCurrentTotalExpense = BigDecimal(currentTotalExpense)
            val expensePriceBigNum = BigDecimal(expensePrice).multiply(expenseNOfInstallmentsBigNum)
            val oldExpensePriceAnalytic = oldExpensePrice
            val oldExpenseBigNum = BigDecimal(oldExpensePriceAnalytic)
            val oldExpensePriceBigNum = oldExpenseBigNum.multiply(oldExpenseNOfInstallmentsBigNum)

            updatedTotalExpense = bigNumCurrentTotalExpense.add(expensePriceBigNum).subtract(oldExpensePriceBigNum).setScale(8, RoundingMode.HALF_UP)
            updatedTotalExpenseString.complete(updatedTotalExpense.toString())

        }
        return updatedTotalExpenseString
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addToExpenseList(expense : Expense, installment : Boolean, nOfInstallments: Int) : MutableList<Expense>{

        val expenseList : MutableList<Expense> = mutableListOf()
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

                var formattedExpense = formatExpenseToInstallmentExpense(Expense("", expense.price, expense.description, expense.category, expense.inputDate), i)
                val expenseId = "${formattedExpense.inputDate}-${inputTime}-${randonNum}-Parcela-$currentInstallment-${nOfInstallmentsFormatted}"
                formattedExpense.id = expenseId

                expenseList.add(formattedExpense)

            }
        }else{
            val expenseId = "${expense.inputDate}-${inputTime}-${randonNum}"
            val formattedExpense = Expense(expenseId, expense.price, expense.description, expense.category, expense.inputDate)

            expenseList.add(formattedExpense)
        }

        return expenseList
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addToExpenseListFromFileCommonExpense(expenses : MutableList<Expense>) : MutableList<Expense>{

        val expenseList : MutableList<Expense> = mutableListOf()
        val randonNums : MutableList<String> = mutableListOf()

        val inputTime = FormatValuesToDatabase().timeNow()

        var i = 0

        //Generate randon nums to Id and make sure that values aren't equal
        while(i < expenses.size){
            val randonNum = generateRandomAddress(5)
            if(!randonNums.any { it == randonNum }){
                randonNums.add(randonNum)
                i++
            }
        }

        for(expense in expenses){

            val selectedIndex = kotlin.random.Random.nextInt(randonNums.size)

            val randonNumId = randonNums[selectedIndex]

            val expenseId = "${expense.inputDate}-${inputTime}-${randonNumId}"
            val formattedExpense = Expense(expenseId, expense.price, expense.description, expense.category, expense.inputDate)

            expenseList.add(formattedExpense)

            randonNums.removeAt(selectedIndex)
        }

        return expenseList
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun removeFromExpenseList(expense : Expense, viewModelScope: CoroutineScope) : Deferred<MutableList<String>> =
        viewModelScope.async(Dispatchers.IO){

        val expenseIdList : MutableList<String> = mutableListOf()

        val expenseList = firebaseAPI.getExpenseList("").await()
        val commonId = FormatValuesFromDatabase().commonIdOnInstallmentExpense(expense.id)

        for(expenseItem in expenseList.filter { FormatValuesFromDatabase().commonIdOnInstallmentExpense(it.id) == commonId }){
            expenseIdList.add(expenseItem.id)
        }

        return@async expenseIdList
    }

    private fun formatExpenseToInstallmentExpense(expense : Expense, installmentNumber : Int) : Expense {
        val month = expense.inputDate.substring(5,7).toInt()
        var newMonth = month + installmentNumber
        var year = expense.inputDate.substring(0,4).toInt()
        var sumYear : Int = 0
        var day = expense.inputDate.substring(8,10).toInt()
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
        val newExpense = Expense(expense.id,expense.price, newDescription, expense.category, date)

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
    suspend fun addToInformationPerMonth(
        expense : Expense,
        installment : Boolean,
        newExpenseNOfInstallments: Int,
        viewModelScope: CoroutineScope,
        editExpense : Boolean,
        oldExpense : Expense = Expense("","0","","","")
    ) : Deferred<MutableList<InformationPerMonthExpense>> =
        viewModelScope.async(Dispatchers.IO){
            val currentInformationPerMonth = firebaseAPI.getInformationPerMonth().await()
            val newInformationPerMonth = mutableListOf<InformationPerMonthExpense>()
            val defaultBudget = BigDecimal(firebaseAPI.getDefaultBudget().await())
            val defaultBudgetString = defaultBudget.setScale(8, RoundingMode.HALF_UP).toString()

            if(!editExpense){ // Just Add expense price
                for (i in 0 until newExpenseNOfInstallments) {
                    val date = updateInstallmenteExpenseDate(expense.inputDate, i)
                    val existDate = currentInformationPerMonth.any { it.date == date }
                    if (!existDate) {

                        val updatedAvailableNow = defaultBudget.subtract(BigDecimal(expense.price))
                            .setScale(8, RoundingMode.HALF_UP).toString()

                        val monthInfo = InformationPerMonthExpense(
                            date,
                            updatedAvailableNow,
                            defaultBudgetString,
                            expense.price
                        )

                        newInformationPerMonth.add(monthInfo)

                    } else {
                        val currentMonthInfo = currentInformationPerMonth.find { it.date == date }
                        val currentAvailableNow = BigDecimal(currentMonthInfo!!.availableNow)
                        val currentMonthExpense = BigDecimal(currentMonthInfo.monthExpense)

                        val updatedAvailableNow =
                            currentAvailableNow.subtract(BigDecimal(expense.price))
                                .setScale(8, RoundingMode.HALF_UP).toString()
                        val updatedMonthExpense = currentMonthExpense.add(BigDecimal(expense.price))
                            .setScale(8, RoundingMode.HALF_UP).toString()

                        val monthInfo = InformationPerMonthExpense(
                            date,
                            updatedAvailableNow,
                            currentMonthInfo.budget,
                            updatedMonthExpense
                        )

                        newInformationPerMonth.add(monthInfo)

                    }
                }
            }else{ // Remove old expense price and add updated expense price - Edit Expense

                val oldExpenseNOfInstallments : Int = if(installment){
                    FormatValuesFromDatabase().installmentExpenseNofInstallment(oldExpense.id).toInt()
                }else{
                    1
                }

                val oldExpenseMonths = monthsOfInstallmentExpense(oldExpense.inputDate, oldExpenseNOfInstallments)

                val newExpenseMonths = monthsOfInstallmentExpense(expense.inputDate, newExpenseNOfInstallments)

                val months = HashSet<String>(oldExpenseMonths + newExpenseMonths)

                for (month in months){

                    var currentAvailableNow = defaultBudget
                    var currentMonthExpense = BigDecimal("0").setScale(8, RoundingMode.HALF_UP)

                    if (currentInformationPerMonth.any { it.date == month }) {
                        val currentMonthInfo = currentInformationPerMonth.find { it.date == month }

                        if (currentMonthInfo != null) {
                            currentAvailableNow = BigDecimal(currentMonthInfo.availableNow)
                            currentMonthExpense = BigDecimal(currentMonthInfo.monthExpense)
                        }
                    }

                    if(oldExpenseMonths.any { it == month } && newExpenseMonths.any { it == month }){
                        val updatedAvailableNow =
                            currentAvailableNow.add(BigDecimal(oldExpense.price)).subtract(BigDecimal(expense.price))
                                .setScale(8, RoundingMode.HALF_UP).toString()
                        val updatedMonthExpense = currentMonthExpense.subtract(BigDecimal(oldExpense.price)).add(BigDecimal(expense.price))
                            .setScale(8, RoundingMode.HALF_UP).toString()

                        val monthInfo = InformationPerMonthExpense(
                            month,
                            updatedAvailableNow,
                            defaultBudgetString,
                            updatedMonthExpense
                        )
                        newInformationPerMonth.add(monthInfo)
                    }

                    else if(oldExpenseMonths.any { it == month }){
                        val updatedAvailableNow =
                            currentAvailableNow.add(BigDecimal(oldExpense.price))
                                .setScale(8, RoundingMode.HALF_UP).toString()
                        val updatedMonthExpense = currentMonthExpense.subtract(BigDecimal(oldExpense.price))
                            .setScale(8, RoundingMode.HALF_UP).toString()

                        val monthInfo = InformationPerMonthExpense(
                            month,
                            updatedAvailableNow,
                            defaultBudgetString,
                            updatedMonthExpense
                        )
                        newInformationPerMonth.add(monthInfo)
                    }

                    else if(newExpenseMonths.any { it == month }){
                        val updatedAvailableNow =
                            currentAvailableNow.subtract(BigDecimal(expense.price))
                                .setScale(8, RoundingMode.HALF_UP).toString()
                        val updatedMonthExpense = currentMonthExpense.add(BigDecimal(expense.price))
                            .setScale(8, RoundingMode.HALF_UP).toString()

                        val monthInfo = InformationPerMonthExpense(
                            month,
                            updatedAvailableNow,
                            defaultBudgetString,
                            updatedMonthExpense
                        )
                        newInformationPerMonth.add(monthInfo)
                    }
                }
            }
            return@async newInformationPerMonth
        }

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun addToInformationPerMonthFromUpdatedFile(
        expenseList : MutableList<Expense>,
        viewModelScope: CoroutineScope,
    ) : Deferred<MutableList<InformationPerMonthExpense>> =
        viewModelScope.async(Dispatchers.IO){
            val currentInformationPerMonth = firebaseAPI.getInformationPerMonth().await()
            val newInformationPerMonth = mutableListOf<InformationPerMonthExpense>()
            val defaultBudget = BigDecimal(firebaseAPI.getDefaultBudget().await())
            val defaultBudgetString = defaultBudget.setScale(8, RoundingMode.HALF_UP).toString()


            for (expense in expenseList) {
                val date = expense.inputDate.substring(0,7)
                val existDate = currentInformationPerMonth.any { it.date == date }
                if (!existDate) {

                    val updatedAvailableNow = defaultBudget.subtract(BigDecimal(expense.price))
                        .setScale(8, RoundingMode.HALF_UP).toString()

                    val monthInfo = InformationPerMonthExpense(
                        date,
                        updatedAvailableNow,
                        defaultBudgetString,
                        expense.price
                    )

                    newInformationPerMonth.add(monthInfo)

                } else {
                    val currentMonthInfo = currentInformationPerMonth.find { it.date == date }
                    val currentAvailableNow = BigDecimal(currentMonthInfo!!.availableNow)
                    val currentMonthExpense = BigDecimal(currentMonthInfo.monthExpense)

                    val updatedAvailableNow =
                        currentAvailableNow.subtract(BigDecimal(expense.price))
                            .setScale(8, RoundingMode.HALF_UP).toString()
                    val updatedMonthExpense = currentMonthExpense.add(BigDecimal(expense.price))
                        .setScale(8, RoundingMode.HALF_UP).toString()

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

    private fun updateInstallmenteExpenseDate(expenseDate: String, iteraction: Int): String {
        val month = expenseDate.substring(5, 7).toInt()
        var newMonth = month + iteraction
        var year = expenseDate.substring(0, 4).toInt()
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

    fun monthsOfInstallmentExpense(expenseInitialDate: String, nOfInstallments: Int) : MutableList<String>{

        val expenseMonths = mutableListOf<String>()

        for(i in 0 until nOfInstallments){
            val month = updateInstallmenteExpenseDate(expenseInitialDate, i)
            expenseMonths.add(month)
        }

        return expenseMonths
    }

    fun joinExpensesOfMonth(
        expensesList : MutableList<Expense>,
        installment : Boolean,
    ) : MutableList<Expense> {

        val calculatedList : MutableList<Expense> = mutableListOf()
        val months : MutableSet<String> = mutableSetOf()

        if(!installment){
            for(expense in expensesList){
                months.add(expense.inputDate.substring(0,7))
            }
            for(month in months){
                val expensesOfMonth = expensesList.filter { it.inputDate.substring(0,7) == month.substring(0,7) }
                var sumPrices = BigDecimal(0)

                for(expense in expensesOfMonth){
                    val expensePrice = BigDecimal(expense.price)
                    sumPrices = sumPrices.add(expensePrice)
                }

                val abstractExpense = Expense("",sumPrices.toString(),"","","${month}-01")

                calculatedList.add(abstractExpense)
            }
        }else{
            val installmentExpenseList : MutableList<Expense> = mutableListOf()
            for(expense in expensesList){
                for (i in 0 until expense.nOfInstallment.toFloat().toInt()){
                    val price = BigDecimal(expense.price).divide(BigDecimal(expense.nOfInstallment), 8, RoundingMode.HALF_UP).toString()
                    val date = updateInstallmenteExpenseDate(expense.inputDate, i)

                    val newExpense = Expense("", price, expense.description, expense.category, date)

                    installmentExpenseList.add(newExpense)
                }
            }
            for(expense in installmentExpenseList){
                months.add(expense.inputDate.substring(0,7))
            }
            for(month in months){
                val expensesOfMonth = installmentExpenseList.filter { it.inputDate.substring(0,7) == month.substring(0,7) }
                var sumPrices = BigDecimal(0)

                for(expense in expensesOfMonth){
                   sumPrices = sumPrices.add(BigDecimal(expense.price))
                }

                val abstractExpense = Expense("",sumPrices.toString(),"","","${month}-01")

                calculatedList.add(abstractExpense)
            }
        }



        return calculatedList
    }



}