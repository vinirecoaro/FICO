package com.example.fico.domain.model

data class UpdateFromFileExpenseList(
    val expenseList: MutableList<Expense>,
    val nOfInstallments: Int,
    var updatedTotalExpense: String,
    val updatedInformationPerMonth: MutableList<InformationPerMonthExpense>
) {
}