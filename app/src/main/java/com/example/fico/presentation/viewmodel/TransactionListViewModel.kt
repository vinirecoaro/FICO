package com.example.fico.presentation.viewmodel

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.DataStoreManager
import com.example.fico.api.TransactionsFunctions
import com.example.fico.model.Expense
import com.example.fico.api.FirebaseAPI
import com.example.fico.api.FormatValuesFromDatabase
import com.example.fico.api.FormatValuesToDatabase
import com.example.fico.model.Earning
import com.example.fico.model.InformationPerMonthExpense
import com.example.fico.model.Transaction
import com.example.fico.model.TransactionsCategory
import com.example.fico.presentation.fragments.transaction_list.TransactionFragmentState
import com.example.fico.utils.DateFunctions
import com.example.fico.utils.constants.StringConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
    private val _filteredTransactionsListLiveData = MutableLiveData<List<Transaction>>()
    private val _showListLiveData = MutableLiveData<List<Transaction>>()
    val showListLiveData : LiveData<List<Transaction>> = _showListLiveData
    private val _expenseMonthsLiveData = MutableLiveData<List<String>>()
    val expenseMonthsLiveData: LiveData<List<String>> = _expenseMonthsLiveData
    private val _deleteExpenseResult = MutableLiveData<Boolean>()
    val deleteExpenseResult: LiveData<Boolean> = _deleteExpenseResult
    private val _deleteEarningResult = MutableLiveData<Boolean>()
    val deleteEarningResult: LiveData<Boolean> = _deleteEarningResult
    private val _deletedItem = MutableLiveData<Transaction>(Transaction.empty())
    private val _undoDeletedItem = MutableLiveData<Transaction>(Transaction.empty())
    private val _addExpenseResult = MutableLiveData<Boolean>()
    val addExpenseResult: LiveData<Boolean> = _addExpenseResult
    private val _installmentExpenseSwiped = MutableLiveData<Boolean>()
    val installmentExpenseSwiped: LiveData<Boolean> = _installmentExpenseSwiped
    private val transactionsFunctions  = TransactionsFunctions()
    private val _monthFilterLiveData = MutableLiveData<String>()
    val monthFilterLiveData: LiveData<String>
        get() = _monthFilterLiveData
    private val _uiState = MutableStateFlow<TransactionFragmentState<Nothing>>(
        TransactionFragmentState.Loading)
    val uiState : StateFlow<TransactionFragmentState<Nothing>> = _uiState.asStateFlow()
    private val _isFiltered = MutableLiveData<Boolean>()
    val isFiltered : LiveData<Boolean> = _isFiltered
    private val _descriptionFilterState = MutableLiveData<Boolean>()
    val descriptionFilterState : LiveData<Boolean> = _descriptionFilterState
    private val _descriptionFilterValues = MutableLiveData<MutableList<String>>()
    val descriptionFilterValues : LiveData<MutableList<String>> = _descriptionFilterValues
    private val _dateFilterState = MutableLiveData<Boolean>()
    val dateFilterState : LiveData<Boolean> = _dateFilterState
    private val _dateFilterValue = MutableLiveData<Pair<String,String>>()
    val dateFilterValue : LiveData<Pair<String,String>> = _dateFilterValue
    private val _categoryFilterState = MutableLiveData<Boolean>()
    val categoryFilterState : LiveData<Boolean> = _categoryFilterState
    private val _categoryFilterValue = MutableLiveData<MutableList<String>>()
    val categoryFilterValue : LiveData<MutableList<String>> = _categoryFilterValue
    private val _returningFromEdit = MutableLiveData<Boolean>()
    val returningFromEdit : LiveData<Boolean> = _returningFromEdit
    private val _editingTransaction = MutableLiveData<Transaction>()
    private val _transactionTypeFilter = MutableLiveData<String>(StringConstants.DATABASE.TRANSACTION)
    val transactionTypeFilter : LiveData<String> = _transactionTypeFilter
    private val _addEarningResult = MutableLiveData<Boolean>()
    val addEarningResult: LiveData<Boolean> = _addEarningResult
    private val operation = MutableLiveData<String>("")
    private val _internetConnection = MutableLiveData<Boolean>()
    val internetConnection: LiveData<Boolean> = _internetConnection
    private val _showListOrderingType = MutableLiveData<String>(StringConstants.GENERAL.ASCENDING)

    fun updateFilter(filter: String) {
        _monthFilterLiveData.value = filter
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
                val earningMonthInfoList = dataStore.getEarningMonthInfoList()
                transactionMonthsList.addAll(expenseMonths)
                transactionMonthsList.addAll(earningMonthInfoList.map { it.month })
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
        operation.value = StringConstants.OPERATIONS.DELETE
        viewModelScope.async(Dispatchers.IO) {
            val result = firebaseAPI.deleteExpense(expense)
            result.fold(
                onSuccess = {
                    _deletedItem.postValue(expense.toTransaction())

                    //Get current dataStore Expense list
                    val currentList = dataStore.getExpenseList().toMutableList()

                    //Remove from dataStore expense List
                    currentList.removeAll { it.id == expense.id }
                    dataStore.updateAndResetExpenseList(currentList.toList())

                    //Update expenseList on screen
                    updateShowFilteredList()

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

    fun deleteEarning(earning : Earning){
        operation.value = StringConstants.OPERATIONS.DELETE
        viewModelScope.async(Dispatchers.IO) {
            firebaseAPI.deleteEarning(earning).fold(
                onSuccess = {
                    _deletedItem.postValue(earning.toTransaction())
                    dataStore.deleteFromEarningList(earning)
                    updateShowFilteredList()
                    _deleteEarningResult.postValue(true)
                },
                onFailure = {
                    _deleteEarningResult.postValue(false)
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
        operation.value = StringConstants.OPERATIONS.UNDO_DELETE
        viewModelScope.async(Dispatchers.IO) {
            val formattedInputDate =
                "${FormatValuesToDatabase().expenseDate(DateFunctions().getCurrentDate())}-${FormatValuesToDatabase().timeNow()}"

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

            val expenseList = transactionsFunctions.addToExpenseList(
                expense,
                installment,
                nOfInstallments,
                false
            )

            val undoDeletedTransaction = expenseList[0].toTransaction()

            _undoDeletedItem.postValue(undoDeletedTransaction)

            val updatedTotalExpense = transactionsFunctions.calculateUpdatedTotalExpense(
                dataStore.getTotalExpense(),
                formattedPrice,
                nOfInstallments
            )

            val updatedInformationPerMonth =
                transactionsFunctions.calculateExpenseInformationPerMonthAfterAddExpense(
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
                    updateShowFilteredList()
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun undoDeleteEarning(deletedEarning: Earning, ) {
        _undoDeletedItem.postValue(deletedEarning.toTransaction())
        operation.value = StringConstants.OPERATIONS.UNDO_DELETE
        viewModelScope.async(Dispatchers.IO) {
            firebaseAPI.addEarning(deletedEarning).fold(
                onSuccess = {
                    dataStore.updateEarningList(deletedEarning)
                    val earningList = dataStore.getEarningsList()
                    dataStore.updateAndResetEarningMonthInfoList(earningList)
                    updateShowFilteredList()
                    _addEarningResult.postValue(true)
                },
                onFailure = {
                    _addEarningResult.postValue(false)
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
        _showListLiveData.postValue(transactionListSorted)
        _transactionsListLiveData.postValue(transactionListSorted)
    }

    fun applyDescriptionFilter (filter : String){
        var currentList = mutableListOf<Transaction>()
        if(_showListLiveData.value != null){
            _isFiltered.postValue(true)
            _descriptionFilterState.value = true
            currentList.addAll(_showListLiveData.value!!)
        }
        val filteredList = mutableListOf<Transaction>()
        filteredList.addAll(currentList.filter { it.description.lowercase().contains(filter.lowercase()) })
        _filteredTransactionsListLiveData.postValue(filteredList)
        _showListLiveData.postValue(filteredList)
        _descriptionFilterValues.postValue((_descriptionFilterValues.value ?: mutableListOf()).apply {
            add(filter)
        })
    }

    fun applyCategoryFilter(categoriesList : List<String>){
        var currentList = mutableListOf<Transaction>()
        if(_showListLiveData.value != null){
            _isFiltered.postValue(true)
            _categoryFilterState.value = true
            currentList.addAll(_showListLiveData.value!!)
        }
        val filteredList = mutableListOf<Transaction>()
        filteredList.addAll(currentList.filter { it.category in categoriesList })
        _filteredTransactionsListLiveData.postValue(filteredList)
        _showListLiveData.postValue(filteredList)
        _categoryFilterValue.postValue((_descriptionFilterValues.value ?: mutableListOf()).apply {
            addAll(categoriesList)
        })
    }

    fun calculateFilteredTotalValue(filteredTransactionList : List<Transaction>) : BigDecimal{
        var total = BigDecimal(0)
        filteredTransactionList.forEach { transaction ->
            if(transaction.type == StringConstants.DATABASE.EXPENSE){
                total = total.subtract(BigDecimal(transaction.price))
            }else if(transaction.type == StringConstants.DATABASE.EARNING){
                total = total.add(BigDecimal(transaction.price))
            }
        }
        return total
    }

    fun setDescriptionFilterState(state : Boolean){
        _descriptionFilterState.value = state
    }

    fun setIsFilteredState(state : Boolean) {
        _isFiltered.postValue(state)
    }

    fun clearDescriptionFilterValues(){
        _descriptionFilterValues.value = mutableListOf()
    }

    fun setDateFilterState(state : Boolean){
        _dateFilterState.value = state
    }

    fun clearDateFilterValues(){
        _dateFilterValue.value = Pair("","")
    }

    fun setCategoryFilterState(state : Boolean){
        _categoryFilterState.value = state
    }

    fun clearCategoryFilterValues(){
        _categoryFilterValue.value = mutableListOf()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun applyDateFilter(dates : Pair<String,String>){
        var currentList = mutableListOf<Transaction>()
        if(_showListLiveData.value != null){
            _isFiltered.postValue(true)
            _dateFilterState.value = true
            currentList.addAll(_showListLiveData.value!!)
        }
        val filteredList = mutableListOf<Transaction>()
        _dateFilterValue.value = dates
        filteredList.addAll(currentList.filter { isDateInRange(it.paymentDate, dates.first, dates.second) })
        _filteredTransactionsListLiveData.postValue(filteredList)
        _showListLiveData.postValue(filteredList)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun isDateInRange(date: String, startDate: String, endDate: String): Boolean {
        // Defina o formato do parse
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        // Converta as Strings para LocalDate
        val targetDate = LocalDate.parse(date, formatter)
        val start = LocalDate.parse(startDate, formatter)
        val end = LocalDate.parse(endDate, formatter)

        // Verifique se está dentro do intervalo (inclusivo)
        return (targetDate.isEqual(start) || targetDate.isAfter(start)) &&
                (targetDate.isEqual(end) || targetDate.isBefore(end))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun showTransactionsBasedOnType(transactionType : String){
        if(transactionsListLiveData.value != null) {
            val allTransactionList = _transactionsListLiveData.value!!
            val finalFilteredList = mutableListOf<Transaction>()
            val typeFilteredList = mutableListOf<Transaction>()
            if (_isFiltered.value == false || _isFiltered.value == null) {
                finalFilteredList.addAll(allTransactionList)
            } else {
                val textFilteredList = mutableListOf<Transaction>()
                val dateFilteredList = mutableListOf<Transaction>()
                val categoryFilteredList = mutableListOf<Transaction>()

                //Apply description filter
                if (_descriptionFilterState.value == true) {
                    textFilteredList.addAll(
                        allTransactionList.filter { transaction ->
                            _descriptionFilterValues.value!!.all { textFilter ->
                                transaction.description.contains(textFilter, ignoreCase = true)
                            }
                        }
                    )
                    finalFilteredList.clear()
                    finalFilteredList.addAll(textFilteredList)
                }else{
                    finalFilteredList.addAll(allTransactionList)
                }
                //Apply date filter
                if (_dateFilterState.value == true) {
                    val dateFilter = _dateFilterValue.value!!
                    dateFilteredList.addAll(
                        finalFilteredList.filter {
                            isDateInRange(
                                it.paymentDate,
                                dateFilter.first,
                                dateFilter.second
                            )
                        }
                    )
                    finalFilteredList.clear()
                    finalFilteredList.addAll(dateFilteredList)
                }
                //Apply category filter
                if (_categoryFilterState.value == true){
                    val categoryFilter = _categoryFilterValue.value!!
                    categoryFilteredList.addAll(
                        finalFilteredList.filter { transaction ->
                            transaction.category in categoryFilter
                        }
                    )
                    finalFilteredList.clear()
                    finalFilteredList.addAll(categoryFilteredList)
                }

            }
            if(transactionType == StringConstants.DATABASE.EXPENSE){
                typeFilteredList.addAll(finalFilteredList.filter { it.type == StringConstants.DATABASE.EXPENSE })
            }else if(transactionType == StringConstants.DATABASE.EARNING){
                typeFilteredList.addAll(finalFilteredList.filter { it.type == StringConstants.DATABASE.EARNING })
            }else{
                typeFilteredList.addAll(finalFilteredList)
            }

            _showListLiveData.postValue(typeFilteredList)
        }
    }

    fun changeReturningFromEditState(state : Boolean){
        _returningFromEdit.value = state
    }

    fun updateShowFilteredList(){
        viewModelScope.async(Dispatchers.IO){
            val currentList = _showListLiveData.value
            val updatedTransactionList = dataStore.getTransactionList()
            val filteredTransactionList = mutableListOf<Transaction>()

            if(currentList != null){

                when (operation.value) {

                    StringConstants.OPERATIONS.DELETE -> {
                        val excludedItem = _deletedItem.value!!
                        val commondId = if(excludedItem.id.length > 25){
                            FormatValuesFromDatabase().commonIdOnInstallmentExpense(excludedItem.id)
                        }else{
                            excludedItem.id
                        }
                        filteredTransactionList.addAll(currentList.filter {
                            if(it.id.length > 25){
                                FormatValuesFromDatabase().commonIdOnInstallmentExpense(it.id) != commondId
                            }else{
                                it.id != commondId
                            }
                        })

                        operation.postValue(StringConstants.OPERATIONS.NO_OPERATION)
                    }

                    StringConstants.OPERATIONS.UNDO_DELETE -> {
                        filteredTransactionList.addAll(currentList)
                        val transaction = _undoDeletedItem.value!!
                        val formattedPaymentDate = FormatValuesFromDatabase().date(transaction.paymentDate)
                        val formattedPurchaseDate = FormatValuesFromDatabase().date(transaction.purchaseDate)
                        transaction.paymentDate = formattedPaymentDate
                        transaction.purchaseDate = formattedPurchaseDate
                        if(transaction.type == StringConstants.DATABASE.EXPENSE){
                            val formattedPrice = transaction.price.replace("-","")
                            transaction.price = formattedPrice
                        }
                        filteredTransactionList.add(transaction)
                        operation.postValue(StringConstants.OPERATIONS.NO_OPERATION)
                    }

                    StringConstants.OPERATIONS.UPDATE -> {
                        val oldTransaction = _editingTransaction.value
                        if(oldTransaction != null && oldTransaction.id != ""){

                            //check if is installment expense
                            if(oldTransaction.id.length > 25){
                                val updatedTransactions = dataStore.getInstallmentExpense(oldTransaction)
                                val commonId = FormatValuesFromDatabase().commonIdOnInstallmentExpense(oldTransaction.id)

                                // Update List
                                filteredTransactionList.addAll(currentList.filter {
                                    val listItemId = FormatValuesFromDatabase().commonIdOnInstallmentExpense(it.id)
                                    listItemId != commonId
                                }.toMutableList())
                                if(_monthFilterLiveData.value != null){
                                    if(_monthFilterLiveData.value != ""){
                                        updatedTransactions.forEach { updatedTransaction ->
                                            if(
                                                DateFunctions().YYYYmmDDtoYYYYmm(FormatValuesToDatabase().expenseDate(updatedTransaction.paymentDate)) ==
                                                FormatValuesToDatabase().formatDateFromFilterToDatabaseForInfoPerMonth(_monthFilterLiveData.value!!)
                                            ){
                                                filteredTransactionList.add(updatedTransaction)
                                            }
                                        }
                                    }else{
                                        filteredTransactionList.addAll(updatedTransactions)
                                    }
                                }
                            }else{

                                //Prepare updated transaction and take common Id
                                val updatedTransaction = dataStore.getTransaction(oldTransaction)

                                // Update List
                                filteredTransactionList.addAll(currentList.filter {
                                    it.id != updatedTransaction.id
                                }.toMutableList())
                                if(_monthFilterLiveData.value != null){
                                    if(_monthFilterLiveData.value != ""){
                                        if(
                                            DateFunctions().YYYYmmDDtoYYYYmm(FormatValuesToDatabase().expenseDate(updatedTransaction.paymentDate)) ==
                                            FormatValuesToDatabase().formatDateFromFilterToDatabaseForInfoPerMonth(_monthFilterLiveData.value!!)
                                        ){
                                            filteredTransactionList.add(updatedTransaction)
                                        }
                                    }else{
                                        filteredTransactionList.add(updatedTransaction)
                                    }
                                }
                            }
                        }
                        operation.postValue(StringConstants.OPERATIONS.NO_OPERATION)
                    }

                    StringConstants.OPERATIONS.SWIPPED_INSTALLMENT_EXPENSE -> {
                        filteredTransactionList.addAll(currentList)
                        operation.postValue(StringConstants.OPERATIONS.NO_OPERATION)
                    }

                    StringConstants.OPERATIONS.CLEAR_MONTH_FILTER -> {
                        when (_transactionTypeFilter.value) {
                            StringConstants.DATABASE.TRANSACTION -> {
                                filteredTransactionList.addAll(updatedTransactionList)
                            }

                            StringConstants.DATABASE.EXPENSE -> {
                                filteredTransactionList.addAll(
                                    updatedTransactionList.filter { it.type == StringConstants.DATABASE.EXPENSE }
                                )
                            }

                            StringConstants.DATABASE.EARNING -> {
                                filteredTransactionList.addAll(
                                    updatedTransactionList.filter { it.type == StringConstants.DATABASE.EARNING }
                                )
                            }
                        }
                        operation.postValue(StringConstants.OPERATIONS.NO_OPERATION)
                    }

                    StringConstants.OPERATIONS.NO_OPERATION -> {
                        filteredTransactionList.addAll(currentList)
                    }
                }
            }

            val sortedList = filteredTransactionList.sortedByDescending { FormatValuesToDatabase().expenseDate(it.purchaseDate) }

            _showListLiveData.postValue(sortedList)

            //Update transaction list for when user clear filter
            if(_monthFilterLiveData.value != null && _monthFilterLiveData.value != ""){
                val findTransactionFromMonthDateFilter = updatedTransactionList.filter {
                    val transactDate = FormatValuesToDatabase().expenseDateForInfoPerMonth(it.paymentDate)
                    val filterDate = FormatValuesToDatabase().formatDateFromFilterToDatabaseForInfoPerMonth(_monthFilterLiveData.value!!)
                    transactDate == filterDate
                }
                _transactionsListLiveData.postValue(findTransactionFromMonthDateFilter)
            }else{
                _transactionsListLiveData.postValue(updatedTransactionList)
            }
        }
    }

    fun updateEditingTransaction(transaction : Transaction){
        _editingTransaction.postValue(transaction)
    }

    fun getEditingTransaction() : Transaction{
        return _editingTransaction.value!!
    }

    fun updateTransactionTypeFilter(type : String){
        _transactionTypeFilter.postValue(type)
    }

    fun updateOperation(operationValue : String){
        operation.value = operationValue
    }

    fun getDeletedItem() : Transaction{
        return _deletedItem.value!!
    }

    fun updateDeletedItem(transaction : Transaction) {
        _deletedItem.value = transaction
    }

    fun updateInternetConnectionState(state : Boolean){
        _internetConnection.postValue(state)
    }

    fun updateShowList(transactionList : List<Transaction>){
        _showListLiveData.postValue(transactionList)
    }

    fun orderShowListByValue(){
        if(_showListOrderingType.value == StringConstants.GENERAL.ASCENDING){
            _showListOrderingType.value = StringConstants.GENERAL.DESCENDING
            _showListLiveData.postValue(_showListLiveData.value!!.sortedByDescending { BigDecimal(it.price) })
        }else if(_showListOrderingType.value == StringConstants.GENERAL.DESCENDING){
            _showListOrderingType.value = StringConstants.GENERAL.ASCENDING
            _showListLiveData.postValue(_showListLiveData.value!!.sortedBy { BigDecimal(it.price) })
        }
    }

    fun getShowListCategories(context : Context) : List<TransactionsCategory>{
        if(_showListLiveData.value != null){
            val transactionsCategoryList = mutableListOf<TransactionsCategory>()
            val categoriesList = mutableSetOf<String>()
            _showListLiveData.value!!.forEach { transaction ->
                categoriesList.add(transaction.category)
            }
            val allTransactionsCategoryList = TransactionsCategory.getTransactionCategoryList()
            categoriesList.forEach { description ->
                val category = allTransactionsCategoryList.find { context.getString(it.descriptionResId) == description }
                if(category != null){
                    transactionsCategoryList.add(category)
                }
            }
            return transactionsCategoryList.sortedBy { context.getString(it.descriptionResId) }
        }else{
            return emptyList()
        }
    }

}