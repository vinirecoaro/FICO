package com.example.fico.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LocalExpense(
    @PrimaryKey(autoGenerate = true) val primKey : Int,
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "price") var price: String,
    @ColumnInfo(name = "description") var description: String,
    @ColumnInfo(name = "category") var category: String,
    @ColumnInfo(name = "date") var date: String,
    @ColumnInfo(name = "nOfInstallment") val nOfInstallment: String = "1"
)
