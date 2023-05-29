package com.example.fico.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.fico.R
import com.example.fico.databinding.FragmentSetMonthBudgetBinding
import com.example.fico.ui.viewmodel.SetMonthBudgetViewModel

class SetMonthBudget : Fragment() {

    private val binding by lazy {FragmentSetMonthBudgetBinding.inflate(layoutInflater)}
    private val viewModel by viewModels<SetMonthBudgetViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        setUpListeners()

        return binding.root
    }

    private fun setUpListeners(){
        binding.btSave.setOnClickListener{
            val budget = binding.etBudget.text.toString()
            viewModel.setUpBudget(budget)
        }
    }


}