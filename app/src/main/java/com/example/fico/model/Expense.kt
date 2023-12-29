package com.example.fico.model
import android.os.Parcel
import android.os.Parcelable

data class Expense(
    val id: String,
    val price: String,
    val description: String,
    val category: String,
    val date: String,
    val nOfInstallment: String = "1"
) : Parcelable {
    constructor(parcel: Parcel) : this(
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
        parcel.writeString(date)
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
}
