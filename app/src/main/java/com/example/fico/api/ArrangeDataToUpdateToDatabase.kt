package com.example.fico.api

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.fico.model.Expense
import com.example.fico.model.InformationPerMonthExpense
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

class ArrangeDataToUpdateToDatabase() {

    fun calculateUpdatedTotalExpense(
        currentTotalExpense: String,
        expensePrice: String,
        expenseNOfInstallments: Int,
        oldExpensePrice: String = "0",
        oldExpenseNOfInstallments: Int = 1
    ): String {
        val updatedTotalExpense: BigDecimal

        val expenseNOfInstallmentsBigNum = BigDecimal(expenseNOfInstallments)
        val oldExpenseNOfInstallmentsBigNum = BigDecimal(oldExpenseNOfInstallments)
        val bigNumCurrentTotalExpense = BigDecimal(currentTotalExpense)
        val expensePriceBigNum = BigDecimal(expensePrice).multiply(expenseNOfInstallmentsBigNum)
        val oldExpenseBigNum = BigDecimal(oldExpensePrice)
        val oldExpensePriceBigNum = oldExpenseBigNum.multiply(oldExpenseNOfInstallmentsBigNum)

        updatedTotalExpense =
            bigNumCurrentTotalExpense.add(expensePriceBigNum).subtract(oldExpensePriceBigNum)
                .setScale(8, RoundingMode.HALF_UP)

        return updatedTotalExpense.toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addToExpenseList(expense : Expense, installment : Boolean, nOfInstallments: Int, isEdit : Boolean) : MutableList<Expense>{

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

                var formattedExpense = formatExpenseToInstallmentExpense(Expense("", expense.price, expense.description, expense.category, expense.paymentDate, expense.purchaseDate, expense.inputDateTime), i)

                if(isEdit){
                    val commonExpenseId = FormatValuesFromDatabase().commonIdOnInstallmentExpense(expense.id)
                    val expenseId = "${formattedExpense.paymentDate}-${commonExpenseId}-Parcela-$currentInstallment-${nOfInstallmentsFormatted}"

                    formattedExpense.id = expenseId
                }else{
                    val expenseId = "${formattedExpense.paymentDate}-${inputTime}-${randonNum}-Parcela-$currentInstallment-${nOfInstallmentsFormatted}"

                    formattedExpense.id = expenseId
                }

                expenseList.add(formattedExpense)

            }
        }else{

            val formattedExpense = Expense("", expense.price, expense.description, expense.category, expense.paymentDate, expense.purchaseDate, expense.inputDateTime)

            if (isEdit){
                formattedExpense.id = expense.id
            }else{
                val expenseId = "${formattedExpense.paymentDate}-${inputTime}-${randonNum}"
                formattedExpense.id = expenseId
            }

            expenseList.add(formattedExpense)
        }

        return expenseList
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addToExpenseListFromFileCommonExpense(expenses : MutableList<Expense>) : MutableList<Expense>{

        val expenseList : MutableList<Expense> = mutableListOf()
        val randonNums : MutableList<String> = mutableListOf()
        val inputDateTime = FormatValuesToDatabase().dateTimeNow()

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

            val expenseId = "${expense.purchaseDate}-${inputTime}-${randonNumId}"
            val formattedExpense = Expense(expenseId, expense.price, expense.description, expense.category, expense.paymentDate, expense.purchaseDate, inputDateTime)

            expenseList.add(formattedExpense)

            randonNums.removeAt(selectedIndex)
        }

