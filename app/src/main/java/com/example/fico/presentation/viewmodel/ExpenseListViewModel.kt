package com.example.fico.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.datastore.core.DataStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.DataStoreManager
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

class ExpenseListViewModel(
    private val firebaseAPI: FirebaseAPI,
    private val dataStore: DataStoreManager
) : ViewModel() {


    private val _expensesLiveData = MutableLiveData<List<Expense>>()
    val expensesLiveData: LiveData<List<Expense>> = _expensesLiveData
    private val _expenseMonthsLiveData = MutableLiveData<List<String>>()
    val expenseMonthsLiveData: LiveData<List<String>> = _expenseMonthsLiveData
    private val _filterLiveData = MutableLiveData<String>()
    val filterLiveData: LiveData<String>
        get() = _filterLiveData

    fun updateFilter(filter: String) {
        _filterLiveData.value = filter
    }

    fun getExpenseList(filter: String) {
        viewModelScope.async {
            /*val expenses = firebaseAPI.observeExpenseList(filter).await()
            val sortedExpenses = expenses.sortedByDescending { it.paymentDate }*/
            val expenses = dataStore.getExpenseList()
            var sortedExpenses = listOf<Expense>()
            if (filter != "") {
                val filteredExpenses = expenses.filter {
                    FormatValuesToDatabase().expenseDateForInfoPerMonth(it.paymentDate) == FormatValuesToDatabase().formatDateFromFilterToDatabaseForInfoPerMonth(
                        filter
                    )
                }
                sortedExpenses = filteredExpenses.sortedByDescending { it.paymentDate }
            } else {
                sortedExpenses =
                    expenses.sortedByDescending { FormatValuesToDatabase().expenseDate(it.paymentDate) }
            }
            _expensesLiveData.value = sortedExpenses
        }
    }

    fun getExpenseMonths() {
        viewModelScope.async {
            /*var expenseMonths = firebaseAPI.getExpenseMonths(false).sortedByDescending { it }
            val expenseMonthsFormatted = mutableListOf<String>()
            for (expenseMonth in expenseMonths){
                expenseMonthsFormatted.add(FormatValuesFromDatabase().formatDateForFilterOnExpenseList(expenseMonth))
            }*/
            val expenseMonths = dataStore.getExpenseMonths().sortedByDescending {
                FormatValuesToDatabase().formatDateFromFilterToDatabaseForInfoPerMonth(it)
            }
            _expenseMonthsLiveData.value = expenseMonths
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.async(Dispatchers.IO) {
            firebaseAPI.deleteExpense(expense)
            val currentList = dataStore.getExpenseList().toMutableList()
            currentList.removeAll { it.id == expense.id }
            dataStore.updateExpenseList(currentList.toList())
            getExpenseList(_filterLiveData.value.toString())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun undoDeleteExpense(
        deletedExpense: Expense,
        installment: Boolean,
        nOfInstallments: Int = 1
    ): Deferred<Boolean> {
        return viewModelScope.async(Dispatchers.IO) {

            val formattedPaymentDate =
                FormatValuesToDatabase().expenseDate(deletedExpense.paymentDate)

            val formattedPurchaseDate =
                FormatValuesToDatabase().expenseDate(deletedExpense.purchaseDate)

            val formattedInputDate =
                "${FormatValuesToDatabase().expenseDate(DateFunctions().getCurrentlyDate())}-${FormatValuesToDatabase().timeNow()}"

            val formattedPrice = FormatValuesToDatabase().expensePrice(
                FormatValuesFromDatabase().price(deletedExpense.price), nOfInstallments
            )

            val expense = Expense(
                "",
                formattedPrice,
                deletedExpense.description,
                deletedExpense.category,
                formattedPaymentDate,
                formattedPurchaseDate,
                formattedInputDate
            )

            val expenseList = ArrangeDataToUpdateToDatabase().addToExpenseList(
                expense,
                installment,
                nOfInstallments
            )

            val updatedTotalExpense = ArrangeDataToUpdateToDatabase().calculateUpdatedTotalExpense(
                formattedPrice,
                nOfInstallments,
                viewModelScope
            ).await()

            val updatedInformationPerMonth =
                ArrangeDataToUpdateToDatabase().addToInformationPerMonth(
                    expense,
                    installment,
                    nOfInstallments,
                    viewModelScope,
                    false
                ).await()
            val updatedExpenseList = dataStore.getExpenseList().toMutableList()
            val formattedExpense = Expense(expenseList[0].id,expense.price,expense.description,expense.category,FormatValuesFromDatabase().date(expense.paymentDate),FormatValuesFromDatabase().date(expense.purchaseDate),expense.inputDateTime)
            updatedExpenseList.add(formattedExpense)
            dataStore.updateExpenseList(updatedExpenseList)
            getExpenseList(_filterLiveData.value.toString())
            firebaseAPI.addExpense(expenseList, updatedTotalExpense, updatedInformationPerMonth)
        }

    }

}
