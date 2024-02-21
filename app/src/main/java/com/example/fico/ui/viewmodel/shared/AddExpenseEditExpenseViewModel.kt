package com.example.fico.ui.viewmodel.shared

import androidx.lifecycle.ViewModel
import com.example.fico.domain.model.ExpenseCategory
import com.example.fico.util.constants.AppConstants

class AddExpenseEditExpenseViewModel : ViewModel() {

    val categoryList = listOf(
        ExpenseCategory(
            AppConstants.CATEGORY_LIST.DESCRIPTION.FOOD,
            AppConstants.CATEGORY_LIST.ICON_NAME.FOOD,
            false
        ),
        ExpenseCategory(
            AppConstants.CATEGORY_LIST.DESCRIPTION.TRANSPORT,
            AppConstants.CATEGORY_LIST.ICON_NAME.TRANSPORT,
            false
        ),
        ExpenseCategory(
            AppConstants.CATEGORY_LIST.DESCRIPTION.ENTERTAINMENT,
            AppConstants.CATEGORY_LIST.ICON_NAME.ENTERTAINMENT,
            false
        ),
        ExpenseCategory(
            AppConstants.CATEGORY_LIST.DESCRIPTION.MARKET,
            AppConstants.CATEGORY_LIST.ICON_NAME.MARKET,
            false
        ),
        ExpenseCategory(
            AppConstants.CATEGORY_LIST.DESCRIPTION.EDUCATION,
            AppConstants.CATEGORY_LIST.ICON_NAME.EDUCATION,
            false
        ),
        ExpenseCategory(
            AppConstants.CATEGORY_LIST.DESCRIPTION.GIFT,
            AppConstants.CATEGORY_LIST.ICON_NAME.GIFT,
            false
        ),
        ExpenseCategory(
            AppConstants.CATEGORY_LIST.DESCRIPTION.HEALTHY,
            AppConstants.CATEGORY_LIST.ICON_NAME.HEALTHY,
            false
        ),
        ExpenseCategory(
            AppConstants.CATEGORY_LIST.DESCRIPTION.GAMES,
            AppConstants.CATEGORY_LIST.ICON_NAME.GAMES,
            false
        ),
        ExpenseCategory(
            AppConstants.CATEGORY_LIST.DESCRIPTION.INVESTMENT,
            AppConstants.CATEGORY_LIST.ICON_NAME.INVESTMENT,
            false
        ),

    )

}