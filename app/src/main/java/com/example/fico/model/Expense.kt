package com.example.fico.model

import android.os.Parcel
import android.os.Parcelable
import com.example.fico.utils.constants.StringConstants

data class Expense(
    var id: String,
    var price: String,
    var description: String,
    var category: String,
    var paymentDate: String,
    var purchaseDate: String,
    var inputDateTime: String,
    var nOfInstallment: String = "1"
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(price)
        parcel.writeString(description)
        parcel.writeString(category)
        parcel.writeString(paymentDate)
        parcel.writeString(purchaseDate)
        parcel.writeString(inputDateTime)
        parcel.writeString(nOfInstallment)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Expense> {
        override fun createFromParcel(parcel: Parcel): Expense {
            return Expense(parcel)
        }

        override fun newArray(size: Int): Array<Expense?> {
            return arrayOfNulls(size)
        }
    }

    fun toTransaction() : Transaction{
        return Transaction(
            this.id,
            this.price,
            this.description,
            this.category,
            this.paymentDate,
            this.purchaseDate,
            this.inputDateTime,
            this.nOfInstallment,
            StringConstants.DATABASE.EXPENSE
        )
    }
}