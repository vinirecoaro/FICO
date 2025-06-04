package com.example.fico.presentation.fragments.home.balance

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.fico.databinding.FragmentHomeAllBalanceBinding
import com.example.fico.presentation.fragments.home.HomeFragmentState
import com.example.fico.presentation.viewmodel.HomeAllBalanceViewModel
import com.example.fico.components.charts.ChartsCreator
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class HomeAllBalanceFragment : Fragment() {

    private var _binding : FragmentHomeAllBalanceBinding? = null
    private val binding get() = _binding!!
    private val viewModel : HomeAllBalanceViewModel by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeAllBalanceBinding.inflate(inflater, container, false)
        val rootView = binding.root

        setupListeners()

        return rootView
    }

    override fun onResume() {
        super.onResume()
        viewModel.getBalanceInfo()
    }

    private fun setupListeners(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when(state){
                    is HomeFragmentState.Loading -> {
                        binding.clHomeAllBalanceInfo.visibility = View.GONE
                        binding.clHomeAllBalanceEmptyState.visibility = View.GONE
                        binding.pbHomeAllBalance.visibility = View.VISIBLE
                    }
                    is HomeFragmentState.Empty ->{
                        binding.clHomeAllBalanceInfo.visibility = View.GONE
                        binding.clHomeAllBalanceEmptyState.visibility = View.VISIBLE
                        binding.pbHomeAllBalance.visibility = View.GONE
                    }
                    is HomeFragmentState.Error -> {

                    }
                    is HomeFragmentState.Success -> {

                        val balanceInfo = state.info

                        ChartsCreator.lineChart(
                            requireActivity(),
                            requireContext(),
                            binding.lcBalancePerMonth,
                            balanceInfo.xyEntries,
                            balanceInfo.yLabels,
                            ::onChartSingleTapped
                        )

                        binding.clHomeAllBalanceInfo.visibility = View.VISIBLE
                        binding.clHomeAllBalanceEmptyState.visibility = View.GONE
                        binding.pbHomeAllBalance.visibility = View.GONE

                    }
                }
            }
        }
    }

    private fun onChartSingleTapped() : Boolean{
        if(viewModel.getLineChartYAxisVisible()){
            viewModel.setLineChartYAxisVisible(false)
            return false
        }else{
            viewModel.setLineChartYAxisVisible(true)
            return true
        }
    }

}