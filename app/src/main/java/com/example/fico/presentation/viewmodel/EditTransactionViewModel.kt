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
import com.example.fico.model.RecurringTransaction
import com.example.fico.utils.DateFunctions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.math.BigDecimal
import java.math.RoundingMode

class EditTransactionViewModel(
    private val firebaseAPI : FirebaseAPI,
    private val dataStore: DataStoreManager
) : ViewModel() {

    private val _editExpenseResult = MutableLiveData<Boolean>()
    val editExpenseResult : LiveData<Boolean> = _editExpenseResult
    private val _deleteInstallmentExpenseResult = MutableLiveData<Boolean>()
    val deleteInstallmentExpenseResult : LiveData<Boolean> = _deleteInstallmentExpenseResult
    private val arrangeDataToUpdateToDatabase  = ArrangeDataToUpdateToDatabase()
    private val _editEarningResult = MutableLiveData<Boolean>()
    val editEarningResult : LiveData<Boolean> = _editEarningResult
    private val _editRecurringTransactionResult = MutableLiveData<Boolean>()
    val editRecurringTransactionResult : LiveData<Boolean> = _editRecurringTransactionResult
    private val _deleteExpenseResult = MutableLiveData<Boolean>()
    val deleteExpenseResult: LiveData<Boolean> = _deleteExpenseResult
    private val _deleteEarningResult = MutableLiveData<Boolean>()
    val deleteEarningResult: LiveData<Boolean> = _deleteEarningResult
    private val _deleteRecurringExpenseResult = MutableLiveData<Boolean>()
    val deleteRecurringExpenseResult: LiveData<Boolean> = _deleteRecurringExpenseResult

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
    )  {
        viewModelScope.async(Dispatchers.IO) {

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
                FormatValuesFromDatabase().installmentExpenseDescription(oldExpense.description),
                oldExpense.category,
                oldExpensePaymentDate,
                oldExpensePurchaseDate,
                "",
                oldExpenseNOfInstallment.toString()
            )

            val removeFromExpenseList = arrangeDataToUpdateToDatabase.removeFromExpenseList(oldExpenseFormatted, dataStore.getExpenseList())

            val newExpensePrice = FormatValuesToDatabase().expensePrice(price, nOfInstallments)
            val newExpensePaymentDate = FormatValuesToDatabase().expenseDate(paymentDate)
            val newExpensePurchaseDate = FormatValuesToDatabase().expenseDate(purchaseData)
            val formattedInputDate = "${FormatValuesToDatabase().expenseDate(DateFunctions().getCurrentlyDate())}-${FormatValuesToDatabase().timeNow()}"

            val newExpense = Expense(oldExpense.id, newExpensePrice, description, category, newExpensePaymentDate, newExpensePurchaseDate, formattedInputDate)

            val updatedTotalExpense = arrangeDataToUpdateToDatabase.calculateUpdatedTotalExpense(
                dataStore.getTotalExpense(),
                newExpense.price,
                nOfInstallments,
                oldExpenseFormatted.price,
                oldExpenseNOfInstallment)

            val updatedInformationPerMonth = arrangeDataToUpdateToDatabase.addToInformationPerMonth(
                newExpense,
                installment,
                nOfInstallments,
                dataStore.getExpenseInfoPerMonth(),
                dataStore.getDefaultBudget(),
                true,
                oldExpenseFormatted
            )

            val expenseList = arrangeDataToUpdateToDatabase.addToExpenseList(newExpense, installment, nOfInstallments, true)

            firebaseAPI.editExpense(expenseList, updatedTotalExpense,updatedInformationPerMonth, removeFromExpenseList, oldExpenseNOfInstallment).fold(
                onSuccess = {

                    //Update Total Expense on DataStore
                    val oldExpensePriceFullPrice = BigDecimal(oldExpense.price).multiply(BigDecimal(oldExpenseNOfInstallment))
                    val newExpensePriceFullPrice = BigDecimal(FormatValuesToDatabase().expensePrice(price,1))
                    val currentTotalExpenseDataStore = BigDecimal(dataStore.getTotalExpense())
                    val updatedTotalExpenseDataStore = currentTotalExpenseDataStore.subtract(oldExpensePriceFullPrice).add(newExpensePriceFullPrice).setScale(8, RoundingMode.HALF_UP)
                    dataStore.updateTotalExpense(updatedTotalExpenseDataStore.toString())

                    //Update expenseList and infoPerMonth on dataStore
                    val updatedExpenseListDataStore = dataStore.getExpenseList().toMutableList()
                    val updatedInfoPerMonthDataStore = dataStore.getExpenseInfoPerMonth().toMutableSet()
                        //Remove old expenses
                    removeFromExpenseList.forEach { expenseId ->
                        //Remove expenses from expense list
                        updatedExpenseListDataStore.removeAll{it.id == expenseId}
                    }
                        //Remove old expense values from info per month list
                    val oldExpenseList = arrangeDataToUpdateToDatabase.addToExpenseList(
                        oldExpenseFormatted,
                        installment,
                        oldExpenseFormatted.nOfInstallment.toInt(),
                        true
                    )
                    oldExpenseList.forEach {oldExpense ->
                        val oldExpenseDateYYYYmm = DateFunctions().YYYYmmDDtoYYYYmm(oldExpense.paymentDate)
                        val infoOfMonth = updatedInfoPerMonthDataStore.find { it.date == oldExpenseDateYYYYmm }
                        val currentMonthExpenseOldExpense = BigDecimal(infoOfMonth!!.monthExpense).setScale(8, RoundingMode.HALF_UP)
                        val currentAvailableNowOldExpense = BigDecimal(infoOfMonth.availableNow).setScale(8, RoundingMode.HALF_UP)
                        val updatedMonthExpenseOldExpense = currentMonthExpenseOldExpense.subtract(BigDecimal(oldExpense.price))
                        val updatedAvailableNowOldExpense = currentAvailableNowOldExpense.add(BigDecimal(oldExpense.price))
                        val updatedInfoOfMonth = InformationPerMonthExpense(
                            infoOfMonth.date,
                            updatedAvailableNowOldExpense.toString(),
                            infoOfMonth.budget,
                            updatedMonthExpenseOldExpense.toString()
                        )
                        updatedInfoPerMonthDataStore.removeAll{it.date == updatedInfoOfMonth.date}
                        updatedInfoPerMonthDataStore.add(updatedInfoOfMonth)
                    }
                        //Add new expenses
                    expenseList.forEach { newExpenseDataStore ->
                        val formattedExpense = Expense(
                            newExpenseDataStore.id,
                            newExpenseDataStore.price,
                            newExpenseDataStore.description,
                            newExpenseDataStore.category,
                            FormatValuesFromDatabase().date(newExpenseDataStore.paymentDate),
                            FormatValuesFromDatabase().date(newExpenseDataStore.purchaseDate),
                            newExpenseDataStore.inputDateTime
                        )
                        updatedExpenseListDataStore.add(formattedExpense)
                        //Add new expense values to info per month list
                        val newExpenseDateYYYYmm = DateFunctions().YYYYmmDDtoYYYYmm(newExpenseDataStore.paymentDate)
                        val infoOfMonth = updatedInfoPerMonthDataStore.find { it.date == newExpenseDateYYYYmm }
                        if(infoOfMonth != null){
                            val currentMonthExpenseNewExpense = BigDecimal(infoOfMonth.monthExpense).setScale(8, RoundingMode.HALF_UP)
                            val currentAvailableNowNewExpense = BigDecimal(infoOfMonth.availableNow).setScale(8, RoundingMode.HALF_UP)
                            val updatedMonthExpenseNewExpense = currentMonthExpenseNewExpense.add(BigDecimal(newExpenseDataStore.price))
                            val updatedAvailableNowNewExpense = currentAvailableNowNewExpense.subtract(BigDecimal(newExpenseDataStore.price))
                            val updatedInfoOfMonth = InformationPerMonthExpense(
                                infoOfMonth.date,
                                updatedAvailableNowNewExpense.toString(),
                                infoOfMonth.budget,
                                updatedMonthExpenseNewExpense.toString()
                            )
                            updatedInfoPerMonthDataStore.removeAll{it.date == updatedInfoOfMonth.date}
                            updatedInfoPerMonthDataStore.add(updatedInfoOfMonth)
                        }else{
                            val defaultBudget = BigDecimal(dataStore.getDefaultBudget()).setScale(8, RoundingMode.HALF_UP)
                            val updatedAvailableNowNewExpense = defaultBudget.subtract(BigDecimal(newExpenseDataStore.price)).setScale(8, RoundingMode.HALF_UP)
                            val updatedInfoOfMonth = InformationPerMonthExpense(
                                DateFunctions().YYYYmmDDtoYYYYmm(newExpenseDataStore.paymentDate),
                                updatedAvailableNowNewExpense.toString(),
                                defaultBudget.toString(),
                                newExpenseDataStore.price
                            )
                            updatedInfoPerMonthDataStore.removeAll{it.date == updatedInfoOfMonth.date}
                            updatedInfoPerMonthDataStore.add(updatedInfoOfMonth)
                        }
                    }
                    //Update expense list on dataStore
                    dataStore.updateAndResetExpenseList(updatedExpenseListDataStore)
                    dataStore.updateAndResetInfoPerMonthExpense(updatedInfoPerMonthDataStore.toList())

                    //Update expense months on dataStore
                    val updatedExpenseMonthsDataStore = mutableListOf<String>()
                    updatedInfoPerMonthDataStore.forEach{infoPerMonthDataStore ->
                        val monthExpense = BigDecimal(infoPerMonthDataStore.monthExpense).setScale(2,RoundingMode.HALF_UP)
                        val zeroBigDecimal = BigDecimal(0)
                        if(monthExpense > zeroBigDecimal){
                            updatedExpenseMonthsDataStore.add(infoPerMonthDataStore.date)
                        }
                    }
                    dataStore.updateAndResetExpenseMonths(updatedExpenseMonthsDataStore)

                    _editExpenseResult.postValue(true)
                },
                onFailure = {
                    _editExpenseResult.postValue(false)
                }
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun saveEditEarning(
        oldEarning: Earning,
        value: String,
        description: String,
        category: String,
        date: String,
    )  {
        viewModelScope.async(Dispatchers.IO) {

            val newEarningValue = FormatValuesToDatabase().expensePrice(value, 1)
            val newEarningDate = FormatValuesToDatabase().expenseDate(date)
            val formattedInputDate = "${FormatValuesToDatabase().expenseDate(DateFunctions().getCurrentlyDate())}-${FormatValuesToDatabase().timeNow()}"

            val newEarning = Earning(oldEarning.id, newEarningValue, description, category, newEarningDate, formattedInputDate)

            firebaseAPI.editEarning(newEarning).fold(
                onSuccess = {
                    dataStore.updateEarningList(newEarning)
                    _editEarningResult.postValue(true)
                },
                onFailure = {
                    _editEarningResult.postValue(false)
                }
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun saveEditRecurringTransaction(
        oldRecurringTransaction: RecurringTransaction,
        value: String,
        description: String,
        category: String,
        day: String,
    )  {
        viewModelScope.async(Dispatchers.IO) {

            val newValue = FormatValuesToDatabase().expensePrice(value, 1)
            val formattedInputDate = "${FormatValuesToDatabase().expenseDate(DateFunctions().getCurrentlyDate())}-${FormatValuesToDatabase().timeNow()}"

            val newRecurringExpense = RecurringTransaction(oldRecurringTransaction.id, newValue, description, category, day, formattedInputDate, oldRecurringTransaction.type)

            firebaseAPI.editRecurringExpense(newRecurringExpense).fold(
                onSuccess = {
                    dataStore.updateRecurringExpenseList(newRecurringExpense)
                    _editRecurringTransactionResult.postValue(true)
                },
                onFailure = {
                    _editRecurringTransactionResult.postValue(false)
                }
            )
        }
    }

    fun deleteInstallmentExpense(expense : Expense){
        viewModelScope.async(Dispatchers.IO){
            var expensePaymentDate = FormatValuesToDatabase().expenseDate(FormatValuesFromDatabase().installmentExpenseInitialDate(expense.id,expense.paymentDate))
            var expensePurchaseDate = FormatValuesToDatabase().expenseDate(expense.purchaseDate)
            var expenseNOfInstallment = FormatValuesFromDatabase().installmentExpenseNofInstallment(expense.id).toInt()

            val formattedExpense = Expense(
                expense.id,
                BigDecimal(expense.price).multiply(BigDecimal("-1")).toString(), FormatValuesFromDatabase().installmentExpenseDescription(expense.description),
                expense.category,
                expensePaymentDate,
                expensePurchaseDate,
                "",
                expenseNOfInstallment.toString()
            )

            //Id's to remove from expense list
            val removeFromExpenseList = arrangeDataToUpdateToDatabase.removeFromExpenseList(
                formattedExpense,dataStore.getExpenseList()
            )

            //Updated total expense
            val updatedTotalExpense = arrangeDataToUpdateToDatabase.calculateUpdatedTotalExpense(
                dataStore.getTotalExpense(),
                formattedExpense.price,
                expenseNOfInstallment
            )

            //Updated info per month
            val updatedInformationPerMonthExpense = arrangeDataToUpdateToDatabase.addToInformationPerMonth(
                expense = formattedExpense,
                installment = true,
                newExpenseNOfInstallments =  expenseNOfInstallment,
                editExpense = false,
                currentInfoPerMonth = dataStore.getExpenseInfoPerMonth(),
                defaultBudget = dataStore.getDefaultBudget()
            )

            firebaseAPI.deleteInstallmentExpense(
                removeFromExpenseList,
                expenseNOfInstallment,
                updatedTotalExpense,
                updatedInformationPerMonthExpense
            ).fold(
                onSuccess = {
                    //Update Total Expense on DataStore
                    val newExpensePriceFullPrice = BigDecimal(expense.price).multiply(BigDecimal(expenseNOfInstallment))
                    val currentTotalExpenseDataStore = BigDecimal(dataStore.getTotalExpense())
                    val updatedTotalExpenseDataStore = currentTotalExpenseDataStore.subtract(newExpensePriceFullPrice).setScale(8, RoundingMode.HALF_UP)
                    dataStore.updateTotalExpense(updatedTotalExpenseDataStore.toString())

                    //Update expenseList and infoPerMonth on dataStore
                    val updatedExpenseListDataStore = dataStore.getExpenseList().toMutableList()
                    val updatedInfoPerMonthDataStore = dataStore.getExpenseInfoPerMonth().toMutableSet()
                    val expensesToRemoveList = mutableListOf<Expense>()
                    //Remove expenses
                    removeFromExpenseList.forEach { expenseId ->
                        //Save expenses that will be removed
                        val expenseFromDataStoreList = updatedExpenseListDataStore.find { it.id == expenseId }
                        expensesToRemoveList.add(expenseFromDataStoreList!!)
                        //Remove expenses from expense list
                        updatedExpenseListDataStore.removeAll{it.id == expenseId}
                    }
                    //Remove expense values from info per month list
                    expensesToRemoveList.forEach {expenseToRemove ->
                        val oldExpenseDateYYYYmm = DateFunctions().YYYYmmDDtoYYYYmm(FormatValuesToDatabase().expenseDate(expenseToRemove.paymentDate))
                        val infoOfMonth = updatedInfoPerMonthDataStore.find { it.date == oldExpenseDateYYYYmm }
                        val currentMonthExpenseOldExpense = BigDecimal(infoOfMonth!!.monthExpense).setScale(8, RoundingMode.HALF_UP)
                        val currentAvailableNowOldExpense = BigDecimal(infoOfMonth.availableNow).setScale(8, RoundingMode.HALF_UP)
                        val updatedMonthExpenseOldExpense = currentMonthExpenseOldExpense.subtract(BigDecimal(expenseToRemove.price))
                        val updatedAvailableNowOldExpense = currentAvailableNowOldExpense.add(BigDecimal(expenseToRemove.price))
                        val updatedInfoOfMonth = InformationPerMonthExpense(
                            infoOfMonth.date,
                            updatedAvailableNowOldExpense.toString(),
                            infoOfMonth.budget,
                            updatedMonthExpenseOldExpense.toString()
                        )
                        updatedInfoPerMonthDataStore.removeAll{it.date == updatedInfoOfMonth.date}
                        updatedInfoPerMonthDataStore.add(updatedInfoOfMonth)
                    }
                    //Update expense list on dataStore
                    dataStore.updateAndResetExpenseList(updatedExpenseListDataStore)
                    dataStore.updateAndResetInfoPerMonthExpense(updatedInfoPerMonthDataStore.toList())

                    //Update expense months on dataStore
                    val updatedExpenseMonthsDataStore = mutableListOf<String>()
                    updatedInfoPerMonthDataStore.forEach{infoPerMonthDataStore ->
                        val monthExpense = BigDecimal(infoPerMonthDataStore.monthExpense).setScale(2,RoundingMode.HALF_UP)
                        val zeroBigDecimal = BigDecimal(0)
                        if(monthExpense > zeroBigDecimal){
                            updatedExpenseMonthsDataStore.add(infoPerMonthDataStore.date)
                        }
                    }
                    dataStore.updateAndResetExpenseMonths(updatedExpenseMonthsDataStore)

                    _deleteInstallmentExpenseResult.postValue(true)
                },
                onFailure = {
                    _deleteInstallmentExpenseResult.postValue(false)
                }
            )
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.async(Dispatchers.IO) {
            val formattedExpense = expense
            formattedExpense.price = "-${expense.price}"
            formattedExpense.paymentDate = FormatValuesToDatabase().expenseDate(expense.paymentDate)
            formattedExpense.purchaseDate = FormatValuesToDatabase().expenseDate(expense.purchaseDate)

            val result = firebaseAPI.deleteExpense(formattedExpense)
            result.fold(
                onSuccess = {

                    //Get current dataStore Expense list
                    val currentList = dataStore.getExpenseList().toMutableList()

                    //Remove from dataStore expense List
                    currentList.removeAll { it.id == formattedExpense.id }
                    dataStore.updateAndResetExpenseList(currentList.toList())

                    //Remove from dataStore expense Months List
                    val removedExpenseMonth = DateFunctions().YYYYmmDDtoYYYYmm(formattedExpense.paymentDate)
                    val existDate =
                        currentList.any { DateFunctions().YYYYmmDDtoYYYYmm(FormatValuesToDatabase().expenseDate(it.paymentDate)) == removedExpenseMonth }
                    if (!existDate) {
                        val currentMonthList = dataStore.getExpenseMonths().toMutableList()
                        currentMonthList.removeAll {
                            it == DateFunctions().YYYYmmDDtoYYYYmm(formattedExpense.paymentDate)
                        }
                        dataStore.updateAndResetExpenseMonths(currentMonthList)

                    }

                    //Update info per month on dataStore
                    val currentInfoPerMonth = dataStore.getExpenseInfoPerMonth()
                    val updatedInfoPerMonth = mutableListOf<InformationPerMonthExpense>()
                    val monthInfo = currentInfoPerMonth.find { infoPerMonth ->
                        infoPerMonth.date == DateFunctions().YYYYmmDDtoYYYYmm(formattedExpense.paymentDate) }
                    if(monthInfo != null){
                        val expensePrice = BigDecimal(formattedExpense.price).setScale(8,RoundingMode.HALF_UP)
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
                        val date = DateFunctions().YYYYmmDDtoYYYYmm(formattedExpense.paymentDate)
                        val defaultBudget = BigDecimal(dataStore.getDefaultBudget()).setScale(8,RoundingMode.HALF_UP)
                        val monthExpenseUpdated = BigDecimal(formattedExpense.price).setScale(8, RoundingMode.HALF_UP).toString()
                        val availableNowUpdated = defaultBudget.subtract(BigDecimal(formattedExpense.price)).setScale(8,RoundingMode.HALF_UP).toString()
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
                    val priceFormatted = BigDecimal(formattedExpense.price).setScale(2,RoundingMode.HALF_UP)
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
        viewModelScope.async(Dispatchers.IO) {
            firebaseAPI.deleteEarning(earning).fold(
                onSuccess = {
                    dataStore.deleteFromEarningList(earning)
                    _deleteEarningResult.postValue(true)
                },
                onFailure = {
                    _deleteEarningResult.postValue(false)
                }
            )
        }
    }

    fun deleteRecurringExpense(recurringExpense : RecurringTransaction){
        viewModelScope.async(Dispatchers.IO) {
            firebaseAPI.deleteRecurringExpense(recurringExpense).fold(
                onSuccess = {
                    dataStore.deleteFromRecurringExpenseList(recurringExpense)
                    _deleteRecurringExpenseResult.postValue(true)
                },
                onFailure = {
                    _deleteRecurringExpenseResult.postValue(false)
                }
            )
        }
    }

}

