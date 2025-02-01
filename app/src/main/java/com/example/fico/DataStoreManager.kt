package com.example.fico

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.fico.api.FormatValuesFromDatabase
import com.example.fico.api.FormatValuesToDatabase
import com.example.fico.model.Earning
import com.example.fico.model.Expense
import com.example.fico.model.InformationPerMonthExpense
import com.example.fico.model.RecurringTransaction
import com.example.fico.model.Transaction
import com.example.fico.utils.constants.StringConstants
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DataStoreManager (context: Context) {
    private val Context.dataStore : DataStore<Preferences> by preferencesDataStore(StringConstants.DATA_STORE.NAME)
    private val dataStore = context.dataStore

    companion object {
        val expenseListKey = stringPreferencesKey(StringConstants.DATA_STORE.EXPENSE_LIST)
        val expenseMonthsKey = stringPreferencesKey(StringConstants.DATA_STORE.EXPENSE_MONTHS)
        val expenseInfoPerMonthKey = stringPreferencesKey(StringConstants.DATA_STORE.INFO_PER_MONTH)
        val totalExpenseKey = stringPreferencesKey(StringConstants.DATA_STORE.TOTAL_EXPENSE)
        val defaultBudgetKey = stringPreferencesKey(StringConstants.DATA_STORE.DEFAULT_BUDGET_KEY)
        val defaultPaymentDayKey = stringPreferencesKey(StringConstants.DATA_STORE.DEFAULT_PAYMENT_DAY_KEY)
        val daysForClosingBillKey = stringPreferencesKey(StringConstants.DATA_STORE.DAYS_FOR_CLOSING_BILL)
        val paymentDateSwitchKey = stringPreferencesKey(StringConstants.DATA_STORE.PAYMENT_DATE_SWITCH)
        val earningsListKey = stringPreferencesKey(StringConstants.DATA_STORE.EARNINGS_LIST_KEY)
        val recurringTransactionsListKey = stringPreferencesKey(StringConstants.DATA_STORE.RECURRING_TRANSACTIONS_LIST_KEY)
        val earningMonthsListKey = stringPreferencesKey(StringConstants.DATA_STORE.EARNING_MONTHS_LIST_KEY)
    }

    suspend fun updateAndResetExpenseList(expenseList : List<Expense>){
        val expenseListString = Gson().toJson(expenseList)
        dataStore.edit { preferences ->
            preferences[expenseListKey] = expenseListString
        }
    }

    suspend fun updateExpenseList(expenseList : List<Expense>){
        dataStore.edit { preferences ->
            val existingExpenseListString = preferences[expenseListKey] ?: "[]"
            val existingExpenseList = Gson().fromJson(existingExpenseListString, Array<Expense>::class.java).toMutableList()
            existingExpenseList.addAll(expenseList)
            val expenseListString = Gson().toJson(existingExpenseList)
            preferences[expenseListKey] = expenseListString
        }
    }

    suspend fun getExpenseList() : List<Expense>{
        val expenseListString = dataStore.data.map { preferences ->
            preferences[expenseListKey]
        }.first() ?: return emptyList()
        return Gson().fromJson(expenseListString, object : TypeToken<List<Expense>>() {}.type)
    }

    suspend fun updateExpenseMonths(newExpenseMonths : List<String>){
        dataStore.edit { preferences ->
            val existingExpenseMonthsString = preferences[expenseMonthsKey] ?: "[]"
            val existingExpenseMonths = Gson().fromJson(existingExpenseMonthsString, Array<String>::class.java).toMutableList()
            val uniqueExpenseMonths = existingExpenseMonths.toMutableSet()
            uniqueExpenseMonths.addAll(newExpenseMonths)
            val updatedExpenseMonthsString = Gson().toJson(uniqueExpenseMonths)
            preferences[expenseMonthsKey] = updatedExpenseMonthsString
        }
    }

    suspend fun updateAndResetExpenseMonths(expenseMonths : List<String>){
        dataStore.edit { preferences ->
            val updatedExpenseMonthsString = Gson().toJson(expenseMonths)
            preferences[expenseMonthsKey] = updatedExpenseMonthsString
        }
    }

    suspend fun getExpenseMonths() : List<String>{
        val expenseMonthsString = dataStore.data.map { preferences ->
            preferences[expenseMonthsKey]
        }.first() ?: return emptyList()
        return Gson().fromJson(expenseMonthsString, object : TypeToken<List<String>>() {}.type)
    }

    suspend fun updateAndResetInfoPerMonthExpense(expenseInfoPerMonthList : List<InformationPerMonthExpense>){
        val expenseInfoPerMonthListString = Gson().toJson(expenseInfoPerMonthList)
        dataStore.edit { preferences ->
            preferences[expenseInfoPerMonthKey] = expenseInfoPerMonthListString
        }
    }

    suspend fun updateInfoPerMonthExpense(infoPerMonthList : List<InformationPerMonthExpense>){
        dataStore.edit { preferences ->
            val existingInfoPerMonthExpenseString = preferences[expenseInfoPerMonthKey] ?: "[]"
            val existingInfoPerMonthExpense = Gson().fromJson(existingInfoPerMonthExpenseString, Array<InformationPerMonthExpense>::class.java).toMutableList()
            infoPerMonthList.forEach{ infoPerMonthToAdd ->
                existingInfoPerMonthExpense.removeAll {
                    it.date == infoPerMonthToAdd.date
                }
            }
            val updatedInfoPerMonthExpense = existingInfoPerMonthExpense
            updatedInfoPerMonthExpense.addAll(infoPerMonthList)
            val updatedInfoPerMonthExpenseSorted = updatedInfoPerMonthExpense.sortedBy { it.date }
            val updatedInfoPerMonthExpenseSortedString = Gson().toJson(updatedInfoPerMonthExpenseSorted)
            preferences[expenseInfoPerMonthKey] = updatedInfoPerMonthExpenseSortedString
        }
    }

    suspend fun getExpenseInfoPerMonth() : List<InformationPerMonthExpense>{
        val expenseInfoPerMonthString = dataStore.data.map { preferences ->
            preferences[expenseInfoPerMonthKey]
        }.first() ?: return emptyList()
        return Gson().fromJson(expenseInfoPerMonthString, object : TypeToken<List<InformationPerMonthExpense>>() {}.type)
    }

    suspend fun updateTotalExpense(totalExpense : String){
        dataStore.edit { preferences ->
            preferences[totalExpenseKey] = totalExpense
        }
    }

    suspend fun getTotalExpense() : String{
        val totalExpense = dataStore.data.map { preferences ->
            preferences[totalExpenseKey]
        }.first() ?: "0.00"
        return Gson().fromJson(totalExpense, object : TypeToken<String>() {}.type)
    }

    suspend fun updateDefaultBudget(budget : String){
        dataStore.edit {preferences ->
            preferences[defaultBudgetKey] = budget
        }
    }

    suspend fun getDefaultBudget() : String {
        val defaultBudget = dataStore.data.map { preferences ->
            preferences[defaultBudgetKey]
        }.first()
        return Gson().fromJson(defaultBudget, object : TypeToken<String>() {}.type)
    }

    suspend fun setDefaultPaymentDay(expirationDay : String){
        dataStore.edit { preferences ->
            preferences[defaultPaymentDayKey] = expirationDay
        }
    }

    suspend fun setDaysForClosingBill(daysForClosingBill : String){
        dataStore.edit { preferences ->
            preferences[daysForClosingBillKey] = daysForClosingBill
        }
    }

    suspend fun getDefaultPaymentDay() : String?{
        val defaultPaymentDay = dataStore.data.map { preferences ->
            preferences[defaultPaymentDayKey]
        }.first()
        return Gson().fromJson(defaultPaymentDay, object : TypeToken<String?>() {}.type)
    }

    suspend fun getDaysForClosingBill() : String?{
        val defaultPaymentDay = dataStore.data.map { preferences ->
            preferences[daysForClosingBillKey]
        }.first()
        return Gson().fromJson(defaultPaymentDay, object : TypeToken<String?>() {}.type)
    }

    suspend fun setPaymentDateSwitchInitialState(active : Boolean){
        dataStore.edit { preferences ->
            preferences[paymentDateSwitchKey] = active.toString()
        }
    }

    suspend fun getPaymentDateSwitchInitialState() : Boolean {
        val paymentDateSwitch = dataStore.data.map { preferences ->
            preferences[paymentDateSwitchKey]
        }.first().toBoolean()
        return paymentDateSwitch
    }

    suspend fun updateAndResetEarningList(earningList : List<Earning>){
        val earningListString = Gson().toJson(earningList)
        dataStore.edit { preferences ->
            preferences[earningsListKey] = earningListString
        }
    }

    suspend fun updateEarningList(earning : Earning){
        dataStore.edit { preferences ->
            val existingEarningsListString = preferences[earningsListKey] ?: "[]"
            val existingEarningList = Gson().fromJson(existingEarningsListString, Array<Earning>::class.java).toMutableList()
            if(existingEarningList.find { it.id == earning.id } != null){
                existingEarningList.removeAll { it.id == earning.id }
                existingEarningList.add(earning)
            }else{
                existingEarningList.add(earning)
            }
            val earningListString = Gson().toJson(existingEarningList)
            preferences[earningsListKey] = earningListString
        }
    }

    suspend fun updateRecurringExpenseList(recurringExpense : RecurringTransaction){
        dataStore.edit { preferences ->
            val existingRecurringExpenseListString = preferences[recurringTransactionsListKey] ?: "[]"
            val existingRecurringExpenseList = Gson().fromJson(existingRecurringExpenseListString, Array<RecurringTransaction>::class.java).toMutableList()
            if(existingRecurringExpenseList.find { it.id == recurringExpense.id } != null){
                existingRecurringExpenseList.removeAll { it.id == recurringExpense.id }
                existingRecurringExpenseList.add(recurringExpense)
            }else{
                existingRecurringExpenseList.add(recurringExpense)
            }
            val recurringExpenseListString = Gson().toJson(existingRecurringExpenseList)
            preferences[recurringTransactionsListKey] = recurringExpenseListString
        }
    }

    suspend fun deleteFromEarningList(earning : Earning){
        dataStore.edit { preferences ->
            val existingEarningsListString = preferences[earningsListKey] ?: "[]"
            val existingEarningList = Gson().fromJson(existingEarningsListString, Array<Earning>::class.java).toMutableList()
            if(existingEarningList.find { it.id == earning.id } != null){
                existingEarningList.removeAll { it.id == earning.id }
            }
            val earningListString = Gson().toJson(existingEarningList)
            preferences[earningsListKey] = earningListString
        }
    }

    suspend fun deleteFromRecurringExpenseList(recurringExpense : RecurringTransaction){
        dataStore.edit { preferences ->
            val existingRecurringExpenseListString = preferences[recurringTransactionsListKey] ?: "[]"
            val existingRecurringExpenseList = Gson().fromJson(existingRecurringExpenseListString, Array<RecurringTransaction>::class.java).toMutableList()
            if(existingRecurringExpenseList.find { it.id == recurringExpense.id } != null){
                existingRecurringExpenseList.removeAll { it.id == recurringExpense.id }
            }
            val earningListString = Gson().toJson(existingRecurringExpenseList)
            preferences[recurringTransactionsListKey] = earningListString
        }
    }

    suspend fun updateRecurringExpensesList(recurringExpenseList : List<RecurringTransaction>){
        dataStore.edit { preferences ->
            val existingRecurringExpensesListString = preferences[recurringTransactionsListKey] ?: "[]"
            val existingRecurringExpensesList = Gson().fromJson(existingRecurringExpensesListString, Array<RecurringTransaction>::class.java).toMutableList()
            existingRecurringExpensesList.addAll(recurringExpenseList)
            val recurringExpensesListString = Gson().toJson(existingRecurringExpensesList)
            preferences[recurringTransactionsListKey] = recurringExpensesListString
        }
    }

    suspend fun updateAndResetRecurringExpensesList(recurringExpenseList : List<RecurringTransaction>){
        val recurringExpensesListString = Gson().toJson(recurringExpenseList)
        dataStore.edit { preferences ->
            preferences[recurringTransactionsListKey] = recurringExpensesListString
        }
    }

    suspend fun getRecurringTransactionsList() : List<RecurringTransaction>{
        val recurringExpensesListString = dataStore.data.map { preferences ->
            preferences[recurringTransactionsListKey]
        }.first() ?: return emptyList()
        return Gson().fromJson(recurringExpensesListString, object : TypeToken<List<RecurringTransaction>>() {}.type)
    }

    suspend fun getEarningsList() : List<Earning>{
        val earningsListString = dataStore.data.map { preferences ->
            preferences[earningsListKey]
        }.first() ?: return emptyList()
        return Gson().fromJson(earningsListString, object : TypeToken<List<Earning>>() {}.type)
    }

    suspend fun updateAndResetEarningMonths(earningsMonths : List<String>){
        val earningMonthsListString = Gson().toJson(earningsMonths)
        dataStore.edit { preferences ->
            preferences[earningMonthsListKey] = earningMonthsListString
        }
    }

    suspend fun getEarningMonths() : List<String>{
        val earningMonthsString = dataStore.data.map { preferences ->
            preferences[earningMonthsListKey]
        }.first() ?: return emptyList()
        return Gson().fromJson(earningMonthsString, object : TypeToken<List<String>>() {}.type)
    }

    suspend fun getTransactionList() : List<Transaction>{
        val earningList = getEarningsList()
        val expenseList = getExpenseList()
        val transactionList = mutableListOf<Transaction>()

        earningList.forEach { earning ->
            earning.date = FormatValuesFromDatabase().date(earning.date)
            transactionList.add(earning.toTransaction())
        }

        expenseList.forEach { expense ->
            transactionList.add(expense.toTransaction())
        }

        var sortedList = listOf<Transaction>()

        sortedList = transactionList.sortedByDescending { FormatValuesToDatabase().expenseDate(it.paymentDate) }

        return sortedList
    }

    suspend fun getTransaction(transaction: Transaction) : Transaction{
        when (transaction.type) {
            StringConstants.DATABASE.EXPENSE -> {
                val commonId = if(transaction.id.length > 25){
                    transaction.id.substring(0,25)
                }else{
                    transaction.id
                }
                val expenseList = getExpenseList()
                val updatedTransaction = expenseList.first {
                    val listItemId =
                        if(it.id.length > 25){
                            it.id.substring(0,25)
                        }else{
                            it.id
                        }
                    listItemId == commonId }.toTransaction()
                return updatedTransaction
            }
            StringConstants.DATABASE.EARNING -> {
                val earningList = getEarningsList()
                val updatedTransaction = earningList.first { it.id == transaction.id }.toTransaction()
                val formattedDate = FormatValuesFromDatabase().date(updatedTransaction.paymentDate)
                updatedTransaction.purchaseDate = formattedDate
                updatedTransaction.paymentDate = formattedDate
                return updatedTransaction
            }
            else -> {
                return Transaction.empty()
            }
        }
    }

    suspend fun getInstallmentExpense(transaction: Transaction) : List<Transaction>{
        val commonId = transaction.id.substring(0,25)
        val expenseList = getExpenseList()
        val updatedExpenseList = expenseList.filter {
            val listItemCommonId = it.id.substring(0,25)
            listItemCommonId == commonId }
        val updatedTransactionList = mutableListOf<Transaction>()
        updatedExpenseList.forEach { updatedTransactionList.add(it.toTransaction()) }

        return updatedTransactionList
    }


}