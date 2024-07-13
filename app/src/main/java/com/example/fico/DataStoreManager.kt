package com.example.fico

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.fico.model.Expense
import com.example.fico.model.InformationPerMonthExpense
import com.example.fico.util.constants.AppConstants
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DataStoreManager (context: Context) {
    private val Context.dataStore : DataStore<Preferences> by preferencesDataStore(AppConstants.DATA_STORE.NAME)
    private val dataStore = context.dataStore

    companion object {
        val expenseListKey = stringPreferencesKey(AppConstants.DATA_STORE.EXPENSE_LIST)
        val expenseMonthsKey = stringPreferencesKey(AppConstants.DATA_STORE.EXPENSE_MONTHS)
        val expenseInfoPerMonthKey = stringPreferencesKey(AppConstants.DATA_STORE.INFO_PER_MONTH)
        val totalExpenseKey = stringPreferencesKey(AppConstants.DATA_STORE.TOTAL_EXPENSE)
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
        }.first()
        return Gson().fromJson(totalExpense, object : TypeToken<String>() {}.type)
    }

}