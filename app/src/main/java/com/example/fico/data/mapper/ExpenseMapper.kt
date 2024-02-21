package com.example.fico.data.mapper

import com.example.fico.data.model.ExpenseEntity
import com.example.fico.domain.model.ExpenseDomain

fun ExpenseDomain.toEntity() = ExpenseEntity(
    id = id,
    price = price,
    description = description,
    category = category,
    date = date,
    nOfInstallment = nOfInstallment
)

fun ExpenseEntity.toDomain() = ExpenseDomain(
    id = id,
    price = price,
    description = description,
    category = category,
    date = date,
    nOfInstallment = nOfInstallment
)
