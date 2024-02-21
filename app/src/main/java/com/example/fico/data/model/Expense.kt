package com.example.fico.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity

typealias ExpenseEntity = Expense

@Entity
data class Expense(
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "price") var price: String,
    @ColumnInfo(name = "description") var description: String,
    @ColumnInfo(name = "category") var category: String,
    @ColumnInfo(name = "date") var date: String,
    @ColumnInfo(name = "nOfInstallment") val nOfInstallment: String = "1"
)
