package com.example.fico.domain.usecase

import com.example.fico.domain.model.ExpenseDomain
import com.example.fico.domain.repository.ExpenseRepository

class InsertExpenseUseCase constructor(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(expense : ExpenseDomain) = repository.insert(expense)
}