package com.example.fico

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.fico.model.Expense
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
    }

    suspend fun updateExpenseList(expenseList : List<Expense>){
        val expenseListString = Gson().toJson(expenseList)
        dataStore.edit { preferences ->
            preferences[expenseListKey] = expenseListString
        }
    }

    suspend fun getExpenseList() : List<Expense>{
        val expenseListString = dataStore.data.map { preferences ->
            preferences[expenseListKey]
        }.first()
        return Gson().fromJson(expenseListString, object : TypeToken<List<Expense>>() {}.type)
    }
}