package com.example.fico.repositories

import com.example.fico.interfaces.TransactionsInterface
import com.example.fico.model.Expense

class TransactionsRepository(private val transactionsInterface : TransactionsInterface) {
    suspend fun getExpenseList() : Result<List<Expense>> {
        return transactionsInterface.getExpenseList()
    }

    suspend fun getExpenseMonths() : Result<List<String>>{
        return transactionsInterface.getExpenseMonths()
    }
}