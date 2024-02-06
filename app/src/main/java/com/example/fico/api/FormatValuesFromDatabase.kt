package com.example.fico.api

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
        if(month == "01"){
            formattedDate = "Janeiro - ${date.substring(0,4)}"
        } else if(month == "02"){
            formattedDate = "Fevereiro - ${date.substring(0,4)}"
        } else if(month == "03"){
            formattedDate = "Março - ${date.substring(0,4)}"
        } else if(month == "04"){
            formattedDate = "Abril - ${date.substring(0,4)}"
        } else if(month == "05"){
            formattedDate = "Maio - ${date.substring(0,4)}"
        } else if(month == "06"){
            formattedDate = "Junho - ${date.substring(0,4)}"
        } else if(month == "07"){
            formattedDate = "Julho - ${date.substring(0,4)}"
        } else if(month == "08"){
            formattedDate = "Agosto - ${date.substring(0,4)}"
        } else if(month == "09"){
            formattedDate = "Setembro - ${date.substring(0,4)}"
        } else if(month == "10"){
            formattedDate = "Outubro - ${date.substring(0,4)}"
        } else if(month == "11"){
            formattedDate = "Novembro - ${date.substring(0,4)}"
        } else if(month == "12"){
            formattedDate = "Dezembro - ${date.substring(0,4)}"
        }
        return formattedDate
    }

}