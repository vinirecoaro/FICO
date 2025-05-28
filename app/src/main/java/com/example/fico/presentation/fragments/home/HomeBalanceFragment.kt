package com.example.fico.presentation.fragments.home

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.example.fico.R
import com.example.fico.api.FormatValuesToDatabase
import com.example.fico.databinding.FragmentHomeBalanceBinding
import com.example.fico.databinding.FragmentHomeEarningsBinding
import com.example.fico.interfaces.OnMonthSelectedListener
import com.example.fico.presentation.adapters.MonthsForHorizontalRecyclerViewAdapter
import com.example.fico.presentation.viewmodel.HomeBalanceViewModel
import com.example.fico.presentation.viewmodel.HomeEarningsViewModel
import com.example.fico.utils.constants.StringConstants
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class HomeBalanceFragment : Fragment() {

    private var _binding : FragmentHomeBalanceBinding? = null
    private val binding get() = _binding!!
    private val viewModel : HomeBalanceViewModel by inject()
    private lateinit var adapter : MonthsForHorizontalRecyclerViewAdapter

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBalanceBinding.inflate(inflater, container, false)
        val rootView = binding.root

        adapter = MonthsForHorizontalRecyclerViewAdapter(requireContext(),emptyList())
        binding.rvBalanceMonths.adapter = adapter

        setupListeners()

        return rootView
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        viewModel.getBalanceInfo()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupListeners(){
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when(state){
                    is HomeFragmentState.Loading -> {
                        binding.clHomeBalanceInfo.visibility = View.GONE
                        binding.clHomeBalanceEmptyState.visibility = View.GONE
                        binding.pbHomeBalance.visibility = View.VISIBLE
                    }
                    is HomeFragmentState.Empty ->{
                        binding.clHomeBalanceInfo.visibility = View.GONE
                        binding.clHomeBalanceEmptyState.visibility = View.VISIBLE
                        binding.pbHomeBalance.visibility = View.GONE
                    }
                    is HomeFragmentState.Error -> {

                    }
                    is HomeFragmentState.Success -> {

                        val balanceInfo = state.info
                        /*//Earning months
                        adapter.updateMonths(earningsInfo.earningMonths)
                        adapter.focusOnCurrentMonth(binding.rvEarningMonths, earningsInfo.month)*/
                        //Total earning of month
                        binding.tvBalanceMonthTotalEarningValue.text = balanceInfo.totalEarningOfMonth
                        //Total expense of month
                        binding.tvBalanceMonthTotalExpenseValue.text = balanceInfo.totalExpenseOfMonth
                        //Relative earning result
                        /*when (earningsInfo.relativeResult.second) {
                            StringConstants.HOME_FRAGMENT.NO_BEFORE_MONTH, StringConstants.HOME_FRAGMENT.EQUAL -> {
                                binding.tvRelativeIncomePercent.text = StringConstants.GENERAL.THREE_DASH
                                binding.ivArrowRelativeIncomePercent.visibility = View.GONE
                            }
                            StringConstants.HOME_FRAGMENT.INCREASE -> {
                                binding.tvRelativeIncomePercent.tooltipText = getString(R.string.relative_earning_increase_message)
                                val result = "${earningsInfo.relativeResult.first}%"
                                binding.tvRelativeIncomePercent.text = result
                                binding.tvRelativeIncomePercent.setTextColor(Color.rgb(0,255,0))
                                binding.ivArrowRelativeIncomePercent.visibility = View.VISIBLE
                                binding.ivArrowRelativeIncomePercent.setColorFilter(Color.rgb(0,255,0))
                                binding.ivArrowRelativeIncomePercent.setImageResource(R.drawable.arrow_up_model_2)
                            }
                            StringConstants.HOME_FRAGMENT.DECREASE -> {
                                binding.tvRelativeIncomePercent.tooltipText = getString(R.string.relative_earning_decrease_message)
                                val result = "${earningsInfo.relativeResult.first}%"
                                binding.tvRelativeIncomePercent.text = result
                                binding.tvRelativeIncomePercent.setTextColor(Color.rgb(255,0,0))
                                binding.ivArrowRelativeIncomePercent.visibility = View.VISIBLE
                                binding.ivArrowRelativeIncomePercent.setColorFilter(Color.rgb(255,0,0))
                                binding.ivArrowRelativeIncomePercent.setImageResource(R.drawable.arrow_down_model_2)
                            }
                        }*/

                        //Show components
                        binding.clHomeBalanceInfo.visibility = View.VISIBLE
                        binding.clHomeBalanceEmptyState.visibility = View.GONE
                        binding.pbHomeBalance.visibility = View.GONE
                    }
                }
            }
        }

        adapter.setOnItemClickListener(object : OnMonthSelectedListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onExpenseMonthSelected(date: String) {
                val formattedDate = FormatValuesToDatabase().formatDateFromFilterToDatabaseForInfoPerMonth(date)
                viewModel.getBalanceInfo(formattedDate)
            }
        })

    }
}