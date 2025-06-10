package com.example.fico.repositories

import com.example.fico.interfaces.CreditCardInterface
import com.example.fico.model.CreditCard

class CreditCardRepository(private val creditCardInterface: CreditCardInterface){
    suspend fun addCreditCard(creditCard : CreditCard): Result<CreditCard> {
        return creditCardInterface.addCreditCard(creditCard)
    }
    suspend fun getCreditCardList() : Result<List<CreditCard>>{
        return creditCardInterface.getCreditCardList()
    }
}