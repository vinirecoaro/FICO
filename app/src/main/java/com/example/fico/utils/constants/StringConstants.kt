package com.example.fico.utils.constants

class StringConstants private constructor() {

    object DATABASE {
        const val USERS = "users"
        const val EXPENSES_LIST = "expensesList"
        const val RECURRING_TRANSACTIONS_LIST = "recurringTransactionsList"
        const val TOTAL_EXPENSE = "totalExpense"
        const val PRICE = "price"
        const val TYPE = "type"
        const val PAYMENT_DATE = "payment_date"
        const val PAYMENT_DAY = "payment_day"
        const val DAYS_FOR_CLOSING_BILL = "days_for_closing_bill"
        const val PURCHASE_DATE = "purchase_date"
        const val INPUT_DATE_TIME = "input_date_time"
        const val DATE = "date"
        const val DAY = "day"
        const val DESCRIPTION = "description"
        const val CATEGORY = "category"
        const val INFORMATION_PER_MONTH = "informationPerMonth"
        const val AVAILABLE_NOW = "availableNow"
        const val EXPENSE = "expense"
        const val EXPENSES = "expenses"
        const val RECURRING_EXPENSE = "recurring_expense"
        const val RECURRING_EARNING = "recurring_earning"
        const val BUDGET = "budget"
        const val DEFAULT_BUDGET = "DefaultBudget"
        const val DEFAULT_VALUES = "DefaultValues"
        const val USER_INFO = "userInfo"
        const val NAME = "name"
        const val EARNINGS = "earnings"
        const val EARNING = "earning"
        const val TRANSACTION = "transaction"
        const val TRANSACTIONS = "transactions"
        const val EARNINGS_LIST = "earningsList"
        const val VALUE = "value"
    }

    object GENERAL_CONFIGURATION_LIST {
        const val PERSONAL_DATA = "Dados pessoais"
        const val LOGOUT = "Sair"
    }

    object XLS {
        val EXPENSE_TITLES: Array<String> = arrayOf("Preço", "Descrição", "Categoria", "Data de Pagamento", "Data de Compra")
        val EXPENSE_INDEX_NAME: Array<String> = arrayOf("price", "description", "category", "paymentDate", "purchaseDate")
        val EARNINGS_TITLES: Array<String> = arrayOf("Valor", "Descrição", "Categoria", "Data")
        val EARNINGS_INDEX_NAME: Array<String> = arrayOf("value", "description", "category", "date")
        const val SHEET_NAME_EXPENSES = "despesas"
        const val SHEET_NAME_EARNINGS = "receitas"
        const val FILE_NAME = "transactions"
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
            const val SALE_OF_SHARES = "category_icon_sale_of_shares"
            const val SALARY = "category_icon_salary"
            const val CASHBACK = "category_icon_cashback"
            const val FREELANCE = "category_icon_freelance"
            const val OTHER = "category_icon_other"
            const val DIVIDEND = "category_icon_dividend"
            const val BENEFITS = "category_icon_benefits"
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
        const val DEFAULT_PAYMENT_DAY_KEY = "DefaultPaymentDayKey"
        const val DAYS_FOR_CLOSING_BILL = "days_for_closing_bill"
        const val PAYMENT_DATE_SWITCH = "PaymentDateSwitch"
        const val EARNINGS_LIST_KEY = "EarningsListKey"
        const val RECURRING_TRANSACTIONS_LIST_KEY = "RecurringTransactionsListKey"
        const val EARNING_MONTHS_LIST_KEY = "EarningMonthsListKey"
        const val USER_NAME_KEY = "UserNameKey"
        const val USER_EMAIL_KEY = "UserEmailKey"

    }

    object REQUEST_CODES {
        const val TRANSACTION_LIST_TO_EDIT_TRANSACTION = 1111
    }

    object RESULT_CODES {
        const val BACK_BUTTON_PRESSED = 111111
        const val DELETE_INSTALLMENT_EXPENSE_RESULT_OK = 111112
        const val DELETE_INSTALLMENT_EXPENSE_RESULT_FAILURE = 111113
        const val EDIT_EARNING_EXPENSE_RESULT_OK = 111114
        const val EDIT_EARNING_EXPENSE_RESULT_FAILURE = 111115
        const val EDIT_RECURRING_TRANSACTION_OK = 111116
        const val EDIT_RECURRING_TRANSACTION_FAILURE = 111117
        const val DELETE_EXPENSE_RESULT_OK = 111118
        const val DELETE_EXPENSE_RESULT_FAILURE = 111119
        const val DELETE_EARNING_RESULT_OK = 111120
        const val DELETE_EARNING_RESULT_FAILURE = 111121
        const val DELETE_RECURRING_TRANSACTION_RESULT_OK = 111122
        const val DELETE_RECURRING_TRANSACTION_RESULT_FAILURE = 111123
    }

    object ADD_TRANSACTION {
        const val ADD_EARNING = "add_earning"
        const val ADD_EXPENSE = "add_expense"
        const val ADD_RECURRING_EXPENSE = "recurring_expense"
        const val ADD_RECURRING_EARNING = "recurring_earning"
    }

    object TRANSACTION_LIST {
        const val TRANSACTION = "transaction"
    }

    object OPERATIONS {
        const val DELETE = "delete"
        const val UNDO_DELETE = "undo_delete"
        const val UPDATE = "update"
        const val SWIPPED_INSTALLMENT_EXPENSE = "swipped_installment_expense"
        const val CLEAR_MONTH_FILTER = "clear_month_filter"
        const val NO_OPERATION = ""
    }

    object USER_DATA_ACTIVITY{
        const val PROFILE_IMAGE_FILE_NAME = "profile_image.jpg"
    }

    object MESSAGES {
        const val USER_NOT_FOUND = "user_not_found"
        const val INVALID_CREDENTIALS = "invalid_credentials"
        const val LOGIN_ERROR = "login_error"
    }

}