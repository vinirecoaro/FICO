package com.example.fico.service.constants

class AppConstants private constructor() {

    object DATABASE {
        const val USERS = "users"
        const val EXPENSES_LIST = "expensesList"
        const val TOTAL_EXPENSE = "totalExpense"
        const val PRICE = "price"
        const val DATE = "date"
        const val DESCRIPTION = "description"
        const val CATEGORY = "category"
        const val INFORMATION_PER_MONTH = "informationPerMonth"
        const val AVAILABLE_NOW = "availableNow"
        const val EXPENSE = "expense"
        const val BUDGET = "budget"
        const val DEFAULT_BUDGET = "DefaultBudget"
        const val DEFAULT_VALUES = "DefaultValues"
        const val USER_INFO = "userInfo"
        const val NAME = "name"
    }

    object CONFIGURATION_LIST {
        const val DADOS_PESSOAIS = "Dados pessoais"
        const val BUDGET = "Orçamento"
        const val LOGOUT = "Sair"
        object BUDGET_LIST{
            const val DEFAULT_BUDGET = "Orçamento padrão"
            const val BUDGET_PER_MONTH = "Orçamento por mês"
        }
    }

}