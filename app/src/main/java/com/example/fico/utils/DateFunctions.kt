package com.example.fico.utils

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.fico.api.FormatValuesFromDatabase
import com.example.fico.api.FormatValuesToDatabase
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class DateFunctions {
    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentDate(formatted : Boolean = true) : String{
        val currentDate = LocalDate.now()
            val formatter =
                if(formatted){
                    DateTimeFormatter.ofPattern("dd/MM/yyyy") // EX: 20/04/2024
                }else{
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")
                }
        return currentDate.format(formatter)
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
    fun paymentDate(expirationDay: Int, closingDay: Int, purchaseDate: String): String {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val parsedPurchaseDate = LocalDate.parse(purchaseDate, formatter)

        val baseDate = if (parsedPurchaseDate.dayOfMonth < closingDay) {
            adjustToLastDayOfMonthIfNecessary(parsedPurchaseDate.year, parsedPurchaseDate.monthValue, expirationDay)
        } else {
            val nextMonthDate = parsedPurchaseDate.plusMonths(1)
            adjustToLastDayOfMonthIfNecessary(nextMonthDate.year, nextMonthDate.monthValue, expirationDay)
        }
        return baseDate.format(formatter)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun purchaseDateForRecurringExpense(day : String) : String{
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val currentDate = LocalDate.now()
        val purchaseDate = LocalDate.of(currentDate.year, currentDate.month, day.toInt())
        return purchaseDate.format(formatter)
    }

    fun formatMonthValueFromFilterTransactionListToMonthYear(filterValue : String) : Pair<Int, Int>{
        val date = FormatValuesToDatabase().formatDateFromFilterToDatabaseForInfoPerMonth(filterValue)
        val dateInfo = date.split("-")
        val year = dateInfo[0].toInt()
        val month = dateInfo[1].toInt()
        val infoPair = Pair(month,year)
        return infoPair
    }

    fun isValidMonthDay(day : Int) : Boolean{
        return day in 1..31
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun adjustToLastDayOfMonthIfNecessary(year: Int, month: Int, day: Int): LocalDate {
        // Get last valid day for month and year
        val lastDayOfMonth = YearMonth.of(year, month).lengthOfMonth()
        // Using lowest value between `day` and `lastDayOfMonth`
        return LocalDate.of(year, month, minOf(day, lastDayOfMonth))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatDateTimeToShow(input: String): String {
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
        val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm")

        val dateTime = LocalDateTime.parse(input, inputFormatter)
        return dateTime.format(outputFormatter)
    }
}