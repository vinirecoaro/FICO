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
import com.example.fico.model.Earning
import com.example.fico.model.InformationPerMonthExpense
import com.example.fico.model.Transaction
import com.example.fico.presentation.fragments.transaction_list.TransactionFragmentState
import com.example.fico.utils.DateFunctions
import com.example.fico.utils.constants.StringConstants
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

class TransactionListViewModel(
    private val firebaseAPI: FirebaseAPI,
    private val dataStore: DataStoreManager
) : ViewModel() {

    private val _expensesLiveData = MutableLiveData<List<Expense>>()
    val expensesLiveData: LiveData<List<Expense>> = _expensesLiveData
    private val _earningsListLiveData = MutableLiveData<List<Earning>>()
    val earningsListLiveData: LiveData<List<Earning>> = _earningsListLiveData
    private val _transactionsListLiveData = MutableLiveData<List<Transaction>>()
    val transactionsListLiveData: LiveData<List<Transaction>> = _transactionsListLiveData
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
    private val arrangeDataToUpdateToDatabase  = ArrangeDataToUpdateToDatabase()
    val filterLiveData: LiveData<String>
        get() = _filterLiveData
    private val _uiState = MutableStateFlow<TransactionFragmentState<Nothing>>(
        TransactionFragmentState.Loading)
    val uiState : StateFlow<TransactionFragmentState<Nothing>> = _uiState.asStateFlow()
    private val _earningMonthsLiveData = MutableLiveData<List<String>>()
    val earningMonthsLiveData: LiveData<List<String>> = _earningMonthsLiveData

    fun updateFilter(filter: String) {
        _filterLiveData.value = filter
    }

    fun getExpenseList(filter: String) {
        _uiState.value = TransactionFragmentState.Loading

        viewModelScope.async {
            try {
                val expenses = dataStore.getExpenseList()
                if(expenses.isNotEmpty()){
                    var sortedExpenses = listOf<Expense>()
                    if (filter != "") {
                        val filteredExpenses = expenses.filter {
                            FormatValuesToDatabase().expenseDateForInfoPerMonth(it.paymentDate) == FormatValuesToDatabase().formatDateFromFilterToDatabaseForInfoPerMonth(
                                filter
                            )
                        }
                        sortedExpenses = filteredExpenses.sortedByDescending { FormatValuesToDatabase().expenseDate(it.purchaseDate) }
                    } else {
                        sortedExpenses =
                            expenses.sortedByDescending { FormatValuesToDatabase().expenseDate(it.purchaseDate) }
                    }
                    _expensesLiveData.value = sortedExpenses
                }else{
                    _expensesLiveData.value = emptyList()

                }
            }catch (error: Exception){
                _uiState.value = TransactionFragmentState.Error(error.message.toString())
            }

        }
    }

    fun getExpenseMonths() {
        viewModelScope.async {
            try {
                val transactionMonthsList = mutableSetOf<String>()
                val expenseMonths = dataStore.getExpenseMonths()
                val earningMonths = dataStore.getEarningMonths()
                transactionMonthsList.addAll(expenseMonths)
                transactionMonthsList.addAll(earningMonths)
                val sortedTransactionMonthsList = transactionMonthsList.sortedByDescending{it}
                if(sortedTransactionMonthsList.isNotEmpty()){
                    val transactionMonthsListFormatted = mutableListOf<String>()
                    sortedTransactionMonthsList.forEach {
                        transactionMonthsListFormatted.add(FormatValuesFromDatabase().formatDateForFilterOnExpenseList(it))
                    }
                    _expenseMonthsLiveData.value = transactionMonthsListFormatted
                    _uiState.value = TransactionFragmentState.Success
                }else{
                    _uiState.value = TransactionFragmentState.Empty
                }
            }catch (error: Exception){
                _uiState.value = TransactionFragmentState.Error(error.message.toString())
            }
        }
    }

    fun getEarningList(filter: String){
        viewModelScope.launch {
            val earningListDataStore = dataStore.getEarningsList()
            if(earningListDataStore.isNotEmpty()){
                var filteredList = listOf<Earning>()
                if (filter != ""){
                    filteredList = earningListDataStore.filter {
                        val earningDate = DateFunctions().YYYYmmDDtoYYYYmm(it.date)
                        var filterDate = FormatValuesToDatabase().formatDateFromFilterToDatabaseForInfoPerMonth(filter)
                        earningDate == filterDate
                    }.sortedByDescending { it.date }
                }else{
                    filteredList = earningListDataStore.sortedByDescending { it.date }
                }
                val earningList = mutableListOf<Earning>()

                for(earning in filteredList){
                    val dateFormatted = FormatValuesFromDatabase().date(earning.date)
                    earningList.add(
                        Earning(
                            earning.id,
                            earning.value,
                            earning.description,
                            earning.category,
                            dateFormatted,
                            earning.inputDateTime
                        )
                    )
                }
                _earningsListLiveData.postValue(earningList)
            }else{
                _earningsListLiveData.postValue(emptyList())
                if(_expensesLiveData.value != null ){
                    if (_expensesLiveData.value!!.isEmpty()){
                        _uiState.value = TransactionFragmentState.Empty
                    }
                }
            }
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
                    val removedExpenseMonth = DateFunctions().YYYYmmDDtoYYYYmm(expense.paymentDate)
                    val existDate =
                        currentList.any { DateFunctions().YYYYmmDDtoYYYYmm(FormatValuesToDatabase().expenseDate(it.paymentDate)) == removedExpenseMonth }
                    if (!existDate) {
                        val currentMonthList = dataStore.getExpenseMonths().toMutableList()
                        currentMonthList.removeAll {
                            it == DateFunctions().YYYYmmDDtoYYYYmm(expense.paymentDate)
                        }
                        dataStore.updateAndResetExpenseMonths(currentMonthList)

                        //update expense months options
                        getExpenseMonths()
                    }

                    //Update info per month on dataStore
                    val currentInfoPerMonth = dataStore.getExpenseInfoPerMonth()
                    val updatedInfoPerMonth = mutableListOf<InformationPerMonthExpense>()
                    val monthInfo = currentInfoPerMonth.find { infoPerMonth ->
                        infoPerMonth.date == DateFunctions().YYYYmmDDtoYYYYmm(expense.paymentDate) }
                    if(monthInfo != null){
                        val expensePrice = BigDecimal(expense.price).setScale(8,RoundingMode.HALF_UP)
                        val monthExpenseUpdated = BigDecimal(monthInfo.monthExpense).add(expensePrice).setScale(8,RoundingMode.HALF_UP).toString()
                        val availableNowUpdated = BigDecimal(monthInfo.availableNow).subtract(expensePrice).setScale(8,RoundingMode.HALF_UP).toString()
                        val monthInfoUpdated = InformationPerMonthExpense(
                            monthInfo.date,
                            availableNowUpdated,
                            monthInfo.budget,
                            monthExpenseUpdated
                        )
                        updatedInfoPerMonth.add(monthInfoUpdated)
                    }else{
                        val date = DateFunctions().YYYYmmDDtoYYYYmm(expense.paymentDate)
                        val defaultBudget = BigDecimal(dataStore.getDefaultBudget()).setScale(8,RoundingMode.HALF_UP)
                        val monthExpenseUpdated = BigDecimal(expense.price).setScale(8, RoundingMode.HALF_UP).toString()
                        val availableNowUpdated = defaultBudget.subtract(BigDecimal(expense.price)).setScale(8,RoundingMode.HALF_UP).toString()
                        val monthInfoUpdated = InformationPerMonthExpense(
                            date,
                            availableNowUpdated,
                            defaultBudget.toString(),
                            monthExpenseUpdated
                        )
                        updatedInfoPerMonth.add(monthInfoUpdated)
                    }
                    dataStore.updateInfoPerMonthExpense(updatedInfoPerMonth)

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

            val expenseList = arrangeDataToUpdateToDatabase.addToExpenseList(
                expense,
                installment,
                nOfInstallments
            )

            val updatedTotalExpense = arrangeDataToUpdateToDatabase.calculateUpdatedTotalExpense(
                dataStore.getTotalExpense(),
                formattedPrice,
                nOfInstallments
            )

            val updatedInformationPerMonth =
                arrangeDataToUpdateToDatabase.addToInformationPerMonth(
                    expense,
                    installment,
                    nOfInstallments,
                    dataStore.getExpenseInfoPerMonth(),
                    dataStore.getDefaultBudget(),
                    false
                )

            //After update database update local storage
            var result = firebaseAPI.addExpense(expenseList, updatedTotalExpense, updatedInformationPerMonth)
            result.fold(
                onSuccess = {

                    //Generate list to update dataStore expenseList and InfoPerMonthExpense
                    val currentInfoPerMonth = dataStore.getExpenseInfoPerMonth()
                    val updatedInfoPerMonth = mutableListOf<InformationPerMonthExpense>()
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
                        //ExpenseList
                        updatedExpenseList.add(formattedExpense)
                        // InfoPerMonth
                        val monthInfo = currentInfoPerMonth.find { infoPerMonth ->
                            infoPerMonth.date == DateFunctions().YYYYmmDDtoYYYYmm(expense.paymentDate) }
                        if(monthInfo != null){
                            val expensePrice = BigDecimal(expense.price).setScale(8,RoundingMode.HALF_UP)
                            val monthExpenseUpdated = BigDecimal(monthInfo.monthExpense).add(expensePrice).setScale(8,RoundingMode.HALF_UP).toString()
                            val availableNowUpdated = BigDecimal(monthInfo.availableNow).subtract(expensePrice).setScale(8,RoundingMode.HALF_UP).toString()
                            val monthInfoUpdated = InformationPerMonthExpense(
                                monthInfo.date,
                                availableNowUpdated,
                                monthInfo.budget,
                                monthExpenseUpdated
                            )
                            updatedInfoPerMonth.add(monthInfoUpdated)
                        }else{
                            val date = DateFunctions().YYYYmmDDtoYYYYmm(expense.paymentDate)
                            val defaultBudget = BigDecimal(dataStore.getDefaultBudget()).setScale(8,RoundingMode.HALF_UP)
                            val monthExpenseUpdated = BigDecimal(expense.price).setScale(8, RoundingMode.HALF_UP).toString()
                            val availableNowUpdated = defaultBudget.subtract(BigDecimal(expense.price)).setScale(8,RoundingMode.HALF_UP).toString()
                            val monthInfoUpdated = InformationPerMonthExpense(
                                date,
                                availableNowUpdated,
                                defaultBudget.toString(),
                                monthExpenseUpdated
                            )
                            updatedInfoPerMonth.add(monthInfoUpdated)
                        }
                    }

                    // Update dataStore expenseList and InfoPerMonth
                    dataStore.updateExpenseList(updatedExpenseList)
                    getExpenseList(_filterLiveData.value.toString())
                    dataStore.updateInfoPerMonthExpense(updatedInfoPerMonth)

                    // Update dataStore expenseMonths
                    val expenseMonthsList = mutableListOf<String>()
                    updatedInformationPerMonth.forEach { infoPerMonth ->
                        expenseMonthsList.add(infoPerMonth.date)
                    }
                    dataStore.updateExpenseMonths(expenseMonthsList)
                    getExpenseMonths()

                    //Update dataStore Total Expense
                    val currentTotalExpense = BigDecimal(dataStore.getTotalExpense())
                    val updatedTotalExpenseFromDataStore = currentTotalExpense.add(BigDecimal(formattedPrice))
                    dataStore.updateTotalExpense(updatedTotalExpenseFromDataStore.toString())

                    _addExpenseResult.postValue(true)

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

    fun updateTransactionsList(expenseList : List<Expense>, earningList : List<Earning>){
        val transactionListTemp = mutableListOf<Transaction>()
        expenseList.forEach { expense ->
            transactionListTemp.add(
                Transaction(
                    id = expense.id,
                    price = expense.price,
                    description = expense.description,
                    category = expense.category,
                    paymentDate = expense.paymentDate,
                    purchaseDate = expense.purchaseDate,
                    inputDateTime = expense.inputDateTime,
                    nOfInstallment = expense.nOfInstallment,
                    type = StringConstants.DATABASE.EXPENSE
                )
            )
        }
        earningList.forEach { earning ->
            transactionListTemp.add(
                Transaction(
                    id = earning.id,
                    price = earning.value,
                    description = earning.description,
                    category = earning.category,
                    paymentDate = earning.date,
                    purchaseDate = earning.date,
                    inputDateTime = earning.inputDateTime,
                    nOfInstallment = "1",
                    type = StringConstants.DATABASE.EARNING
                )
            )
        }
        val transactionListSorted = transactionListTemp.toList().sortedByDescending { FormatValuesToDatabase().expenseDate(it.purchaseDate) }
        _transactionsListLiveData.postValue(transactionListSorted)
    }



}
