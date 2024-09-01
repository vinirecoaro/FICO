package com.example.fico.shared.constants
import android.content.Context
import com.example.fico.R
import com.example.fico.model.TransactionCategory

class CategoriesList(private val context : Context) {

        fun getExpenseCategoryList() : List<TransactionCategory>{
            return listOf(
                TransactionCategory(
                    context.getString(R.string.category_list_description_food),
                    StringConstants.CATEGORY_LIST.ICON_NAME.FOOD,
                    false
                ),
                TransactionCategory(
                    context.getString(R.string.category_list_description_transport),
                    StringConstants.CATEGORY_LIST.ICON_NAME.TRANSPORT,
                    false
                ),
                TransactionCategory(
                    context.getString(R.string.category_list_description_entertainment),
                    StringConstants.CATEGORY_LIST.ICON_NAME.ENTERTAINMENT,
                    false
                ),
                TransactionCategory(
                    context.getString(R.string.category_list_description_market),
                    StringConstants.CATEGORY_LIST.ICON_NAME.MARKET,
                    false
                ),
                TransactionCategory(
                    context.getString(R.string.category_list_description_education),
                    StringConstants.CATEGORY_LIST.ICON_NAME.EDUCATION,
                    false
                ),
                TransactionCategory(
                    context.getString(R.string.category_list_description_gift),
                    StringConstants.CATEGORY_LIST.ICON_NAME.GIFT,
                    false
                ),
                TransactionCategory(
                    context.getString(R.string.category_list_description_healthy),
                    StringConstants.CATEGORY_LIST.ICON_NAME.HEALTHY,
                    false
                ),
                TransactionCategory(
                    context.getString(R.string.category_list_description_games),
                    StringConstants.CATEGORY_LIST.ICON_NAME.GAMES,
                    false
                ),
                TransactionCategory(
                    context.getString(R.string.category_list_description_investment),
                    StringConstants.CATEGORY_LIST.ICON_NAME.INVESTMENT,
                    false
                ),
                TransactionCategory(
                    context.getString(R.string.category_list_description_eletronics),
                    StringConstants.CATEGORY_LIST.ICON_NAME.ELETRONICS,
                    false
                ),
                TransactionCategory(
                    context.getString(R.string.category_list_description_repair),
                    StringConstants.CATEGORY_LIST.ICON_NAME.REPAIR,
                    false
                ),
                TransactionCategory(
                    context.getString(R.string.category_list_description_acessories),
                    StringConstants.CATEGORY_LIST.ICON_NAME.ACESSORIES,
                    false
                ),
                TransactionCategory(
                    context.getString(R.string.category_list_description_clothing),
                    StringConstants.CATEGORY_LIST.ICON_NAME.CLOTHING,
                    false
                ),
                TransactionCategory(
                    context.getString(R.string.category_list_description_car),
                    StringConstants.CATEGORY_LIST.ICON_NAME.CAR,
                    false
                ),
                TransactionCategory(
                    context.getString(R.string.category_list_description_motorcycle),
                    StringConstants.CATEGORY_LIST.ICON_NAME.MOTORCYCLE,
                    false
                ),
                TransactionCategory(
                    context.getString(R.string.category_list_description_trip),
                    StringConstants.CATEGORY_LIST.ICON_NAME.TRIP,
                    false
                ),
                TransactionCategory(
                    context.getString(R.string.category_list_description_house),
                    StringConstants.CATEGORY_LIST.ICON_NAME.HOUSE,
                    false
                ),
                TransactionCategory(
                    context.getString(R.string.category_list_description_donation),
                    StringConstants.CATEGORY_LIST.ICON_NAME.DONATION,
                    false
                ),
                TransactionCategory(
                    context.getString(R.string.category_list_description_bet),
                    StringConstants.CATEGORY_LIST.ICON_NAME.BET,
                    false
                ),
                TransactionCategory(
                    context.getString(R.string.category_list_description_pets),
                    StringConstants.CATEGORY_LIST.ICON_NAME.PETS,
                    false
                ),
                TransactionCategory(
                    context.getString(R.string.category_list_description_fees),
                    StringConstants.CATEGORY_LIST.ICON_NAME.FEES,
                    false
                ),
                TransactionCategory(
                    context.getString(R.string.category_list_description_gym),
                    StringConstants.CATEGORY_LIST.ICON_NAME.GYM,
                    false
                ),
                TransactionCategory(
                    context.getString(R.string.category_list_description_cellphone),
                    StringConstants.CATEGORY_LIST.ICON_NAME.CELLPHONE,
                    false
                ),
                TransactionCategory(
                    context.getString(R.string.category_list_description_personal_hygiene),
                    StringConstants.CATEGORY_LIST.ICON_NAME.PERSONAL_HYGIENE,
                    false
                ),
                TransactionCategory(
                    context.getString(R.string.category_list_description_pharmacy),
                    StringConstants.CATEGORY_LIST.ICON_NAME.PHARMACY,
                    false
                ),
                TransactionCategory(
                    context.getString(R.string.category_list_description_cash_withdrawal),
                    StringConstants.CATEGORY_LIST.ICON_NAME.CASH_WITHDRAWAL,
                    false
                ),
                TransactionCategory(
                    context.getString(R.string.category_list_description_ride),
                    StringConstants.CATEGORY_LIST.ICON_NAME.RIDE,
                    false
                ),
                TransactionCategory(
                    context.getString(R.string.category_list_description_payment),
                    StringConstants.CATEGORY_LIST.ICON_NAME.PAYMENT,
                    false
                ),
                TransactionCategory(
                    context.getString(R.string.category_list_description_services),
                    StringConstants.CATEGORY_LIST.ICON_NAME.SERVICES,
                    false
                )

            )
        }

    fun getEarningCategoryList() : List<TransactionCategory>{
        return listOf(
            TransactionCategory(
                context.getString(R.string.category_list_description_sale_of_shares),
                StringConstants.CATEGORY_LIST.ICON_NAME.SALE_OF_SHARES,
                false
            ),
            TransactionCategory(
                context.getString(R.string.category_list_description_salary),
                StringConstants.CATEGORY_LIST.ICON_NAME.SALARY,
                false
            ),
            TransactionCategory(
                context.getString(R.string.category_list_description_cashback),
                StringConstants.CATEGORY_LIST.ICON_NAME.CASHBACK,
                false
            ),
            TransactionCategory(
                context.getString(R.string.category_list_description_freelance),
                StringConstants.CATEGORY_LIST.ICON_NAME.FREELANCE,
                false
            ),
            TransactionCategory(
                context.getString(R.string.category_list_description_other),
                StringConstants.CATEGORY_LIST.ICON_NAME.OTHER,
                false
            ),
            TransactionCategory(
                context.getString(R.string.category_list_description_dividend),
                StringConstants.CATEGORY_LIST.ICON_NAME.DIVIDEND,
                false
            ),
            TransactionCategory(
                context.getString(R.string.category_list_description_benefits),
                StringConstants.CATEGORY_LIST.ICON_NAME.BENEFITS,
                false
            ),
        )
    }
}