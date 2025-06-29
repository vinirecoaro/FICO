package com.example.fico.model

data class ImportTransactionsFromFileResponse(
    val expenseList : List<Expense>,
    val earningList : List<Earning>,
    val installmentExpenseList : List<Expense>,
    val result : Boolean,
    val message : String
)