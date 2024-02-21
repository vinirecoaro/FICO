package com.example.fico.domain.repository

import com.example.fico.domain.model.ExpenseDomain

interface ExpenseRepository {
    suspend fun getAll() : List<ExpenseDomain>
    suspend fun insert(expense : ExpenseDomain)
}