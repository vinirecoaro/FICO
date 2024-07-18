package com.example.fico.util.constants

class AppConstants private constructor() {

    object DATABASE {
        const val USERS = "users"
        const val EXPENSES_LIST = "expensesList"
        const val TOTAL_EXPENSE = "totalExpense"
        const val PRICE = "price"
        const val PAYMENT_DATE = "payment_date"
        const val PAYMENT_DAY = "payment_day"
        const val PURCHASE_DATE = "purchase_date"
        const val INPUT_DATE_TIME = "input_date_time"
        const val DATE = "date"
        const val DESCRIPTION = "description"
        const val CATEGORY = "category"
        const val INFORMATION_PER_MONTH = "informationPerMonth"
        const val AVAILABLE_NOW = "availableNow"
        const val EXPENSE = "expense"
        const val EXPENSES = "expenses"
        const val BUDGET = "budget"
        const val DEFAULT_BUDGET = "DefaultBudget"
        const val DEFAULT_VALUES = "DefaultValues"
        const val USER_INFO = "userInfo"
        const val NAME = "name"
    }

    object EXPENSE_CONFIGURATION_LIST {
        const val BUDGET = "Orçamento"
        const val DEFAULT_PAYMENT_DATE = "Data de Pagamento Padrão"

        object BUDGET_LIST {
            const val DEFAULT_BUDGET = "Orçamento padrão"
            const val BUDGET_PER_MONTH = "Orçamento por mês"
        }
    }

    object GENERAL_CONFIGURATION_LIST {
        const val PERSONAL_DATA = "Dados pessoais"
        const val LOGOUT = "Sair"
    }

    object XLS {
        val TITLES: Array<String> = arrayOf("Preço", "Descrição", "Categoria", "Data")
        val INDEX_NAME: Array<String> = arrayOf("price", "description", "category", "date")
        const val SHEET_NAME = "Gastos"
        const val FILE_NAME = "expenses"
    }

    object FILE_PROVIDER {
        const val AUTHORITY = "com.example.fico.fileprovider"
    }

    object UPLOAD_FILE_SERVICE {
        const val SUCCESS_UPLOAD = "SUCCESS_UPLOAD"
    }

    object CATEGORY_LIST {
        object DESCRIPTION {
            const val FOOD = "Comida"
            const val TRANSPORT = "Transporte"
            const val ENTERTAINMENT = "Entretenimento"
            const val MARKET = "Mercado"
            const val EDUCATION = "Educação"
            const val GIFT = "Presente"
            const val HEALTHY = "Saúde"
            const val GAMES = "Jogos"
            const val INVESTMENT = "Investimento"
            const val ELETRONICS = "Eletrônicos"
            const val REPAIR = "Conserto"
            const val ACESSORIES = "Acessórios"
            const val CLOTHING = "Roupa"
            const val CAR = "Carro"
            const val MOTORCYCLE = "Moto"
            const val TRIP = "Viagem"
            const val HOUSE = "Casa"
            const val DONATION = "Doação"
            const val BET = "Aposta"
            const val PETS = "Pets"
            const val FEES = "Juros"
            const val GYM = "Academia"
            const val CELLPHONE = "Celular"
            const val PERSONAL_HYGIENE = "Higiene Pessoal"
            const val PHARMACY = "Farmácia"
            const val CASH_WITHDRAWAL = "Saque de Dinheiro"
            const val RIDE = "Passeio"
            const val PAYMENT = "Pagamento"
            const val SERVICES = "Serviços"
        }

        object ICON_NAME {
            const val FOOD = "category_icon_food"
            const val TRANSPORT = "category_icon_transport"
            const val ENTERTAINMENT = "category_icon_entertainment"
            const val MARKET = "category_icon_market"
            const val EDUCATION = "category_icon_education"
            const val GIFT = "category_icon_gift"
            const val HEALTHY = "category_icon_healthy"
            const val GAMES = "category_icon_games"
            const val INVESTMENT = "category_icon_investment"
            const val ELETRONICS = "category_icon_eletronics"
            const val REPAIR = "category_icon_repair"
            const val ACESSORIES = "category_icon_acessories"
            const val CLOTHING = "category_icon_clothing"
            const val CAR = "category_icon_car"
            const val MOTORCYCLE = "category_icon_motorcycle"
            const val TRIP = "category_icon_trip"
            const val HOUSE = "category_icon_house"
            const val DONATION = "category_icon_donation"
            const val BET = "category_icon_bet"
            const val PETS = "category_icon_pets"
            const val FEES = "category_icon_fees"
            const val GYM = "category_icon_gym"
            const val CELLPHONE = "category_icon_cellphone"
            const val PERSONAL_HYGIENE = "category_icon_personal_hygiene"
            const val PHARMACY = "category_icon_pharmacy"
            const val CASH_WITHDRAWAL = "category_icon_cash_withdrawal"
            const val RIDE = "category_icon_ride"
            const val PAYMENT = "category_icon_payment"
            const val SERVICES = "category_icon_services"
        }
    }

    object DEFAULT_MESSAGES {
        const val FAIL = "fail"
    }

    object SHARED_PREFERENCES {
        const val NAME = "FicoSharedPref"
    }

    object DATA_STORE {
        const val NAME = "FicoDataStore"
        const val EXPENSE_LIST = "ExpenseList"
        const val EXPENSE_MONTHS = "ExpenseMonths"
        const val INFO_PER_MONTH = "ExpenseInfoPerMonth"
        const val TOTAL_EXPENSE = "TotalExpense"
        const val DEFAULT_BUDGET_KEY = "DefaultBudgetKey"
    }

    object REQUEST_CODES {
        const val EXPENSE_LIST_TO_EDIT_EXPENSE = 1111
    }

}