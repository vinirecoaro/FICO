package com.example.fico.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.fico.R
import com.example.fico.databinding.FragmentSetMonthBudgetBinding
import com.example.fico.ui.viewmodel.AddExpenseViewModel
import com.example.fico.ui.viewmodel.SetMonthBudgetViewModel

class SetMonthBudget : Fragment() {

    private val binding by lazy {FragmentSetMonthBudgetBinding.inflate(layoutInflater)}
    private val viewModel by viewModels<SetMonthBudgetViewModel>()
    private val viewModel2 by viewModels<AddExpenseViewModel>()

    companion object {
        private const val PRICE = "price"
        private const val DESCRIPTION = "description"
        private const val CATEGORY = "category"
        private const val DATE = "date"

        fun newInstance(param1: String, param2: String, param3: String, param4: String): SetMonthBudget {
            val fragment = SetMonthBudget()
            val args = Bundle()
            args.putString(PRICE, param1)
            args.putString(DESCRIPTION, param2)
            args.putString(CATEGORY, param3)
            args.putString(DATE, param4)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        setUpListeners()

        return binding.root
    }

    private fun setUpListeners() {

        arguments?.let {
            val price = it.getString(PRICE)
            val description = it.getString(DESCRIPTION)
            val category = it.getString(CATEGORY)
            val date = it.getString(DATE)

            val dateModified = date?.substring(0,7)

            binding.btSave.setOnClickListener {
                val budget = binding.etBudget.text.toString()
                if (dateModified != null) {
                    viewModel.setUpBudget(budget,dateModified)
                }
                if (price != null && description != null && category != null && date != null) {
                    viewModel2.addExpense(
                        price,
                        description,
                        category,
                        date
                    )
                }

            }
        }
    }


}