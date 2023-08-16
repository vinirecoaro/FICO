package com.example.fico.ui.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.fico.databinding.FragmentHomeBinding

import com.example.fico.ui.viewmodel.MainViewModel

class HomeFragment : Fragment(){

    private var _binding : FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<MainViewModel>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater,container,false)
        val rootView = binding.root
        setUpListeners()
        viewModel.returnTotalExpense(binding.tvTotalExpensesValue)
        viewModel.returnAvailableNow(binding.tvAvailableThisMonthValue, viewModel.getCurrentYearMonth().toString())
        viewModel.returnMonthExpense(binding.tvTotalExpensesThisMonthValue, viewModel.getCurrentYearMonth().toString())
        return rootView
    }

    private fun setUpListeners(){
        binding.tvTotalExpensesValue.setOnClickListener {
            viewModel.ShowHideValue(binding.tvTotalExpensesValue)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}