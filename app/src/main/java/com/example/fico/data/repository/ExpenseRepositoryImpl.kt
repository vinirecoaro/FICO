package com.example.fico.data.repository

import com.example.fico.data.dao.ExpenseDao
import com.example.fico.data.mapper.toDomain
import com.example.fico.data.mapper.toEntity
import com.example.fico.domain.model.ExpenseDomain
import com.example.fico.domain.repository.ExpenseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExpenseRepositoryImpl(private val dao : ExpenseDao) : ExpenseRepository{
    override suspend fun getAll(): List<ExpenseDomain> = withContext(Dispatchers.IO){
        dao.getAll().map {
            it.toDomain()
        }
    }

    override suspend fun insert(expense: ExpenseDomain) = withContext(Dispatchers.IO){
        dao.insert(expense.toEntity())
    }
}