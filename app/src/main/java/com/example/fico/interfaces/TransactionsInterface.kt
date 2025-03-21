package com.example.fico.interfaces

import com.example.fico.model.Expense
import com.example.fico.model.InformationPerMonthExpense

interface TransactionsInterface {
    suspend fun getExpenseList() : Result<List<Expense>>
    suspend fun getExpenseMonths() : Result<List<String>>
    suspend fun getExpenseInfoPerMonth() : Result<List<InformationPerMonthExpense>>
}