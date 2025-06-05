package com.example.fico.model

data class CreditCard(
    val nickName : String,
    val expirationDay : Int,
    val closingDay : Int,
    val color : CreditCardColors
)