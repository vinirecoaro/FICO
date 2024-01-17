package com.example.fico.ui.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.api.ArrangeDataToUpdateToDatabase
import com.example.fico.model.Expense
import com.example.fico.api.FirebaseAPI
import com.example.fico.api.FormatValuesToDatabase
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
            val oldExpenseDate = FormatValuesToDatabase().expenseDate(expense.date)
            val expencePrice = "-${expense.price.replace("R$ ", "").replace(",", ".")}"
            val oldExpense = Expense(
                expense.id,
                expencePrice,
                expense.description,
                expense.category,
                oldExpenseDate
            )
            val newExpense = Expense(id = "", price, description, category, date)

            val inputTime = FormatValuesToDatabase().timeNow()

            firebaseAPI.editExpense(oldExpense, newExpense, inputTime)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun saveEditExpense2(
        expense: Expense,
        price: String,
        description: String,
        category: String,
        date: String,
        installment : Boolean,
        nOfInstallments: Int = 1
    ) : Deferred<Boolean > {
        return viewModelScope.async(Dispatchers.IO) {

            val oldExpenseDate = FormatValuesToDatabase().expenseDate(expense.date)

            val oldExpense = Expense(
                expense.id,
                expense.price,
                expense.description,
                expense.category,
                oldExpenseDate
            )

            val removeFromExpenseList = ArrangeDataToUpdateToDatabase().removeFromExpenseList(oldExpense, viewModelScope).await()

            val newExpensePrice = FormatValuesToDatabase().expensePrice(price, nOfInstallments)
            val newExpenseDate = FormatValuesToDatabase().expenseDate(date)

            val newExpense = Expense(id = "", newExpensePrice, description, category, newExpenseDate)

            val updatedTotalExpense = ArrangeDataToUpdateToDatabase().calculateUpdatedTotalExpense(newExpense.price, nOfInstallments, viewModelScope, oldExpense.price).await()

            val updatedInformationPerMonth = ArrangeDataToUpdateToDatabase().addToInformationPerMonth(newExpense, installment, nOfInstallments, viewModelScope, true, oldExpense).await()

            val expenseList = ArrangeDataToUpdateToDatabase().addToExpenseList(newExpense, installment, nOfInstallments)

            firebaseAPI.editExpense2(expenseList,nOfInstallments, updatedTotalExpense,updatedInformationPerMonth, removeFromExpenseList)
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