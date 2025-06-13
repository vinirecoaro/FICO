package com.example.fico.model

import java.io.Serializable

data class CreditCard(
    var id : String = "",
    val nickName : String,
    val expirationDay : Int,
    val closingDay : Int,
    val colors : CreditCardColors
) : Serializable