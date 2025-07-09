package com.example.fico.api

import com.example.fico.model.CreditCard
import com.example.fico.model.CreditCardColors
import com.example.fico.model.Earning
import com.example.fico.model.Expense
import com.example.fico.model.InformationPerMonthExpense
import com.example.fico.model.RecurringTransaction
import com.example.fico.model.UploadTransactionFromFileInfo
import com.example.fico.model.ValuePerMonth
import com.example.fico.utils.constants.StringConstants
import com.google.firebase.database.DataSnapshot
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat

class FormatValuesFromDatabase {

    fun installmentExpensePrice(expensePrice : String, expenseId : String) : String{
        val regex = Regex("[\\d,.]+")
        val expenseInstallment = BigDecimal(regex.find(expensePrice)!!.value.replace(",","."))
        val nOfInstallment = BigDecimal(expenseId.substring(38,41))
        val price = expenseInstallment.multiply(nOfInstallment)
        return (NumberFormat.getCurrencyInstance().format(price))
    }

    fun price(expensePrice : String) : String{
        val regex = Regex("[\\d,.]+")
        val justNumber = BigDecimal(regex.find(expensePrice)!!.value.replace(",","."))
        return (NumberFormat.getCurrencyInstance().format(justNumber))
    }

    fun date(date : String) : String{
        return "${date.substring(8, 10)}/${date.substring(5, 7)}/${date.substring(0, 4)}"
    }

    fun priceToFile(expensePrice : String) : String{
        val regex = Regex("[\\d,.]+")
        val justNumber = BigDecimal(regex.find(expensePrice)!!.value.replace(",","."))
        val numberWithLocationFormat = NumberFormat.getCurrencyInstance().format(justNumber)
        return (regex.find(numberWithLocationFormat)!!.value)
    }

    fun installmentExpenseDescription(expenseDescription : String) : String{
        return expenseDescription.split(" Parcela")[0]
    }

    fun installmentExpenseNofInstallment(expenseId : String) : String{
        return expenseId.substring(38,41).toInt().toString()
    }

    fun installmentExpenseCurrentInstallment(expenseId : String) : String{
        return expenseId.split("-")[8]
    }

    fun installmentExpenseInitialDate(id: String, date: String) : String{
        val currentInstallment = id.substring(35,37).toInt().toString()
        var day = date.substring(0, 2)
        val month = date.substring(3, 5)
        val year = date.substring(6, 10)
        var initialYear = year.toInt() - (currentInstallment.toInt()/12)
        var initialMonth = 1
        var initialMonthString = ""

        val restAfterDivideTwelve = currentInstallment.toInt()%12

        if(month.toInt() < restAfterDivideTwelve){
            initialMonth = 12 + (month.toInt() - restAfterDivideTwelve) + 1
            initialMonthString = initialMonth.toString()
            initialYear -= 1
        }else if(month.toInt() > restAfterDivideTwelve){
            initialMonth = month.toInt() - restAfterDivideTwelve + 1
            initialMonthString = initialMonth.toString()
        }
        if(day.toInt() < 10){
            day = "0${day.toInt()}"
        }
        if(initialMonth < 10){
            initialMonthString = "0${initialMonth}"
        }

        return "${day}/${initialMonthString}/${initialYear}"
    }

    fun commonIdOnInstallmentExpense(installmentExpenseId : String) : String{
        return installmentExpenseId.substring(11,25)
    }

    fun formatDateForFilterOnExpenseList(date: String) : String{
        var formattedDate = ""
        val month = date.substring(5,7)
        when (month) {
            "01" -> {
                formattedDate = "Janeiro - ${date.substring(0,4)}"
            }
            "02" -> {
                formattedDate = "Fevereiro - ${date.substring(0,4)}"
            }
            "03" -> {
                formattedDate = "Março - ${date.substring(0,4)}"
            }
            "04" -> {
                formattedDate = "Abril - ${date.substring(0,4)}"
            }
            "05" -> {
                formattedDate = "Maio - ${date.substring(0,4)}"
            }
            "06" -> {
                formattedDate = "Junho - ${date.substring(0,4)}"
            }
            "07" -> {
                formattedDate = "Julho - ${date.substring(0,4)}"
            }
            "08" -> {
                formattedDate = "Agosto - ${date.substring(0,4)}"
            }
            "09" -> {
                formattedDate = "Setembro - ${date.substring(0,4)}"
            }
            "10" -> {
                formattedDate = "Outubro - ${date.substring(0,4)}"
            }
            "11" -> {
                formattedDate = "Novembro - ${date.substring(0,4)}"
            }
            "12" -> {
                formattedDate = "Dezembro - ${date.substring(0,4)}"
            }
        }
        return formattedDate
    }

