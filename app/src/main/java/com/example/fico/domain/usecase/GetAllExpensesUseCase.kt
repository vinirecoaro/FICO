package com.example.fico.domain.usecase

import com.example.fico.domain.repository.ExpenseRepository

class GetAllExpensesUseCase constructor(
    private val repository : ExpenseRepository
){

    suspend operator fun invoke() = repository.getAll()

}