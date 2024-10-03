package com.example.fico.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.fico.R
import com.example.fico.shared.constants.StringConstants

class BudgetConfigurationListViewModel(
    context : Context
) : ViewModel(){
    val budgetConfigurationList : MutableList<String> = mutableListOf(
        context.getString(R.string.default_budget_activity_title),
        context.getString(R.string.budget_per_month_item_list),
    )
}