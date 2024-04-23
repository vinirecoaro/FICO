package com.example.fico.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.model.Expense
import com.example.fico.api.FirebaseAPI
import com.example.fico.api.FormatValuesToDatabase
import com.example.fico.api.ArrangeDataToUpdateToDatabase
import com.example.fico.util.constants.DateFunctions
import kotlinx.coroutines.*
import java.math.BigDecimal
import java.math.RoundingMode

class AddExpenseViewModel(
    private val firebaseAPI : FirebaseAPI,
) : ViewModel() {

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun addExpense(price: String, description: String, category: String, paymentDate: String, purchaseDate: String, installment : Boolean, nOfInstallments: Int = 1) : Deferred<Boolean>{
        return viewModelScope.async(Dispatchers.IO){

            val formattedPaymentDate = FormatValuesToDatabase().expenseDate(paymentDate)

            val formattedPurchaseDate = FormatValuesToDatabase().expenseDate(purchaseDate)

            val formattedInputDate = "${FormatValuesToDatabase().expenseDate(DateFunctions().getCurrentlyDate())}-${FormatValuesToDatabase().timeNow()}"

            val formattedPrice = FormatValuesToDatabase().expensePrice(price, nOfInstallments)

            val expense = Expense("",formattedPrice, description, category, formattedPaymentDate, formattedPurchaseDate,formattedInputDate)

            val expenseList = ArrangeDataToUpdateToDatabase().addToExpenseList(expense, installment, nOfInstallments)

            val updatedTotalExpense = ArrangeDataToUpdateToDatabase().calculateUpdatedTotalExpense(formattedPrice, nOfInstallments, viewModelScope).await()

            val updatedInformationPerMonth = ArrangeDataToUpdateToDatabase().addToInformationPerMonth(expense, installment, nOfInstallments, viewModelScope, false).await()

            firebaseAPI.addExpense(expenseList, updatedTotalExpense, updatedInformationPerMonth)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun checkIfExistDefaultBudget() : Deferred<Boolean> {
        return viewModelScope.async(Dispatchers.IO){
            firebaseAPI.checkIfExistDefaultBudget()
        }
    }

    suspend fun setDefaultBudget(budget: String) : Deferred<Boolean> {
        return viewModelScope.async(Dispatchers.IO) {
            val bigNum = BigDecimal(budget)
            val formattedBudget = bigNum.setScale(8, RoundingMode.HALF_UP).toString()
            firebaseAPI.setDefaultBudget(formattedBudget)
        }
    }


}