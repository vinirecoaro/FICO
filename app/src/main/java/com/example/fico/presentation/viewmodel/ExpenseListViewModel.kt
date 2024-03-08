package com.example.fico.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.api.ArrangeDataToUpdateToDatabase
import com.example.fico.domain.model.Expense
import com.example.fico.api.FirebaseAPI
import com.example.fico.api.FormatValuesFromDatabase
import com.example.fico.api.FormatValuesToDatabase
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class ExpenseListViewModel: ViewModel() {

    private val firebaseAPI = FirebaseAPI.instance
    private val _expensesLiveData = MutableLiveData<List<Expense>>()
    val expensesLiveData: LiveData<List<Expense>> = _expensesLiveData
    private val _expenseMonthsLiveData = MutableLiveData<List<String>>()
    val expenseMonthsLiveData: LiveData<List<String>> = _expenseMonthsLiveData
    private val _filterLiveData = MutableLiveData<String>()
    val filterLiveData : LiveData<String>
        get() = _filterLiveData

    fun updateFilter(filter : String){
        _filterLiveData.value = filter
    }

    fun getExpenseList(filter : String) {
        viewModelScope.async{
            val expenses = firebaseAPI.getExpenseList(filter).await()
            val sortedExpenses = expenses.sortedByDescending { it.id }
            _expensesLiveData.value = sortedExpenses
        }
    }

    fun getExpenseMonths(){
        viewModelScope.async {
            var expenseMonths = firebaseAPI.getExpenseMonths(true)
            _expenseMonthsLiveData.value = expenseMonths
        }
    }

    fun deleteExpense(expense : Expense){
        viewModelScope.async(Dispatchers.IO) {
            firebaseAPI.deleteExpense(expense)
            val filter = filterLiveData.value.toString()
            getExpenseList(filter)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
     fun undoDeleteExpense(deletedExpense : Expense, installment : Boolean, nOfInstallments: Int = 1) : Deferred<Boolean> {
        return viewModelScope.async(Dispatchers.IO){

            val formattedDate = FormatValuesToDatabase().expenseDate(deletedExpense.date)

            val formattedPrice = FormatValuesToDatabase().expensePrice(FormatValuesFromDatabase().price(deletedExpense.price), nOfInstallments)

            val expense = Expense("",formattedPrice, deletedExpense.description, deletedExpense.category, formattedDate)

            val expenseList = ArrangeDataToUpdateToDatabase().addToExpenseList(expense, installment, nOfInstallments)

            val updatedTotalExpense = ArrangeDataToUpdateToDatabase().calculateUpdatedTotalExpense(formattedPrice, nOfInstallments, viewModelScope).await()

            val updatedInformationPerMonth = ArrangeDataToUpdateToDatabase().addToInformationPerMonth(expense, installment, nOfInstallments, viewModelScope, false).await()

            firebaseAPI.addExpense(expenseList, updatedTotalExpense, updatedInformationPerMonth)
        }
    }

}
