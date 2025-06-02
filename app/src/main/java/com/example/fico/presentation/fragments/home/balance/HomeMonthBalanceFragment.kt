package com.example.fico.presentation.fragments.home.balance

import android.content.res.Configuration
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
import com.example.fico.interfaces.OnMonthSelectedListener
import com.example.fico.presentation.adapters.MonthsForHorizontalRecyclerViewAdapter
import com.example.fico.presentation.fragments.home.HomeFragmentState
import com.example.fico.presentation.viewmodel.HomeMonthBalanceViewModel
import com.example.fico.utils.constants.StringConstants
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class HomeMonthBalanceFragment : Fragment() {

    private var _binding : FragmentHomeBalanceBinding? = null
    private val binding get() = _binding!!
    private val viewModel : HomeMonthBalanceViewModel by inject()
    private lateinit var adapter : MonthsForHorizontalRecyclerViewAdapter

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
        viewModel.getMonthBalanceInfo()
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
                        //Balance months
                        adapter.updateMonths(balanceInfo.balanceMonths)
                        adapter.focusOnCurrentMonth(binding.rvBalanceMonths, balanceInfo.month)
                        //Relative earning result
                        when (balanceInfo.monthBalance.second) {
                            StringConstants.GENERAL.POSITIVE_NUMBER -> {
                                binding.tvBalanceValue.text = balanceInfo.monthBalance.first
                                binding.tvBalanceValue.setTextColor(Color.rgb(0,255,0))
                            }
                            StringConstants.GENERAL.NEGATIVE_NUMBER -> {
                                binding.tvBalanceValue.text = balanceInfo.monthBalance.first
                                binding.tvBalanceValue.setTextColor(Color.rgb(255,0,0))
                            }
                        }

                        //Init chart
                        initEarningsPerCategoryChart(balanceInfo.chartInfo)
                        binding.tvMonthExpenseValue.text = balanceInfo.totalExpenseOfMonth
                        binding.tvMonthEarningValue.text = balanceInfo.totalEarningOfMonth

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
                viewModel.getMonthBalanceInfo(formattedDate)
            }
        })

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initEarningsPerCategoryChart(monthTransactionsValue : List<Pair<String, Double>>) {

        val pieChart = binding.pcCashFlow
        var holeColor = 1

        // Defining chart insede hole color
        when (context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                holeColor = Color.rgb(104, 110, 106)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                holeColor = Color.WHITE
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }

        // Create a entries list for Pie Chart and set a color for each entry
        val entries = mutableListOf<PieEntry>()
        val paletteColors = viewModel.getPieChartTransactionsColors()
        val colors = mutableListOf<Int>()
        monthTransactionsValue.forEachIndexed { index, category ->
            entries.add(PieEntry(category.second.toFloat(), category.first))
            colors.add(paletteColors[index])
        }

        // Create a data set from entries
        val dataSet = PieDataSet(entries, getString(R.string.categories))
        dataSet.colors = colors

        // Data set customizing
        dataSet.sliceSpace = 2f

        // Create an PieData object from data set
        val pieData = PieData(dataSet)
        pieData.setValueFormatter(PercentFormatter(pieChart)) // Format value as percentage

        // Configure the PieChart
        pieChart.data = pieData
        pieChart.setUsePercentValues(false)
        pieChart.description.isEnabled = false
        pieChart.holeRadius = 55f // middle chart hole size
        pieChart.transparentCircleRadius = 60f // Transparent area size
        pieChart.setHoleColor(holeColor)
        pieChart.legend.isEnabled = false

        // Ocult label values
        pieData.setDrawValues(false)
        pieChart.setDrawEntryLabels(false)

        // Circular animation on create chart
        pieChart.animateY(1400, Easing.EaseInOutQuad)

        // Update the chart
        pieChart.invalidate()

    }

}