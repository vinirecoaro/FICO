package com.example.fico.model

import android.os.Parcel
import android.os.Parcelable
import com.example.fico.utils.constants.StringConstants

data class Earning(
    var id: String,
    var value: String,
    var description: String,
    var category: String,
    var date: String,
    var inputDateTime: String,
) : Parcelable {
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

    override fun describeContents(): Int = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(value)
        parcel.writeString(description)
        parcel.writeString(category)
        parcel.writeString(date)
        parcel.writeString(inputDateTime)
    }

    companion object CREATOR : Parcelable.Creator<Earning> {
        override fun createFromParcel(parcel: Parcel): Earning {
            return Earning(
                parcel.readString() ?: "",
                parcel.readString() ?: "",
                parcel.readString() ?: "",
                parcel.readString() ?: "",
                parcel.readString() ?: "",
                parcel.readString() ?: ""
            )
        }

        override fun newArray(size: Int): Array<Earning?> = arrayOfNulls(size)
    }
}