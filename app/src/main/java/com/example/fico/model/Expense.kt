package com.example.fico.model

data class Expense(
    internal val price : Float,
    internal val description : String,
    internal var category : String,
    internal val date : String
)
