package com.example.fico.util.constants

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

    object EXPENSE_CONFIGURATION_LIST {
        const val BUDGET = "Orçamento"
        const val CATEGORIES = "Categorias"

        object BUDGET_LIST{
            const val DEFAULT_BUDGET = "Orçamento padrão"
            const val BUDGET_PER_MONTH = "Orçamento por mês"
        }
    }

    object GENERAL_CONFIGURATION_LIST {
        const val PERSONAL_DATA = "Dados pessoais"
        const val LOGOUT = "Sair"
    }

    object XLS {
        val TITLES : Array<String> = arrayOf("Preço", "Descrição", "Categoria", "Data")
        val INDEX_NAME : Array<String> = arrayOf("price", "description", "category", "date")
        const val SHEET_NAME = "Gastos"
        const val FILE_NAME = "expenses"
    }

    object FILE_PROVIDER {
        const val AUTHORITY = "com.example.fico.fileprovider"
    }

    object UPLOAD_FILE_SERVICE {
        const val SUCCESS_UPLOAD = "SUCCESS_UPLOAD"
    }

    object OPERATIONS {
        const val SUM = "SUM"
        const val SUB = "SUB"
    }

}