    fun formatMonthAbbreviatedWithDash(date: String) : String{
        var formattedDate = ""
        val month = date.substring(5,7)
        when (month) {
            "01" -> {
                formattedDate = "Jan-${date.substring(2,4)}"
            }
            "02" -> {
                formattedDate = "Fev-${date.substring(2,4)}"
            }
            "03" -> {
                formattedDate = "Mar-${date.substring(2,4)}"
            }
            "04" -> {
                formattedDate = "Abr-${date.substring(2,4)}"
            }
            "05" -> {
                formattedDate = "Mai-${date.substring(2,4)}"
            }
            "06" -> {
                formattedDate = "Jun-${date.substring(2,4)}"
            }
            "07" -> {
                formattedDate = "Jul-${date.substring(2,4)}"
            }
            "08" -> {
                formattedDate = "Ago-${date.substring(2,4)}"
            }
            "09" -> {
                formattedDate = "Set-${date.substring(2,4)}"
            }
            "10" -> {
                formattedDate = "Out-${date.substring(2,4)}"
            }
            "11" -> {
                formattedDate = "Nov-${date.substring(2,4)}"
            }
            "12" -> {
                formattedDate = "Dez-${date.substring(2,4)}"
            }
        }
        return formattedDate
    }

    fun formatMonthAbbreviatedWithBar(date: String) : String{
        var formattedDate = ""
        val month = date.substring(5,7)
        when (month) {
            "01" -> {
                formattedDate = "Jan/${date.substring(2,4)}"
            }
            "02" -> {
                formattedDate = "Fev/${date.substring(2,4)}"
            }
            "03" -> {
                formattedDate = "Mar/${date.substring(2,4)}"
            }
            "04" -> {
                formattedDate = "Abr/${date.substring(2,4)}"
            }
            "05" -> {
                formattedDate = "Mai/${date.substring(2,4)}"
            }
            "06" -> {
                formattedDate = "Jun/${date.substring(2,4)}"
            }
            "07" -> {
                formattedDate = "Jul/${date.substring(2,4)}"
            }
            "08" -> {
                formattedDate = "Ago/${date.substring(2,4)}"
            }
            "09" -> {
                formattedDate = "Set/${date.substring(2,4)}"
            }
            "10" -> {
                formattedDate = "Out/${date.substring(2,4)}"
            }
            "11" -> {
                formattedDate = "Nov/${date.substring(2,4)}"
            }
            "12" -> {
                formattedDate = "Dez/${date.substring(2,4)}"
            }
        }
        return formattedDate
    }

    fun formatDateFromFilterToDatabaseForInfoPerMonth(date: String): String {
        var formattedDate = ""
        val month = date.substring(5,7)
        when (month) {
            "01" -> {
                formattedDate = "Janeiro - ${date.substring(0,4)}"
            }
            "02" -> {
                formattedDate = "Fevereiro - ${date.substring(0,4)}"
            }
            "03" -> {
                formattedDate = "Março - ${date.substring(0,4)}"
            }
            "04" -> {
                formattedDate = "Abril - ${date.substring(0,4)}"
            }
            "05" -> {
                formattedDate = "Maio - ${date.substring(0,4)}"
            }
            "06" -> {
                formattedDate = "Junho - ${date.substring(0,4)}"
            }
            "07" -> {
                formattedDate = "Julho - ${date.substring(0,4)}"
            }
            "08" -> {
                formattedDate = "Agosto - ${date.substring(0,4)}"
            }
            "09" -> {
                formattedDate = "Setembro - ${date.substring(0,4)}"
            }
            "10" -> {
                formattedDate = "Outubro - ${date.substring(0,4)}"
            }
            "11" -> {
                formattedDate = "Novembro - ${date.substring(0,4)}"
            }
            "12" -> {
                formattedDate = "Dezembro - ${date.substring(0,4)}"
            }
        }
        return formattedDate
    }

    fun dataSnapshotToExpense(dataSnapShot : DataSnapshot) : Expense {
        val id = dataSnapShot.key.toString()
        val priceDatabase =
            BigDecimal(dataSnapShot.child(StringConstants.DATABASE.PRICE).value.toString())
        val priceFormatted =
            priceDatabase.setScale(8, RoundingMode.HALF_UP).toString()
        val description =
            dataSnapShot.child(StringConstants.DATABASE.DESCRIPTION).value.toString()
        val category =
            dataSnapShot.child(StringConstants.DATABASE.CATEGORY).value.toString()
        val paymentDateDatabase =
            dataSnapShot.child(StringConstants.DATABASE.PAYMENT_DATE).value.toString()
        val paymentDateFormatted =
            "${paymentDateDatabase.substring(8, 10)}/" +
                    "${paymentDateDatabase.substring(5, 7)}/" +
                    paymentDateDatabase.substring(0, 4)
        if ((!dataSnapShot.child(StringConstants.DATABASE.PURCHASE_DATE).exists())
            || (!dataSnapShot.child(StringConstants.DATABASE.INPUT_DATE_TIME).exists())
        ) {
            val expense = Expense(
                id,
                priceFormatted,
                description,
                category,
                paymentDateFormatted,
                "",
                ""
            )
            return expense
        } else {
            val purchaseDateDatabase =
                dataSnapShot.child(StringConstants.DATABASE.PURCHASE_DATE).value.toString()
            val purchaseDateFormatted =
                "${purchaseDateDatabase.substring(8, 10)}/" +
                        "${purchaseDateDatabase.substring(5, 7)}/" +
                        purchaseDateDatabase.substring(0, 4)
            val inputDateTime =
                dataSnapShot.child(StringConstants.DATABASE.INPUT_DATE_TIME).value.toString()
            val expense = Expense(
                id,
                priceFormatted,
                description,
                category,
                paymentDateFormatted,
                purchaseDateFormatted,
                inputDateTime
            )
            return expense
        }
    }

