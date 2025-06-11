package com.example.fico.model

import java.io.Serializable

data class CreditCardColors(
    val backgroundColorNameRes: Int,
    val backgroundColor: Int,
    val textColor: Int
) : Serializable