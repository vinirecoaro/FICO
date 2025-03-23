package com.example.fico.repositories

import com.example.fico.interfaces.TransactionsInterface
import com.example.fico.model.Expense
import com.example.fico.model.InformationPerMonthExpense

class TransactionsRepository(private val transactionsInterface : TransactionsInterface) {
    suspend fun getExpenseList() : Result<List<Expense>> {
        return transactionsInterface.getExpenseList()
    }

    suspend fun getExpenseMonths() : Result<List<String>>{
        return transactionsInterface.getExpenseMonths()
    }

    suspend fun getExpenseInfoPerMonth() : Result<List<InformationPerMonthExpense>>{
        return transactionsInterface.getExpenseInfoPerMonth()
    }

    suspend fun getTotalExpense() : Result<String> {
        return transactionsInterface.getTotalExpense()
    }

}