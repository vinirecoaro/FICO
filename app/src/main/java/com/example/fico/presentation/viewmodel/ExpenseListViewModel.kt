package com.example.fico.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.math.BigDecimal
import java.math.RoundingMode

class ExpenseListViewModel(
    private val firebaseAPI: FirebaseAPI,
    private val dataStore: DataStoreManager
) : ViewModel() {


    private val _expensesLiveData = MutableLiveData<List<Expense>>()
    val expensesLiveData: LiveData<List<Expense>> = _expensesLiveData
    private val _expenseMonthsLiveData = MutableLiveData<List<String>>()
    val expenseMonthsLiveData: LiveData<List<String>> = _expenseMonthsLiveData
    private val _filterLiveData = MutableLiveData<String>()
    private val _deleteExpenseResult = MutableLiveData<Boolean>()
    val deleteExpenseResult: LiveData<Boolean> = _deleteExpenseResult
    var deletedItem: Expense? = null
    private val _addExpenseResult = MutableLiveData<Boolean>()
    val addExpenseResult: LiveData<Boolean> = _addExpenseResult
    private val _installmentExpenseSwiped = MutableLiveData<Boolean>()
    val installmentExpenseSwiped: LiveData<Boolean> = _installmentExpenseSwiped
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
            val expenseMonths = dataStore.getExpenseMonths()
            val expenseMonthsFormatted = mutableListOf<String>()
            expenseMonths.forEach {
                expenseMonthsFormatted.add(FormatValuesFromDatabase().formatDateForFilterOnExpenseList(it))
            }
            _expenseMonthsLiveData.value = expenseMonthsFormatted
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.async(Dispatchers.IO) {
            val result = firebaseAPI.deleteExpense(expense)
            result.fold(
                onSuccess = {
                    deletedItem = expense

                    //Get current dataStore Expense list
                    val currentList = dataStore.getExpenseList().toMutableList()

                    //Remove from dataStore expense List
                    currentList.removeAll { it.id == expense.id }
                    dataStore.updateAndResetExpenseList(currentList.toList())

                    //Update expenseList on screen
                    getExpenseList(_filterLiveData.value.toString())

                    //Remove from dataStore expense Months List
                    val removedExpenseMonth = DateFunctions().YYYYmmDDtommDD(expense.paymentDate)
                    val existDate =
                        currentList.any { DateFunctions().YYYYmmDDtommDD(FormatValuesToDatabase().expenseDate(it.paymentDate)) == removedExpenseMonth }
                    if (!existDate) {
                        val currentMonthList = dataStore.getExpenseMonths().toMutableList()
                        currentMonthList.removeAll {
                            it == DateFunctions().YYYYmmDDtommDD(expense.paymentDate)
                        }
                        dataStore.updateAndResetExpenseMonths(currentMonthList)

                        //update expense months options
                        getExpenseMonths()
                    }

                    //Update dataStore Total Expense
                    val currentTotalExpense = BigDecimal(dataStore.getTotalExpense())
                    val priceFormatted = BigDecimal(expense.price).setScale(2,RoundingMode.HALF_UP)
                    val updatedTotalExpenseFromDataStore = currentTotalExpense.add(priceFormatted)
                    dataStore.updateTotalExpense(updatedTotalExpenseFromDataStore.toString())

                    _deleteExpenseResult.postValue(true)
                },
                onFailure = {
                    _deleteExpenseResult.postValue(false)
                }
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun undoDeleteExpense(
        deletedExpense: Expense,
        installment: Boolean,
        nOfInstallments: Int = 1
    ) {
        viewModelScope.async(Dispatchers.IO) {

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
                deletedExpense.paymentDate,
                deletedExpense.purchaseDate,
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

            //Generate list to update dataStore
            val updatedExpenseList = mutableListOf<Expense>()
            expenseList.forEach { updatedExpense ->
                val formattedExpense = Expense(
                    updatedExpense.id,
                    updatedExpense.price,
                    updatedExpense.description,
                    updatedExpense.category,
                    FormatValuesFromDatabase().date(updatedExpense.paymentDate),
                    FormatValuesFromDatabase().date(updatedExpense.purchaseDate),
                    updatedExpense.inputDateTime
                )
                updatedExpenseList.add(formattedExpense)
            }

            //After update database update local storage
            var result = firebaseAPI.addExpense(expenseList, updatedTotalExpense, updatedInformationPerMonth)
            result.fold(
                onSuccess = {

                    // Update dataStore expenseList
                    dataStore.updateExpenseList(updatedExpenseList)
                    getExpenseList(_filterLiveData.value.toString())
                    val expenseMonthsList = listOf(updatedInformationPerMonth[0].date)

                    // Update dataStore expenseMonths
                    dataStore.updateExpenseMonths(expenseMonthsList)
                    getExpenseMonths()
                    _addExpenseResult.postValue(true)

                    //Update dataStore Total Expense
                    val currentTotalExpense = BigDecimal(dataStore.getTotalExpense())
                    val updatedTotalExpenseFromDataStore = currentTotalExpense.add(BigDecimal(formattedPrice))
                    dataStore.updateTotalExpense(updatedTotalExpenseFromDataStore.toString())

                },
                onFailure = {
                    _addExpenseResult.postValue(false)
                }
            )

        }

    }

    fun onInstallmentExpenseSwiped(){
        _installmentExpenseSwiped.postValue(true)
    }

}
