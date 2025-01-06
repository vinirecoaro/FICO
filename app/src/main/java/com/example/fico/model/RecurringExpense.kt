package com.example.fico.model

data class RecurringExpense(
    var id: String,
    var price: String,
    var description: String,
    var category: String,
    var day: String,
    var inputDateTime: String,
)
