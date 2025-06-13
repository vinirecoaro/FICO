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
    suspend fun editCreditCard(creditCard : CreditCard): Result<CreditCard> {
        return creditCardInterface.editCreditCard(creditCard)
    }
    suspend fun deleteCreditCard(creditCard : CreditCard): Result<Unit> {
        return creditCardInterface.deleteCreditCard(creditCard)
    }
    suspend fun setCreditCardAsDefault(creditCardId : String): Result<Unit> {
        return creditCardInterface.setCreditCardAsDefault(creditCardId)
    }
    suspend fun getDefaultCreditCard(): Result<String> {
        return creditCardInterface.getDefaultCreditCard()
    }
}