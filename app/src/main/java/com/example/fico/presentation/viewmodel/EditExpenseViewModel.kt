package com.example.fico.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.api.ArrangeDataToUpdateToDatabase
import com.example.fico.model.Expense
import com.example.fico.api.FirebaseAPI
import com.example.fico.api.FormatValuesFromDatabase
import com.example.fico.api.FormatValuesToDatabase
import com.example.fico.util.constants.DateFunctions
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class EditExpenseViewModel : ViewModel() {

    val firebaseAPI = FirebaseAPI.instance

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun saveEditExpense(
        oldExpense: Expense,
        price: String,
        description: String,
        category: String,
        paymentDate: String,
        purchaseData : String,
        installment : Boolean,
        nOfInstallments: Int = 1
    ) : Deferred<Boolean > {
        return viewModelScope.async(Dispatchers.IO) {

            var oldExpensePaymentDate = FormatValuesToDatabase().expenseDate(oldExpense.paymentDate)
            var oldExpensePurchaseDate = FormatValuesToDatabase().expenseDate(oldExpense.purchaseDate)

            var oldExpenseNOfInstallment = 1

            if(installment){
                oldExpenseNOfInstallment = FormatValuesFromDatabase().installmentExpenseNofInstallment(oldExpense.id).toInt()
                oldExpensePaymentDate = FormatValuesToDatabase().expenseDate(FormatValuesFromDatabase().installmentExpenseInitialDate(oldExpense.id,oldExpense.paymentDate))
            }

            val oldExpenseFormatted = Expense(
                oldExpense.id,
                oldExpense.price,
                oldExpense.description,
                oldExpense.category,
                oldExpensePaymentDate,
                oldExpensePurchaseDate,
                ""
            )

            val removeFromExpenseList = ArrangeDataToUpdateToDatabase().removeFromExpenseList(oldExpenseFormatted, viewModelScope).await()

            val newExpensePrice = FormatValuesToDatabase().expensePrice(price, nOfInstallments)
            val newExpensePaymentDate = FormatValuesToDatabase().expenseDate(paymentDate)
            val newExpensePurchaseDate = FormatValuesToDatabase().expenseDate(purchaseData)
            val formattedInputDate = "${FormatValuesToDatabase().expenseDate(DateFunctions().getCurrentlyDate())}-${FormatValuesToDatabase().timeNow()}"

            val newExpense = Expense(id = "", newExpensePrice, description, category, newExpensePaymentDate, newExpensePurchaseDate, formattedInputDate)

            val updatedTotalExpense = ArrangeDataToUpdateToDatabase().calculateUpdatedTotalExpense(newExpense.price, nOfInstallments, viewModelScope, oldExpenseFormatted.price, oldExpenseNOfInstallment).await()

            val updatedInformationPerMonth = ArrangeDataToUpdateToDatabase().addToInformationPerMonth(newExpense, installment, nOfInstallments, viewModelScope, true, oldExpenseFormatted).await()

            val expenseList = ArrangeDataToUpdateToDatabase().addToExpenseList(newExpense, installment, nOfInstallments)

            firebaseAPI.editExpense(expenseList, updatedTotalExpense,updatedInformationPerMonth, removeFromExpenseList, oldExpenseNOfInstallment)
        }
    }

}

