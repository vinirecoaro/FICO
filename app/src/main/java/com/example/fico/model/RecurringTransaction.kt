package com.example.fico.model

data class RecurringTransaction(
    var id: String,
    var price: String,
    var description: String,
    var category: String,
    var day: String,
    var inputDateTime: String,
    var type: String
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
            this.type
        )
    }
}
