package com.example.fico.interfaces

import com.example.fico.model.Expense

interface TransactionsInterface {
    suspend fun getExpenseList() : Result<List<Expense>>
    suspend fun getExpenseMonths() : Result<List<String>>
}