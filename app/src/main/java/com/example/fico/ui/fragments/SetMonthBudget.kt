package com.example.fico.ui.fragments

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.fico.databinding.FragmentSetMonthBudgetBinding
import com.example.fico.ui.interfaces.OnButtonClickListener
import com.example.fico.ui.viewmodel.AddExpenseSetBudgetSharedViewModel
import com.example.fico.ui.viewmodel.SetMonthBudgetViewModel

class SetMonthBudget : Fragment() {

    private val binding by lazy {FragmentSetMonthBudgetBinding.inflate(layoutInflater)}
    private val viewModel by viewModels<SetMonthBudgetViewModel>()
    private lateinit var listener: OnButtonClickListener
    private val sharedViewModel: AddExpenseSetBudgetSharedViewModel by activityViewModels()

    companion object {
        private const val VALUE = "value"
        private const val DESCRIPTION = "description"
        private const val CATEGORY = "category"
        private const val DATE = "date"

        fun newInstance(param1: String, param2: String, param3: String, param4: String): SetMonthBudget {
            val fragment = SetMonthBudget()
            val args = Bundle()
            args.putString(VALUE, param1)
            args.putString(DESCRIPTION, param2)
            args.putString(CATEGORY, param3)
            args.putString(DATE, param4)
            fragment.arguments = args
            return fragment
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        setUpListeners()

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as OnButtonClickListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context deve implementar a interface OnButtonClickListener")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpListeners() {

        arguments?.let {
            val formatedNum = it.getString(VALUE)
            val description = it.getString(DESCRIPTION)
            val category = it.getString(CATEGORY)
            val date = it.getString(DATE)
            val dateModified = date?.substring(0,7)

            binding.btSave.setOnClickListener {

                val budget = binding.etBudget.text.toString()
                if (formatedNum != null && description != null && category != null && dateModified != null ) {
                    viewModel.setUpBudget(budget,dateModified)
                    viewModel.addExpense(formatedNum, description, category, date)
                }
                sharedViewModel.price.value = ""
                sharedViewModel.description.value = ""
                sharedViewModel.category.value = ""
                listener.onSaveButtonFragmentClick()
                binding.root.visibility = View.GONE
            }
        }
    }


}