package com.example.fico.api

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.fico.model.Expense
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.LocalTime

class FormatValuesToDatabase {

    fun expenseDate(editTextDate: String): String {
        val day = editTextDate.substring(0, 2)
        val month = editTextDate.substring(3, 5)
        val year = editTextDate.substring(6, 10)
        return "$year-$month-$day"
    }

    fun expenseDateForInfoPerMonth(editTextDate: String): String {
        val month = editTextDate.substring(3, 5)
        val year = editTextDate.substring(6, 10)
        return "$year-$month"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun timeNow() : String {
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
        return "${hour}-${minute}-${second}"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun dateTimeNow() : String {
        return LocalDateTime.now().toString()
    }

    fun expensePrice(number : String, nOfInstallments : Int) : String {
        val regex = Regex("[\\d,.]+")
        val justNumber = regex.find(number)
        val denominator = BigDecimal(justNumber!!.value.replace(",","").replace(".",""))
        val divisor = BigDecimal(nOfInstallments)
        val correction = BigDecimal("100")
        val installmentPrice = denominator.divide(divisor, 8, RoundingMode.HALF_UP)
        val installmentPriceFormatted = installmentPrice.divide(correction)
        val formatedNum = installmentPriceFormatted.setScale(8, RoundingMode.HALF_UP)
        return formatedNum.toString().replace(",",".")
    }

    fun formatDateFromFilterToDatabaseForInfoPerMonth(date: String) : String{
        var formattedDate = ""
        val stringParts = date.split(" ")
        val month = stringParts[0]
        val year = stringParts[2]

        if(month == "Janeiro"){
            formattedDate = "$year-01"
        } else if(month == "Fevereiro"){
            formattedDate = "$year-02"
        }  else if(month == "Março"){
            formattedDate = "$year-03"
        } else if(month == "Abril"){
            formattedDate = "$year-04"
        } else if(month == "Maio"){
            formattedDate = "$year-05"
        } else if(month == "Junho"){
            formattedDate = "$year-06"
        } else if(month == "Julho"){
            formattedDate = "$year-07"
        } else if(month == "Agosto"){
            formattedDate = "$year-08"
        } else if(month == "Setembro"){
            formattedDate = "$year-09"
        } else if(month == "Outubro"){
            formattedDate = "$year-10"
        } else if(month == "Novembro"){
            formattedDate = "$year-11"
        } else if(month == "Dezembro"){
            formattedDate = "$year-12"
        }
        return formattedDate
    }

}