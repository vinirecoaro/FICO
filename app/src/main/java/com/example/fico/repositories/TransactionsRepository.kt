package com.example.fico.repositories

import com.example.fico.interfaces.TransactionsInterface
import com.example.fico.model.Earning
import com.example.fico.model.Expense
import com.example.fico.model.InformationPerMonthExpense
import com.example.fico.model.RecurringTransaction

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

    suspend fun getDefaultBudget() : Result<String> {
        return transactionsInterface.getDefaultBudget()
    }

    suspend fun getEarningList() : Result<List<Earning>>{
        return transactionsInterface.getEarningList()
    }

    suspend fun getRecurringExpensesList() : Result<List<RecurringTransaction>> {
        return transactionsInterface.getRecurringExpensesList()
    }



}