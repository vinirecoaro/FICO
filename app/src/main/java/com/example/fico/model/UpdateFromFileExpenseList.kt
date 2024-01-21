package com.example.fico.model

data class UpdateFromFileExpenseList(
    val expenseList: MutableList<Pair<Expense, String>>,
    val nOfInstallments: Int,
    var updatedTotalExpense: String,
    val updatedInformationPerMonth: MutableList<InformationPerMonthExpense>
) {
}