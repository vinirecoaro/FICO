package com.example.fico.model

import android.os.Parcelable
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
}
