package com.example.fico.model

data class UpdateTransactionFromFileInfo(
    var id: String,
    val expenseList: MutableList<Expense>,
    var updatedTotalExpense: String,
    val updatedInformationPerMonth: MutableList<InformationPerMonthExpense>,
    val earningList : MutableList<Earning>,
    val expenseIdList : MutableList<String>,
    val earningIdList : MutableList<String>,
    var totalExpenseFromFile : String,
    val expensePerMonthList : MutableList<ValuePerMonth>
)