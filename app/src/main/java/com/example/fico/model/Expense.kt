package com.example.fico.model

data class Expense(
    internal val id: String,
    internal val price : String,
    internal val description : String,
    internal var category : String,
    internal val date : String
)
