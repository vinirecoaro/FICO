package com.example.fico.presentation.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.DataStoreManager
import com.example.fico.model.Expense
import com.example.fico.api.FirebaseAPI
import com.example.fico.api.FormatValuesToDatabase
import com.example.fico.api.ArrangeDataToUpdateToDatabase
import com.example.fico.api.FormatValuesFromDatabase
import com.example.fico.model.Earning
import com.example.fico.model.InformationPerMonthExpense
import com.example.fico.model.RecurringTransaction
import com.example.fico.model.Transaction
import com.example.fico.utils.DateFunctions
import com.example.fico.utils.constants.StringConstants
import kotlinx.coroutines.*
import java.math.BigDecimal
import java.math.RoundingMode

class AddTransactionViewModel(
    private val firebaseAPI: FirebaseAPI,
    private val dataStore: DataStoreManager
) : ViewModel() {

    private val _paymentDay = MutableLiveData<String>()
    val paymentDayLiveData: LiveData<String> = _paymentDay
    private val _daysForClosingBill = MutableLiveData<String>()
    val daysForClosingBill: LiveData<String> = _daysForClosingBill
    private val _addExpenseResult = MutableLiveData<Boolean>()
    val addExpenseResult: LiveData<Boolean> = _addExpenseResult
    private val _setDefaultBudgetResult = MutableLiveData<Boolean>()
    val setDefaultBudgetResult : LiveData<Boolean> = _setDefaultBudgetResult
    private val arrangeDataToUpdateToDatabase  = ArrangeDataToUpdateToDatabase()
    private var operation : String = StringConstants.ADD_TRANSACTION.ADD_EXPENSE
    private val _paymentDateSwitchInitialState = MutableLiveData<Boolean>()
    val paymentDateSwitchInitialStateLiveData: LiveData<Boolean> = _paymentDateSwitchInitialState
    private val _addEarningResult = MutableLiveData<Boolean>()
    val addEarningResult: LiveData<Boolean> = _addEarningResult
    private val _recurringMode = MutableLiveData<String>()
    val recurringMode: LiveData<String> = _recurringMode
    private val _addRecurringTransactionResult = MutableLiveData<Boolean>()
    val addRecurringTransactionResult: LiveData<Boolean> = _addRecurringTransactionResult
    private val _recurringTransactionsList = MutableLiveData<Pair<List<RecurringTransaction>, String>>()
    val recurringTransactionsList: LiveData<Pair<List<RecurringTransaction>, String>> = _recurringTransactionsList

    init {
        getPaymentDateSwitchState()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun addExpense(
        price: String,
        description: String,
        category: String,
        paymentDate: String,
        purchaseDate: String,
        installment: Boolean,
        nOfInstallments: Int = 1
    ) {
        viewModelScope.async(Dispatchers.IO) {

            val formattedPaymentDate = FormatValuesToDatabase().expenseDate(paymentDate)

            val formattedPurchaseDate = FormatValuesToDatabase().expenseDate(purchaseDate)

            val formattedInputDate =
                "${FormatValuesToDatabase().expenseDate(DateFunctions().getCurrentDate())}-${FormatValuesToDatabase().timeNow()}"

            val formattedPrice = FormatValuesToDatabase().expensePrice(price, nOfInstallments)

            val expense = Expense(
                "",
                formattedPrice,
                description,
                category,
                formattedPaymentDate,
                formattedPurchaseDate,
                formattedInputDate
            )

            val expenseList = arrangeDataToUpdateToDatabase.addToExpenseList(
                expense,
                installment,
                nOfInstallments,
                false
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

            var result =
                firebaseAPI.addExpense(expenseList, updatedTotalExpense, updatedInformationPerMonth)
            result.fold(
                onSuccess = {
                    //Update dataStore expense List and infoPerMonth
                    val expenseListFormatted = mutableListOf<Expense>()
                    val currentInfoPerMonth = dataStore.getExpenseInfoPerMonth()
                    val updatedInfoPerMonth = mutableListOf<InformationPerMonthExpense>()
                    expenseList.forEach { expense ->
                        expenseListFormatted.add(
                            Expense(
                                expense.id,
                                expense.price,
                                expense.description,
                                expense.category,
                                FormatValuesFromDatabase().date(expense.paymentDate),
                                FormatValuesFromDatabase().date(expense.purchaseDate),
                                expense.inputDateTime,
                                expense.nOfInstallment
                            )
                        )
                        //Verify if exist month and get its information
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
                    dataStore.updateInfoPerMonthExpense(updatedInfoPerMonth)
                    dataStore.updateExpenseList(expenseListFormatted)

                    //Update dataStore expense Months
                    val expenseMonths = mutableListOf<String>()
                    updatedInformationPerMonth.forEach {
                        expenseMonths.add(
                            it.date
                        )
                    }
                    dataStore.updateExpenseMonths(expenseMonths)

                    //Update dataStore Total Expense
                    val currentTotalExpense = BigDecimal(dataStore.getTotalExpense())
                    val updatedTotalExpenseDataStore = currentTotalExpense.add(BigDecimal(FormatValuesToDatabase().expensePrice(price,1)))
                    dataStore.updateTotalExpense(updatedTotalExpenseDataStore.toString())

                    //Update observable
                    _addExpenseResult.postValue(true)

                },
                onFailure = {
                    _addExpenseResult.postValue(false)
                }
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun addEarning(
        value: String,
        description: String,
        category: String,
        date: String,
    ){
        viewModelScope.async(Dispatchers.IO){
            val formattedDate = FormatValuesToDatabase().expenseDate(date)

            val formattedInputDate =
                "${FormatValuesToDatabase().expenseDate(DateFunctions().getCurrentDate())}-${FormatValuesToDatabase().timeNow()}"

            val formattedValue = FormatValuesToDatabase().expensePrice(value, 1)

            val randonNum = arrangeDataToUpdateToDatabase.generateRandomAddress(5)

            val inputTime = FormatValuesToDatabase().timeNow()

            val earningId = "${formattedDate}-${inputTime}-${randonNum}"

            val earning = Earning(
                earningId,
                formattedValue,
                description,
                category,
                formattedDate,
                formattedInputDate
            )

            firebaseAPI.addEarning(earning).fold(
                onSuccess = {
                    dataStore.updateEarningList(earning)
                    _addEarningResult.postValue(true)
                },
                onFailure = {
                    _addEarningResult.postValue(false)
                }
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun addRecurringTransaction(
        price: String,
        description: String,
        category: String,
        day: String,
        type : String
    ){
        viewModelScope.async(Dispatchers.IO){
            val formattedInputDate =
                "${FormatValuesToDatabase().expenseDate(DateFunctions().getCurrentDate())}-${FormatValuesToDatabase().timeNow()}"

            val formattedPrice = FormatValuesToDatabase().expensePrice(price, 1)

            val randonNum = arrangeDataToUpdateToDatabase.generateRandomAddress(5)

            val id = "${formattedInputDate}-${randonNum}"

            val recurringTransaction = RecurringTransaction(
                id,
                formattedPrice,
                description,
                category,
                day,
                formattedInputDate,
                type
            )

            firebaseAPI.addRecurringTransaction(recurringTransaction).fold(
                onSuccess = {
                    val recurringTransactionList = mutableListOf<RecurringTransaction>()
                    recurringTransactionList.add(recurringTransaction)
                    dataStore.updateRecurringExpensesList(recurringTransactionList)
                    _addRecurringTransactionResult.postValue(true)
                },
                onFailure = {
                    _addRecurringTransactionResult.postValue(false)
                }
            )
        }
    }

    fun getRecurringTransactionList(type : String){
        viewModelScope.async {
            try {
                val recurringTransactionsList = dataStore.getRecurringTransactionsList()
                val recurringTransactionTypeFilteredList = recurringTransactionsList.filter { it.type == type }
                val sortedExpenses = recurringTransactionTypeFilteredList.sortedByDescending { it.description }
                val result = Pair(sortedExpenses, type)
                _recurringTransactionsList.value = result

            }catch (error: Exception){
                Log.e("recurring expenses","Fail in get recurring expenses list: ${error.message}")
            }
        }
    }

    fun recurringExpenseToTransaction(recurringExpenseList : List<RecurringTransaction>) : List<Transaction>{
        val transactionList = mutableListOf<Transaction>()
        for(recurringExpense in recurringExpenseList){
            val transaction = Transaction(
                id = recurringExpense.id,
                price = recurringExpense.price,
                description = recurringExpense.description,
                category = recurringExpense.category,
                paymentDate = recurringExpense.day,
                purchaseDate = recurringExpense.day,
                inputDateTime = recurringExpense.inputDateTime,
                nOfInstallment = "1",
                type = StringConstants.DATABASE.RECURRING_EXPENSE
            )
            transactionList.add(transaction)
        }
        return transactionList
    }

    suspend fun checkIfExistDefaultBudget(): Deferred<Boolean> {
        return viewModelScope.async(Dispatchers.IO) {
            firebaseAPI.checkIfExistDefaultBudget()
        }
    }

    suspend fun setDefaultBudget(budget: String) {
        viewModelScope.async(Dispatchers.IO){
            firebaseAPI.setDefaultBudget(budget).fold(
                onSuccess = {
                    dataStore.updateDefaultBudget(budget)
                    _setDefaultBudgetResult.postValue(true)
                },
                onFailure = {
                    _setDefaultBudgetResult.postValue(false)
                }
            )
        }
    }

    fun getDefaultPaymentDay() {
        viewModelScope.launch {
            val result = dataStore.getDefaultPaymentDay()
            if(result != null){
                _paymentDay.value = result!!
            }
        }
    }

    fun getDaysForClosingBill() {
        viewModelScope.launch {
            val result = dataStore.getDaysForClosingBill()
            if(result != null){
                _daysForClosingBill.value = result!!
            }
        }
    }

    fun changeOperation(operation : String){
        this.operation = operation
    }

    fun getOperation() : String{
        return operation
    }

    private fun getPaymentDateSwitchState(){
        viewModelScope.async(Dispatchers.IO) {
            val state = dataStore.getPaymentDateSwitchInitialState()
            _paymentDateSwitchInitialState.postValue(state)
        }
    }

    fun updateRecurringMode(mode : String){
        viewModelScope.async(Dispatchers.IO){
            _recurringMode.postValue(mode)
        }
    }
}