    fun dataSnapshotToInfoPerMonthExpense(dataSnapShot : DataSnapshot) : InformationPerMonthExpense {
        val monthInfo = InformationPerMonthExpense(
            dataSnapShot.key.toString(),
            dataSnapShot.child(StringConstants.DATABASE.AVAILABLE_NOW).value.toString(),
            dataSnapShot.child(StringConstants.DATABASE.BUDGET).value.toString(),
            dataSnapShot.child(StringConstants.DATABASE.EXPENSE).value.toString(),
        )
        return monthInfo
    }

    fun dataSnapshotToEarning(dataSnapShot : DataSnapshot) : Earning{
        return Earning(
            id = dataSnapShot.key.toString(),
            value = dataSnapShot.child(StringConstants.DATABASE.VALUE).value.toString(),
            description = dataSnapShot.child(StringConstants.DATABASE.DESCRIPTION).value.toString(),
            category = dataSnapShot.child(StringConstants.DATABASE.CATEGORY).value.toString(),
            date = dataSnapShot.child(StringConstants.DATABASE.DATE).value.toString(),
            inputDateTime = dataSnapShot.child(StringConstants.DATABASE.INPUT_DATE_TIME).value.toString()
        )
    }

    fun dataSnapshotToUpdateFromFile(dataSnapShot : DataSnapshot) : UploadTransactionFromFileInfo{

        val expenseIdList = mutableListOf<String>()
        val earningIdList = mutableListOf<String>()
        val expensePerMonthList = mutableListOf<ValuePerMonth>()

        dataSnapShot.child(StringConstants.DATABASE.EXPENSE_ID_LIST).children.forEach {
            val expenseId = it.value.toString()
            expenseIdList.add(expenseId)
        }

        dataSnapShot.child(StringConstants.DATABASE.EARNING_ID_LIST).children.forEach {
            val earningId = it.value.toString()
            earningIdList.add(earningId)
        }

       dataSnapShot.child(StringConstants.DATABASE.EXPENSE_INFORMATION_PER_MONTH).children.forEach {
           val month = it.key.toString()
           val expense = it.child(StringConstants.DATABASE.EXPENSE).value.toString()
           expensePerMonthList.add(ValuePerMonth(month, expense))
       }

        return UploadTransactionFromFileInfo(
            id = dataSnapShot.key.toString(),
            expenseList = mutableListOf(),
            updatedTotalExpense = "",
            updatedInformationPerMonth = mutableListOf(),
            earningList = mutableListOf(),
            expenseIdList = expenseIdList,
            earningIdList = earningIdList,
            totalExpenseFromFile = dataSnapShot.child(StringConstants.DATABASE.TOTAL_EXPENSE).value.toString(),
            expensePerMonthList = expensePerMonthList,
            inputDateTime = dataSnapShot.child(StringConstants.DATABASE.INPUT_DATE_TIME).value.toString()
        )
    }

    fun dataSnapshotToRecurringExpense(dataSnapShot : DataSnapshot) : RecurringTransaction{
        return RecurringTransaction(
            id = dataSnapShot.key.toString(),
            price = dataSnapShot.child(StringConstants.DATABASE.PRICE).value.toString(),
            description = dataSnapShot.child(StringConstants.DATABASE.DESCRIPTION).value.toString(),
            category = dataSnapShot.child(StringConstants.DATABASE.CATEGORY).value.toString(),
            day = dataSnapShot.child(StringConstants.DATABASE.DAY).value.toString(),
            inputDateTime = dataSnapShot.child(StringConstants.DATABASE.INPUT_DATE_TIME).value.toString(),
            type = dataSnapShot.child(StringConstants.DATABASE.TYPE).value.toString()
        )
    }

    fun dataSnapshotToCreditCardList(dataSnapShot : DataSnapshot) : CreditCard {
        return CreditCard(
            id = dataSnapShot.key.toString(),
            nickName = dataSnapShot.child(StringConstants.DATABASE.NAME).value.toString(),
            expirationDay = dataSnapShot.child(StringConstants.DATABASE.EXPIRATION_DAY).value.toString().toInt(),
            closingDay = dataSnapShot.child(StringConstants.DATABASE.CLOSING_DAY).value.toString().toInt(),
            colors = CreditCardColors.valueOf(dataSnapShot.child(StringConstants.DATABASE.COLORS).value.toString())
        )
    }
}