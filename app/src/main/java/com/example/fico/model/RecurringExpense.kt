package com.example.fico.model

import com.example.fico.utils.constants.StringConstants

data class RecurringExpense(
    var id: String,
    var price: String,
    var description: String,
    var category: String,
    var day: String,
    var inputDateTime: String,
){
    fun toTransaction() : Transaction{
        return Transaction(
            this.id,
            this.price,
            this.description,
            this.category,
            this.day,
            this.day,
            this.inputDateTime,
            "1",
            StringConstants.DATABASE.RECURRING_EXPENSE
        )
    }
}
