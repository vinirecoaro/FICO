package com.example.fico.ui.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.fico.R
import com.example.fico.databinding.FragmentSetMonthBudgetBinding
import com.example.fico.ui.interfaces.OnButtonClickListener
import com.example.fico.ui.viewmodel.AddExpenseViewModel
import com.example.fico.ui.viewmodel.SetMonthBudgetViewModel

class SetMonthBudget : Fragment() {

    private val binding by lazy {FragmentSetMonthBudgetBinding.inflate(layoutInflater)}
    private val viewModel by viewModels<SetMonthBudgetViewModel>()
    private lateinit var listener: OnButtonClickListener

    companion object {
        private const val DATE = "date"

        fun newInstance(param1: String): SetMonthBudget {
            val fragment = SetMonthBudget()
            val args = Bundle()
            args.putString(DATE, param1)
            fragment.arguments = args
            return fragment
        }
    }

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

    private fun setUpListeners() {

        arguments?.let {
            val date = it.getString(DATE)
            val dateModified = date?.substring(0,7)

            binding.btSave.setOnClickListener {

                val budget = binding.etBudget.text.toString()
                if (dateModified != null) {
                    viewModel.setUpBudget(budget,dateModified)
                }

                listener.onSaveButtonFragmentClick()
            }
        }
    }




}