package com.example.fico

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.fico.model.Earning
import com.example.fico.model.Expense
import com.example.fico.model.InformationPerMonthExpense
import com.example.fico.shared.constants.StringConstants
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
        val paymentDateSwitchKey = stringPreferencesKey(StringConstants.DATA_STORE.PAYMENT_DATE_SWITCH)
        val earningsListKey = stringPreferencesKey(StringConstants.DATA_STORE.EARNINGS_LIST_KEY)
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

    suspend fun setDefaultPaymentDay(day : String){
        dataStore.edit { preferences ->
            preferences[defaultPaymentDayKey] = day
        }
    }

    suspend fun getDefaultPaymentDay() : String?{
        val defaultPaymentDay = dataStore.data.map { preferences ->
            preferences[defaultPaymentDayKey]
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

    suspend fun updateEarningList(expenseList : List<Earning>){
        dataStore.edit { preferences ->
            val existingEarningsListString = preferences[earningsListKey] ?: "[]"
            val existingEarningList = Gson().fromJson(existingEarningsListString, Array<Earning>::class.java).toMutableList()
            existingEarningList.addAll(expenseList)
            val expenseListString = Gson().toJson(existingEarningList)
            preferences[earningsListKey] = expenseListString
        }
    }

    suspend fun getEarningsList() : List<Earning>{
        val earningsListString = dataStore.data.map { preferences ->
            preferences[earningsListKey]
        }.first() ?: return emptyList()
        return Gson().fromJson(earningsListString, object : TypeToken<List<Earning>>() {}.type)
    }

}