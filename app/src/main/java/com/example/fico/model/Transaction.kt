package com.example.fico.model

import java.io.Serializable

data class Transaction(
    var id: String,
    var price: String,
    var description: String,
    var category: String,
    var paymentDate: String,
    var purchaseDate: String,
    var inputDateTime: String,
    var nOfInstallment: String = "1",
    var type: String
) : Serializable {

    companion object {
        fun empty() = Transaction("","","","","","","","","")
    }

    fun toExpense() : Expense{
        return Expense(
            this.id,
            this.price,
            this.description,
            this.category,
            this.paymentDate,
            this.purchaseDate,
            this.inputDateTime,
            this.nOfInstallment
        )
    }

    fun toEarning() : Earning{
        return Earning(
            this.id,
            this.price,
            this.description,
            this.category,
            this.paymentDate,
            this.inputDateTime,
        )
    }

    fun toRecurringExpense() : RecurringTransaction{
        return RecurringTransaction(
            this.id,
            this.price,
            this.description,
            this.category,
            this.paymentDate,
            this.inputDateTime,
            this.type
        )
    }
}
