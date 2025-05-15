package com.example.fico.presentation.fragments.home

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.example.fico.R
import com.example.fico.databinding.FragmentHomeEarningsBinding
import com.example.fico.databinding.FragmentHomeExpensesBinding
import com.example.fico.presentation.viewmodel.HomeEarningsViewModel
import com.example.fico.presentation.viewmodel.HomeExpensesViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject


class HomeEarningsFragment : Fragment() {

    private var _binding : FragmentHomeEarningsBinding? = null
    private val binding get() = _binding!!
    private val viewModel : HomeEarningsViewModel by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeEarningsBinding.inflate(inflater, container, false)
        val rootView = binding.root

        setupListeners()

        // Inflate the layout for this fragment
        return rootView
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        viewModel.getEarningsInfo()
    }

    private fun setupListeners(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when(state){
                    is HomeFragmentState.Loading -> {
                        binding.clInfo.visibility = View.GONE
                        binding.clHomeExpensesEmptyState.visibility = View.GONE
                        binding.pbHomeEarnings.visibility = View.VISIBLE
                    }
                    is HomeFragmentState.Empty ->{
                        binding.clInfo.visibility = View.GONE
                        binding.clHomeExpensesEmptyState.visibility = View.VISIBLE
                        binding.pbHomeEarnings.visibility = View.GONE
                    }
                    is HomeFragmentState.Error -> {

                    }
                    is HomeFragmentState.Success -> {
                        binding.clInfo.visibility = View.VISIBLE
                        binding.clHomeExpensesEmptyState.visibility = View.GONE
                        binding.pbHomeEarnings.visibility = View.GONE

                        val totalEarningOfMonth = state.info
                        binding.tvMonthTotalEarningValue.text = totalEarningOfMonth
                    }
                }
            }
        }
    }
}