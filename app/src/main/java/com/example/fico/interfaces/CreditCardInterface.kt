package com.example.fico.interfaces

import com.example.fico.model.CreditCard
import com.example.fico.model.User

interface CreditCardInterface {
    suspend fun addCreditCard(creditCard : CreditCard) : Result<CreditCard>

    suspend fun getCreditCardList() : Result<List<CreditCard>>

}