package com.example.fico.interfaces

import com.example.fico.model.Earning
import com.example.fico.model.Expense
import com.example.fico.model.InformationPerMonthExpense
import com.example.fico.model.RecurringTransaction
import com.example.fico.model.UpdateTransactionFromFileInfo

interface TransactionsInterface {

    suspend fun getExpenseList() : Result<List<Expense>>

    suspend fun getExpenseMonths() : Result<List<String>>

    suspend fun getExpenseInfoPerMonth() : Result<List<InformationPerMonthExpense>>

    suspend fun getTotalExpense() : Result<String>

    suspend fun getDefaultBudget() : Result<String>

    suspend fun getEarningList() : Result<List<Earning>>

    suspend fun getRecurringExpensesList() : Result<List<RecurringTransaction>>

    suspend fun addTransactionsFromFile(transactionFromFileInfo: UpdateTransactionFromFileInfo): String?
}