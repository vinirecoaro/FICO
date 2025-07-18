package com.example.fico.model

import androidx.annotation.StringRes
import androidx.annotation.DrawableRes
import com.example.fico.R

enum class TransactionsCategory(
    @StringRes val descriptionResId: Int,
    @DrawableRes val iconResId: Int
) {
    FOOD(R.string.category_list_description_food, R.drawable.category_icon_food),
    TRANSPORT(R.string.category_list_description_transport, R.drawable.category_icon_transport),
    ENTERTAINMENT_1(R.string.category_list_description_entertainment_1, R.drawable.category_icon_entertainment),
    ENTERTAINMENT_2(R.string.category_list_description_entertainment_2, R.drawable.category_icon_entertainment),
    MARKET(R.string.category_list_description_market, R.drawable.category_icon_market),
    EDUCATION_1(R.string.category_list_description_education_1, R.drawable.category_icon_education),
    EDUCATION_2(R.string.category_list_description_education_2, R.drawable.category_icon_education),
    GIFT(R.string.category_list_description_gift, R.drawable.category_icon_gift),
    HEALTHY(R.string.category_list_description_healthy, R.drawable.category_icon_healthy),
    GAMES(R.string.category_list_description_games, R.drawable.category_icon_games),
    INVESTMENT(R.string.category_list_description_investment, R.drawable.category_icon_investment),
    ELETRONICS_1(R.string.category_list_description_eletronics_1, R.drawable.category_icon_eletronics),
    ELETRONICS_2(R.string.category_list_description_eletronics_2, R.drawable.category_icon_eletronics),
    REPAIR(R.string.category_list_description_repair, R.drawable.category_icon_repair),
    ACESSORIES(R.string.category_list_description_acessories, R.drawable.category_icon_acessories),
    CLOTHING(R.string.category_list_description_clothing, R.drawable.category_icon_clothing),
    CAR(R.string.category_list_description_car, R.drawable.category_icon_car),
    MOTORCYCLE(R.string.category_list_description_motorcycle, R.drawable.category_icon_motorcycle),
    TRIP(R.string.category_list_description_trip, R.drawable.category_icon_trip),
    HOUSE(R.string.category_list_description_house, R.drawable.category_icon_house),
    DONATION(R.string.category_list_description_donation, R.drawable.category_icon_donation),
    PETS(R.string.category_list_description_pets, R.drawable.category_icon_pets),
    FEES(R.string.category_list_description_fees, R.drawable.category_icon_fees),
    GYM(R.string.category_list_description_gym, R.drawable.category_icon_gym),
    CELLPHONE(R.string.category_list_description_cellphone, R.drawable.category_icon_cellphone),
    PERSONAL_HYGIENE_1(R.string.category_list_description_personal_hygiene_1, R.drawable.category_icon_personal_hygiene),
    PERSONAL_HYGIENE_2(R.string.category_list_description_personal_hygiene_2, R.drawable.category_icon_personal_hygiene),
    PHARMACY(R.string.category_list_description_pharmacy, R.drawable.category_icon_pharmacy),
    CASH_WITHDRAWAL_1(R.string.category_list_description_cash_withdrawal_1, R.drawable.category_icon_cash_withdrawal),
    CASH_WITHDRAWAL_2(R.string.category_list_description_cash_withdrawal_2, R.drawable.category_icon_cash_withdrawal),
    RIDE(R.string.category_list_description_ride, R.drawable.category_icon_ride),
    PAYMENT(R.string.category_list_description_payment, R.drawable.category_icon_payment),
    SERVICES(R.string.category_list_description_services, R.drawable.category_icon_services),
    SALE_OF_SHARES(R.string.category_list_description_sale_of_shares, R.drawable.category_icon_sale_of_shares),
    SALARY(R.string.category_list_description_salary, R.drawable.category_icon_salary),
    CASHBACK(R.string.category_list_description_cashback, R.drawable.category_icon_cashback),
    FREELANCE(R.string.category_list_description_freelance, R.drawable.category_icon_freelance),
    OTHER(R.string.category_list_description_other, R.drawable.category_icon_other),
    DIVIDEND(R.string.category_list_description_dividend, R.drawable.category_icon_dividend),
    BENEFITS(R.string.category_list_description_benefits, R.drawable.category_icon_benefits),
    INCOME(R.string.category_list_description_income, R.drawable.category_icon_income),
    DRINK(R.string.category_list_description_drink, R.drawable.category_icon_drink),
    SHOES(R.string.category_list_description_shoes, R.drawable.category_icon_shoes),
    STREAMING(R.string.category_list_description_streaming, R.drawable.category_icon_streaming),
    SALE(R.string.category_list_description_sale, R.drawable.category_icon_sale),
    OTHERS_EXPENSE(R.string.category_list_description_others_expense, R.drawable.category_icon_others_expense);

    companion object {

        fun getExpenseCategoryList(): List<TransactionsCategory> = listOf(
            FOOD, TRANSPORT, ENTERTAINMENT_1,
            MARKET, EDUCATION_1, GIFT, HEALTHY, GAMES, INVESTMENT,
            ELETRONICS_1, REPAIR, ACESSORIES, CLOTHING, CAR, MOTORCYCLE,
            TRIP, HOUSE, DONATION, PETS, FEES, GYM, CELLPHONE,
            PERSONAL_HYGIENE_1, PHARMACY, CASH_WITHDRAWAL_1, RIDE, PAYMENT,
            SERVICES,DRINK,SHOES, STREAMING, OTHERS_EXPENSE
        )

        fun getExpenseCategoryListFull(): List<TransactionsCategory> = listOf(
            FOOD, TRANSPORT, ENTERTAINMENT_1, ENTERTAINMENT_2,
            MARKET, EDUCATION_1, EDUCATION_2, GIFT, HEALTHY, GAMES, INVESTMENT,
            ELETRONICS_1, ELETRONICS_2, REPAIR, ACESSORIES, CLOTHING, CAR, MOTORCYCLE,
            TRIP, HOUSE, DONATION, PETS, FEES, GYM, CELLPHONE,
            PERSONAL_HYGIENE_1, PERSONAL_HYGIENE_2, PHARMACY, CASH_WITHDRAWAL_1,
            CASH_WITHDRAWAL_2, RIDE, PAYMENT, SERVICES, DRINK, SHOES,
            STREAMING, OTHERS_EXPENSE
        )

        fun getEarningCategoryList(): List<TransactionsCategory> = listOf(
            SALE_OF_SHARES, SALARY, CASHBACK, FREELANCE,
            OTHER, DIVIDEND, BENEFITS, INCOME, SALE
        )

        fun getTransactionCategoryList(): List<TransactionsCategory> = listOf(
            FOOD, TRANSPORT, ENTERTAINMENT_1, ENTERTAINMENT_2,
            MARKET, EDUCATION_1, EDUCATION_2, GIFT, HEALTHY, GAMES, INVESTMENT,
            ELETRONICS_1, ELETRONICS_2, REPAIR, ACESSORIES, CLOTHING, CAR, MOTORCYCLE,
            TRIP, HOUSE, DONATION, PETS, FEES, GYM, CELLPHONE,
            PERSONAL_HYGIENE_1, PERSONAL_HYGIENE_2, PHARMACY, CASH_WITHDRAWAL_1,
            CASH_WITHDRAWAL_2, RIDE, PAYMENT, SERVICES, SALE_OF_SHARES, SALARY, CASHBACK,
            FREELANCE, OTHER, DIVIDEND, BENEFITS, INCOME, DRINK, SHOES, STREAMING,
            OTHERS_EXPENSE, SALE

        )
    }
}
