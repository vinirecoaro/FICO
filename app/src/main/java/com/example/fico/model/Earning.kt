package com.example.fico.model

import com.example.fico.utils.constants.StringConstants

data class Earning(
    var id: String,
    var value: String,
    var description: String,
    var category: String,
    var date: String,
    var inputDateTime: String,
){
    fun toTransaction() : Transaction{
        return Transaction(
            this.id,
            this.value,
            this.description,
            this.category,
            this.date,
            this.date,
            this.inputDateTime,
            "1",
            StringConstants.DATABASE.EARNING
        )
    }
}