package com.example.fico.util

import java.math.BigDecimal
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

    fun installmentExpenseDescription(expenseId : String) : String{
        return expenseId.split(" Parcela")[0]
    }

    fun installmentExpenseNofInstallment(expenseId : String) : String{
        return expenseId.substring(38,41).toInt().toString()
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

}