package com.example.fico

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.fico.api.FormatValuesFromDatabase
import com.example.fico.api.FormatValuesToDatabase
import com.example.fico.model.CreditCard
import com.example.fico.model.Earning
import com.example.fico.model.Expense
import com.example.fico.model.InformationPerMonthExpense
import com.example.fico.model.RecurringTransaction
import com.example.fico.model.Transaction
import com.example.fico.model.ValuePerMonth
import com.example.fico.utils.DateFunctions
import com.example.fico.utils.constants.StringConstants
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.math.BigDecimal

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
        val earningMonthInfoListKey = stringPreferencesKey(StringConstants.DATA_STORE.EARNING_MONTHS_LIST_KEY)
        val userNameKey = stringPreferencesKey(StringConstants.DATA_STORE.USER_NAME_KEY)
        val userEmailKey = stringPreferencesKey(StringConstants.DATA_STORE.USER_EMAIL_KEY)
        val blockAppStateKey = stringPreferencesKey(StringConstants.DATA_STORE.BLOCK_APP_STATE_KEY)
        val firebaseDatabaseFixingVersionKey = stringPreferencesKey(StringConstants.DATA_STORE.FIREBASE_DATABASE_FIXING_VERSION_KEY)
        val creditCardListKey = stringPreferencesKey(StringConstants.DATA_STORE.CREDIT_CARD_LIST_KEY)
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

    suspend fun setBlockAppState(state : Boolean){
        dataStore.edit { preferences ->
            preferences[blockAppStateKey] = state.toString()
        }
    }

    suspend fun getBlockAppState() : Boolean {
        val blockAppState = dataStore.data.map { preferences ->
            preferences[blockAppStateKey]?.toBoolean() ?: true
        }.first()
        return blockAppState
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

    suspend fun deleteFromRecurringTransactionList(recurringTransaction : RecurringTransaction){
        dataStore.edit { preferences ->
            val existingRecurringTransactionListString = preferences[recurringTransactionsListKey] ?: "[]"
            val existingRecurringTransactionList = Gson().fromJson(existingRecurringTransactionListString, Array<RecurringTransaction>::class.java).toMutableList()
            if(existingRecurringTransactionList.find { it.id == recurringTransaction.id } != null){
                existingRecurringTransactionList.removeAll { it.id == recurringTransaction.id }
            }
            val recurringTransactionListString = Gson().toJson(existingRecurringTransactionList)
            preferences[recurringTransactionsListKey] = recurringTransactionListString
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

    suspend fun updateAndResetEarningMonthInfoList(earningList : List<Earning>){
        val earningMonthInfoList = calculateEarningMonthInfoList(earningList)
        val earningMonthsListString = Gson().toJson(earningMonthInfoList)
        dataStore.edit { preferences ->
            preferences[earningMonthInfoListKey] = earningMonthsListString
        }
    }

    private fun calculateEarningMonthInfoList(earningList : List<Earning>) : List<ValuePerMonth> {
        val earningMonthsList = mutableListOf<ValuePerMonth>()
        for(earning in earningList){
            val month = DateFunctions().YYYYmmDDtoYYYYmm(earning.date)
            val existMonth = earningMonthsList.find { it.month == month }
            if(existMonth != null){
                existMonth.value = BigDecimal(existMonth.value).add(BigDecimal(earning.value)).toString()
            }else{
                earningMonthsList.add(ValuePerMonth(month, earning.value))
            }
        }
        return earningMonthsList
    }

    suspend fun getEarningMonthInfoList() : List<ValuePerMonth>{
        val earningMonthInfoListString = dataStore.data.map { preferences ->
            preferences[earningMonthInfoListKey]
        }.first() ?: return emptyList()
        return Gson().fromJson(earningMonthInfoListString, object : TypeToken<List<ValuePerMonth>>() {}.type)
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

    suspend fun updateUserName(name : String){
        dataStore.edit {preferences ->
            preferences[userNameKey] = name
        }
    }

    suspend fun getUserName() : String {
        val userName = dataStore.data.map { preferences ->
            preferences[userNameKey]
        }.first()
        return userName ?: ""
    }

    suspend fun updateUserEmail(email : String){
        dataStore.edit {preferences ->
            preferences[userEmailKey] = email
        }
    }

    suspend fun getUserEmail() : String {
        val userEmail = dataStore.data.map { preferences ->
            preferences[userEmailKey]
        }.first()
        return userEmail ?: ""
    }

    suspend fun setFirebaseDatabaseFixingVersion(version : String){
        dataStore.edit {preferences ->
            preferences[firebaseDatabaseFixingVersionKey] = version
        }
    }

    suspend fun getFirebaseDatabaseFixingVersion() : String {
        val firebaseDatabaseFixingVersion = dataStore.data.map { preferences ->
            preferences[firebaseDatabaseFixingVersionKey]
        }.first()
        return firebaseDatabaseFixingVersion ?: StringConstants.VERSION.V0
    }

    suspend fun updateCreditCardList(creditCard : CreditCard){
        dataStore.edit { preferences ->
            val currentCreditCardListString = preferences[creditCardListKey] ?: "[]"
            val currentCreditCardList = Gson().fromJson(currentCreditCardListString, Array<CreditCard>::class.java).toMutableList()
            if(currentCreditCardList.find { it.id == creditCard.id } != null){
                currentCreditCardList.removeAll { it.id == creditCard.id }
                currentCreditCardList.add(creditCard)
            }else{
                currentCreditCardList.add(creditCard)
            }
            val creditCardListString = Gson().toJson(currentCreditCardList)

            preferences[creditCardListKey] = creditCardListString
        }
    }

    suspend fun updateAndResetCreditCardList(creditCardList : List<CreditCard>){
        val creditCardListString = Gson().toJson(creditCardList)
        dataStore.edit { preferences ->
            preferences[creditCardListKey] = creditCardListString
        }
    }

    suspend fun getCreditCardList() : List<CreditCard>{
        val creditCardListString = dataStore.data.map { preferences ->
            preferences[creditCardListKey]
        }.first() ?: return emptyList()
        return Gson().fromJson(creditCardListString, object : TypeToken<List<CreditCard>>() {}.type)
    }

    suspend fun deleteFromCreditCardList(creditCard : CreditCard){
        dataStore.edit { preferences ->
            val existingCreditCardListString = preferences[creditCardListKey] ?: "[]"
            val existingCreditCardList = Gson().fromJson(existingCreditCardListString, Array<CreditCard>::class.java).toMutableList()
            if(existingCreditCardList.find { it.id == creditCard.id } != null){
                existingCreditCardList.removeAll { it.id == creditCard.id }
            }
            val creditCardListString = Gson().toJson(existingCreditCardList)
            preferences[creditCardListKey] = creditCardListString
        }
    }
}