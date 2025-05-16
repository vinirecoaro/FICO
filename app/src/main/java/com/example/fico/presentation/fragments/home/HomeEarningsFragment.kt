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
import com.example.fico.presentation.adapters.MonthsForHorizontalRecyclerViewAdapter
import com.example.fico.presentation.viewmodel.HomeEarningsViewModel
import com.example.fico.presentation.viewmodel.HomeExpensesViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject


class HomeEarningsFragment : Fragment() {

    private var _binding : FragmentHomeEarningsBinding? = null
    private val binding get() = _binding!!
    private val viewModel : HomeEarningsViewModel by inject()
    private lateinit var adapter : MonthsForHorizontalRecyclerViewAdapter

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeEarningsBinding.inflate(inflater, container, false)
        val rootView = binding.root

        adapter = MonthsForHorizontalRecyclerViewAdapter(requireContext(),emptyList())
        binding.rvEarningMonths.adapter = adapter

        setupListeners()

        // Inflate the layout for this fragment
        return rootView
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        viewModel.getEarningsInfo()
    }

    @RequiresApi(Build.VERSION_CODES.O)
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

                        val earningsInfo = state.info
                        //Earning months
                        adapter.updateExpenseMonths(earningsInfo.earningMonths)
                        adapter.focusOnCurrentMonth(binding.rvEarningMonths)
                        //Total earning of month
                        binding.tvMonthTotalEarningValue.text = earningsInfo.totalEarningOfMonth
                    }
                }
            }
        }
    }
}