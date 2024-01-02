package com.example.fico.ui.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.model.Expense
import com.example.fico.service.FirebaseAPI
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.time.LocalTime

class EditExpenseViewModel : ViewModel() {

    val firebaseAPI = FirebaseAPI.instance

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun saveEditExpense(
        expense: Expense,
        price: String,
        description: String,
        category: String,
        date: String
    ) : Deferred<Boolean > {
        return viewModelScope.async(Dispatchers.IO) {
            val day = expense.date.substring(0, 2)
            val month = expense.date.substring(3, 5)
            val year = expense.date.substring(6, 10)
            val modifiedDate = "$year-$month-$day"
            val expencePrice = "-${expense.price.replace("R$ ", "").replace(",", ".")}"
            val oldExpense = Expense(
                expense.id,
                expencePrice,
                expense.description,
                expense.category,
                modifiedDate
            )
            val newExpense = Expense(id = "", price, description, category, date)
            val timeNow = LocalTime.now()
            var hour = timeNow.hour.toString()
            var minute = timeNow.minute.toString()
            var second = timeNow.second.toString()
            if (timeNow.hour < 10) {
                hour = "0${timeNow.hour}"
            }
            if (timeNow.minute < 10) {
                minute = "0${timeNow.minute}"
            }
            if (timeNow.second < 10) {
                second = "0${timeNow.second}"
            }
            val inputTime = "${hour}-${minute}-${second}"

            firebaseAPI.editExpense(oldExpense, newExpense, inputTime)
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun saveEditInstallmentExpense(
        price: String,
        description: String,
        category: String,
        date: String,
        nOfInstallments: Int
    ): Deferred<Boolean> {
        return viewModelScope.async {
            val newExpense = Expense(id = "", price, description, category, date)
            val timeNow = LocalTime.now()
            var hour = timeNow.hour.toString()
            var minute = timeNow.minute.toString()
            var second = timeNow.second.toString()
            if (timeNow.hour < 10) {
                hour = "0${timeNow.hour}"
            }
            if (timeNow.minute < 10) {
                minute = "0${timeNow.minute}"
            }
            if (timeNow.second < 10) {
                second = "0${timeNow.second}"
            }
            val inputTime = "${hour}-${minute}-${second}"
            firebaseAPI.addEditedInstallmentExpense(newExpense, inputTime, nOfInstallments)
        }
    }

    suspend fun deleteOldInstallmentExpense(expense : Expense) : Deferred<Boolean> {
        return viewModelScope.async{
            val day = expense.date.substring(0, 2)
            val month = expense.date.substring(3, 5)
            val year = expense.date.substring(6, 10)
            val modifiedDate = "$year-$month-$day"
            val expencePrice = "-${expense.price.replace("R$ ", "").replace(",", ".")}"
            val oldExpense = Expense(
                expense.id,
                expencePrice,
                expense.description,
                expense.category,
                modifiedDate
            )
            firebaseAPI.deleteInstallmentExpense(oldExpense)
        }
    }

    suspend fun updateTotalExpenseAfterEditInstallmentExpense(expense : Expense) : Deferred<Boolean> {
        return viewModelScope.async{
            val day = expense.date.substring(0, 2)
            val month = expense.date.substring(3, 5)
            val year = expense.date.substring(6, 10)
            val modifiedDate = "$year-$month-$day"
            val expencePrice = "-${expense.price.replace("R$ ", "").replace(",", ".")}"
            val oldExpense = Expense(
                expense.id,
                expencePrice,
                expense.description,
                expense.category,
                modifiedDate
            )
            firebaseAPI.updateTotalExpenseAfterEditInstallmentExpense(oldExpense)
        }
    }
}