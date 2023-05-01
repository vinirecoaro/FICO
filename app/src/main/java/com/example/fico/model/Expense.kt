package com.example.fico.model

data class Expense(
    private val value : Float,
    private val description : String,
    private val category : String,
    private val date : String
)
