package com.example.fico.presentation.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.fico.domain.model.Expense
import com.example.fico.api.FirebaseAPI
import com.example.fico.api.FormatValuesToDatabase
import com.example.fico.api.ArrangeDataToUpdateToDatabase
import com.example.fico.data.db
import com.example.fico.data.repository.ExpenseRepositoryImpl
import com.example.fico.domain.model.ExpenseDomain
import com.example.fico.domain.usecase.GetAllExpensesUseCase
import com.example.fico.domain.usecase.InsertExpenseUseCase
import com.example.fico.presentation.fragments.expense.add_expense.AddExpenseState
import kotlinx.coroutines.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AddExpenseViewModel(
    private val getAllExpensesUseCase: GetAllExpensesUseCase,
    private val insertExpenseUseCase: InsertExpenseUseCase
) : ViewModel() {

    private val firebaseAPI = FirebaseAPI.instance
    val state : LiveData<AddExpenseState> = liveData {
        emit(AddExpenseState.Loading)
        val state = try {
            val expenses = getAllExpensesUseCase()
            if(expenses.isEmpty()){
                AddExpenseState.Empty
            }else{
                AddExpenseState.Success(expenses)
            }
        }catch (exception : Exception){
            Log.e("Error", exception.message.toString())
            AddExpenseState.Error(exception.message.toString())
        }
        emit(state)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addExpenseLocal(price: String, description: String, category: String, date: String, installment : Boolean, nOfInstallments: Int = 1) = viewModelScope.launch{

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

    class Factory : ViewModelProvider.Factory{
        override fun <T : ViewModel> create(
            modelClass: Class<T>,
            extras : CreationExtras
        ): T {
            val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
            val repository = ExpenseRepositoryImpl(application.db.expenseDao())
            return AddExpenseViewModel(
                getAllExpensesUseCase =  GetAllExpensesUseCase(repository),
                insertExpenseUseCase = InsertExpenseUseCase(repository)
                ) as T
        }
    }


}