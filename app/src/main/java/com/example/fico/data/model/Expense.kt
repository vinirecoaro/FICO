package com.example.fico.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

typealias ExpenseEntity = Expense

@Entity
data class Expense(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "price") var price: String,
    @ColumnInfo(name = "description") var description: String,
    @ColumnInfo(name = "category") var category: String,
    @ColumnInfo(name = "date") var date: String,
    @ColumnInfo(name = "nOfInstallment") var nOfInstallment: String = "1"
)
