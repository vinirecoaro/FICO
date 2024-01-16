package com.example.fico.api

import android.os.Build
import androidx.annotation.RequiresApi
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalTime

class FormatValuesToDatabase {

    fun expenseDate(editTextDate: String): String {
        val day = editTextDate.substring(0, 2)
        val month = editTextDate.substring(3, 5)
        val year = editTextDate.substring(6, 10)
        return "$year-$month-$day"
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

}