package com.example.fico.shared

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.fico.api.FormatValuesFromDatabase
import com.example.fico.api.FormatValuesToDatabase
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class DateFunctions {
    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentlyDate() : String{
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return currentDate.format(formatter) // EX: 20/04/2024
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentlyDateYearMonthToDatabase() : String{
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        var formattedDate = currentDate.format(formatter)
        val month = formattedDate.substring(3, 5)
        val year = formattedDate.substring(6, 10)
        val date = "$year-$month"
        return date // EX: 2024-04
    }

    fun YYYYmmDDtoYYYYmm(date : String) : String{
        return date.substring(0,7)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentlyDateForFilter() : String{
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val currentDateFormatted = currentDate.format(formatter)
        val yearMonth = FormatValuesToDatabase().expenseDateForInfoPerMonth(currentDateFormatted)
        val monthYearFormatted = FormatValuesFromDatabase().formatDateForFilterOnExpenseList(yearMonth)
        return monthYearFormatted // EX: Abril - 2024
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun paymentDate(payDay: String) : String{
        val day = payDay.toInt()
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val closingDate = LocalDate.of(currentDate.year, currentDate.month, day).minusDays(7)

        val baseDate = if(currentDate.dayOfMonth < closingDate.dayOfMonth){
            LocalDate.of(currentDate.year, currentDate.month, day)
        }else{
            val nextMonthDate = currentDate.plusMonths(1)
            LocalDate.of(nextMonthDate.year, nextMonthDate.month, day)
        }

        val monthLastDay = YearMonth.of(baseDate.year, baseDate.month).lengthOfMonth()
        return if(day < monthLastDay){
            baseDate.format(formatter)
        }else{
            LocalDate.of(baseDate.year, baseDate.month, monthLastDay).plusDays(1).format(formatter)
        }
    }
}