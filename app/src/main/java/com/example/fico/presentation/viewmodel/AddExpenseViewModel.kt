package com.example.fico.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.domain.model.Expense
import com.example.fico.api.FirebaseAPI
import com.example.fico.api.FormatValuesToDatabase
import com.example.fico.api.ArrangeDataToUpdateToDatabase
import com.example.fico.domain.model.ExpenseDomain
import com.example.fico.domain.usecase.GetAllExpensesUseCase
import com.example.fico.domain.usecase.InsertExpenseUseCase
import kotlinx.coroutines.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AddExpenseViewModel(
    private val firebaseAPI : FirebaseAPI,
    private val getAllExpensesUseCase: GetAllExpensesUseCase,
    private val insertExpenseUseCase: InsertExpenseUseCase
) : ViewModel() {

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun addExpenseLocal(price: String, description: String, category: String, date: String, installment : Boolean, nOfInstallments: Int = 1) = viewModelScope.launch{

        val formattedDate = FormatValuesToDatabase().expenseDate(date)
        val formattedPrice = FormatValuesToDatabase().expensePrice(price, nOfInstallments)
        val preFormattedExpense = Expense("",formattedPrice, description, category, formattedDate)
        val expenseList = ArrangeDataToUpdateToDatabase().addToExpenseList(preFormattedExpense, installment, nOfInstallments)
        
        for(expense in expenseList){
            insertExpenseUseCase(ExpenseDomain(
                expense.id,
                expense.price,
                expense.description,
                expense.category,
                expense.date
            ))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun addExpense(price: String, description: String, category: String, date: String, installment : Boolean, nOfInstallments: Int = 1) : Deferred<Boolean>{
        return viewModelScope.async(Dispatchers.IO){

            val formattedDate = FormatValuesToDatabase().expenseDate(date)

            val formattedPrice = FormatValuesToDatabase().expensePrice(price, nOfInstallments)

            val expense = Expense("",formattedPrice, description, category, formattedDate)

            val expenseList = ArrangeDataToUpdateToDatabase().addToExpenseList(expense, installment, nOfInstallments)

            val updatedTotalExpense = ArrangeDataToUpdateToDatabase().calculateUpdatedTotalExpense(formattedPrice, nOfInstallments, viewModelScope).await()

            val updatedInformationPerMonth = ArrangeDataToUpdateToDatabase().addToInformationPerMonth(expense, installment, nOfInstallments, viewModelScope, false).await()

            firebaseAPI.addExpense(expenseList, updatedTotalExpense, updatedInformationPerMonth)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCurrentlyDate() : String{
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return currentDate.format(formatter)
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