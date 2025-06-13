package com.example.fico.interfaces

import com.example.fico.model.CreditCard
import com.example.fico.model.User

interface CreditCardInterface {
    suspend fun addCreditCard(creditCard : CreditCard) : Result<CreditCard>

    suspend fun getCreditCardList() : Result<List<CreditCard>>

    suspend fun editCreditCard(creditCard : CreditCard): Result<CreditCard>

    suspend fun deleteCreditCard(creditCard : CreditCard): Result<Unit>

    suspend fun setCreditCardAsDefault(creditCardId : String): Result<Unit>

    suspend fun getDefaultCreditCard(): Result<String>
}