        return expenseList
    }

    fun removeFromExpenseList(expense : Expense, currentExpenseList : List<Expense>) : MutableList<String>{

        val expenseIdList : MutableList<String> = mutableListOf()

        val commonId = FormatValuesFromDatabase().commonIdOnInstallmentExpense(expense.id)

        for(expenseItem in currentExpenseList.filter { FormatValuesFromDatabase().commonIdOnInstallmentExpense(it.id) == commonId }){
            expenseIdList.add(expenseItem.id)
        }

        return expenseIdList
    }

    private fun formatExpenseToInstallmentExpense(expense : Expense, installmentNumber : Int) : Expense {
        val month = expense.paymentDate.substring(5,7).toInt()
        var newMonth = month + installmentNumber
        var year = expense.paymentDate.substring(0,4).toInt()
        var sumYear : Int = 0
        var day = expense.paymentDate.substring(8,10).toInt()
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

        val paymentDate = "$year-$newMonthFormatted-$dayFormatted"
        val newExpense = Expense(expense.id,expense.price, newDescription, expense.category, paymentDate, expense.purchaseDate, expense.inputDateTime)

        return newExpense
    }

    fun generateRandomAddress(size: Int): String {
        val allowedCharacters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
        val random = Random(System.currentTimeMillis())
        val randomSequence = StringBuilder(size)

        for (i in 0 until size) {
            val index = random.nextInt(allowedCharacters.length)
            randomSequence.append(allowedCharacters[index])
        }

        return randomSequence.toString()
    }

    fun addToInformationPerMonth(
        expense : Expense,
        installment : Boolean,
        newExpenseNOfInstallments: Int,
        currentInfoPerMonth : List<InformationPerMonthExpense>,
        defaultBudget : String,
        editExpense : Boolean,
        oldExpense : Expense = Expense("","0","","","","","")
    ) : MutableList<InformationPerMonthExpense> {
            val newInformationPerMonth = mutableListOf<InformationPerMonthExpense>()
            val defaultBudgetBigDecimal = BigDecimal(defaultBudget)
            val defaultBudgetString = defaultBudgetBigDecimal.setScale(8, RoundingMode.HALF_UP).toString()

            if(!editExpense){ // Just Add expense price
                for (i in 0 until newExpenseNOfInstallments) {
                    val date = updateInstallmentExpenseDate(expense.paymentDate, i)
                    val existDate = currentInfoPerMonth.any { it.date == date }
                    if (!existDate) {

                        val updatedAvailableNow = defaultBudgetBigDecimal.subtract(BigDecimal(expense.price))
                            .setScale(8, RoundingMode.HALF_UP).toString()

                        val monthInfo = InformationPerMonthExpense(
                            date,
                            updatedAvailableNow,
                            defaultBudgetString,
                            expense.price
                        )

                        newInformationPerMonth.add(monthInfo)

                    } else {
                        val currentMonthInfo = currentInfoPerMonth.find { it.date == date }
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
            }else{
                // Remove old expense price and add updated expense price - Edit Expense
                val oldExpenseNOfInstallments : Int = if(installment){
                    FormatValuesFromDatabase().installmentExpenseNofInstallment(oldExpense.id).toInt()
                }else{
                    1
                }

                val oldExpenseMonths = monthsOfInstallmentExpense(oldExpense.paymentDate, oldExpenseNOfInstallments)

                val newExpenseMonths = monthsOfInstallmentExpense(expense.paymentDate, newExpenseNOfInstallments)

                val months = HashSet<String>(oldExpenseMonths + newExpenseMonths)

                for (month in months){

                    var currentAvailableNow = defaultBudgetBigDecimal
                    var currentMonthExpense = BigDecimal("0").setScale(8, RoundingMode.HALF_UP)

                    if (currentInfoPerMonth.any { it.date == month }) {
                        val currentMonthInfo = currentInfoPerMonth.find { it.date == month }

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

            return newInformationPerMonth
        }

    fun addToInformationPerMonthFromUpdatedFile(
        monthExpenseList : MutableList<Pair<String,String>>,
        currentInfoPerMonth : List<InformationPerMonthExpense>,
        defaultBudget : String
    ) : MutableList<InformationPerMonthExpense>{
            val newInformationPerMonth = mutableListOf<InformationPerMonthExpense>()
            val defaultBudgetBigDecimal = BigDecimal(defaultBudget)
            val defaultBudgetString = defaultBudgetBigDecimal.setScale(8, RoundingMode.HALF_UP).toString()

            for (monthExpense in monthExpenseList) {
                val date = monthExpense.first
                val existDate = currentInfoPerMonth.any { it.date == date }
                val monthExpenseValue = monthExpense.second

                if (!existDate) {

                    val updatedAvailableNow = defaultBudgetBigDecimal.subtract(BigDecimal(monthExpenseValue))
                        .setScale(8, RoundingMode.HALF_UP).toString()

                    val monthInfo = InformationPerMonthExpense(
                        date,
                        updatedAvailableNow,
                        defaultBudgetString,
                        monthExpenseValue
                    )

                    newInformationPerMonth.add(monthInfo)

                } else {
                    val currentMonthInfo = currentInfoPerMonth.find { it.date == date }
                    val currentAvailableNow = BigDecimal(currentMonthInfo!!.availableNow)
                    val currentMonthExpense = BigDecimal(currentMonthInfo.monthExpense)

                    val updatedAvailableNow =
                        currentAvailableNow.subtract(BigDecimal(monthExpenseValue))
                            .setScale(8, RoundingMode.HALF_UP).toString()
                    val updatedMonthExpense = currentMonthExpense.add(BigDecimal(monthExpenseValue))
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
            return newInformationPerMonth
        }

    private fun updateInstallmentExpenseDate(expenseDate: String, iteraction: Int): String {
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
            val month = updateInstallmentExpenseDate(expenseInitialDate, i)
            expenseMonths.add(month)
        }

        return expenseMonths
    }

    fun joinExpensesOfMonth(
        expensesList : MutableList<Expense>,
        installmentExpenseList : MutableList<Expense>
    ) : MutableList<Pair<String,String>> {

        val calculatedList : MutableList<Pair<String,String>> = mutableListOf()
        val months : MutableSet<String> = mutableSetOf()

        //Common expense
        for(expense in expensesList){
            months.add(expense.paymentDate.substring(0,7))
        }
        for(month in months){
            val expensesOfMonth = expensesList.filter { it.paymentDate.substring(0,7) == month.substring(0,7) }
            var sumPrices = BigDecimal(0)

            for(expense in expensesOfMonth){
                val expensePrice = BigDecimal(expense.price)
                sumPrices = sumPrices.add(expensePrice)
            }

            val monthExpense = Pair(month, sumPrices.toString())

            calculatedList.add(monthExpense)
        }

        //Installment expense
        val installmentExpenseListFormatted = mutableListOf<Expense>()
        for(expense in installmentExpenseList){
            for (i in 0 until expense.nOfInstallment.toFloat().toInt()){
                val price = BigDecimal(expense.price).divide(BigDecimal(expense.nOfInstallment), 8, RoundingMode.HALF_UP).toString()
                val date = updateInstallmentExpenseDate(expense.paymentDate, i)

                val newExpense = Expense("", price, expense.description, expense.category, date, expense.purchaseDate, expense.inputDateTime)

                installmentExpenseListFormatted.add(newExpense)
            }
        }
        for(expense in installmentExpenseListFormatted){
            months.add(expense.paymentDate.substring(0,7))
        }
        for(month in months){
            val expensesOfMonth = installmentExpenseListFormatted.filter { it.paymentDate.substring(0,7) == month.substring(0,7) }
            var sumPrices = BigDecimal(0)

            for(expense in expensesOfMonth){
               sumPrices = sumPrices.add(BigDecimal(expense.price))
            }

            val monthExpense = Pair(month, sumPrices.toString())

            val month = calculatedList.find { it.first == month }
            if(month != null){
                val expenseMonth = BigDecimal(month.second).add(BigDecimal(monthExpense.second)).toString()
                val newMonthExpense = Pair(month.first, expenseMonth)
                calculatedList.remove(month)
                calculatedList.add(newMonthExpense)
            }else{
                calculatedList.add(monthExpense)
            }

        }

        return calculatedList
    }



}