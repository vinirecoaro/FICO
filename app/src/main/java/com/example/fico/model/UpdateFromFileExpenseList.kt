package com.example.fico.model

data class UpdateFromFileExpenseList(
    val expenseList: MutableList<Expense>,
    var updatedTotalExpense: String,
    val updatedInformationPerMonth: MutableList<InformationPerMonthExpense>
) {
}