package com.example.fico.presentation.fragments.home

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
import com.example.fico.databinding.FragmentHomeEarningsBinding
import com.example.fico.databinding.FragmentHomeExpensesBinding
import com.example.fico.interfaces.OnMonthSelectedListener
import com.example.fico.presentation.adapters.MonthsForHorizontalRecyclerViewAdapter
import com.example.fico.presentation.viewmodel.HomeEarningsViewModel
import com.example.fico.presentation.viewmodel.HomeExpensesViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.text.NumberFormat


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
                        binding.clHomeEarningsInfo.visibility = View.GONE
                        binding.clHomeEarningsEmptyState.visibility = View.GONE
                        binding.pbHomeEarnings.visibility = View.VISIBLE
                    }
                    is HomeFragmentState.Empty ->{
                        binding.clHomeEarningsInfo.visibility = View.GONE
                        binding.clHomeEarningsEmptyState.visibility = View.VISIBLE
                        binding.pbHomeEarnings.visibility = View.GONE
                    }
                    is HomeFragmentState.Error -> {

                    }
                    is HomeFragmentState.Success -> {

                        val earningsInfo = state.info
                        //Earning months
                        adapter.updateExpenseMonths(earningsInfo.earningMonths)
                        adapter.focusOnCurrentMonth(binding.rvEarningMonths, earningsInfo.month)
                        //Total earning of month
                        binding.tvMonthTotalEarningValue.text = earningsInfo.totalEarningOfMonth
                        //Earnings per category
                        setEarningsPerCategory(earningsInfo.topFiveEarningByCategoryList)

                        //Show components
                        binding.clHomeEarningsInfo.visibility = View.VISIBLE
                        binding.clHomeEarningsEmptyState.visibility = View.GONE
                        binding.pbHomeEarnings.visibility = View.GONE
                    }
                }
            }
        }

        adapter.setOnItemClickListener(object : OnMonthSelectedListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onExpenseMonthSelected(date: String) {
                val formattedDate = FormatValuesToDatabase().formatDateFromFilterToDatabaseForInfoPerMonth(date)
                viewModel.getEarningsInfo(formattedDate)
            }
        })

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setEarningsPerCategory(earningsPerCategoryList : List<Pair<String, Double>>){
        initEarningsPerCategoryChart(earningsPerCategoryList)

        val imageViews = listOf(
            binding.ivIconCategoriesLegend1HomeEarnings,
            binding.ivIconCategoriesLegend2HomeEarnings,
            binding.ivIconCategoriesLegend3HomeEarnings,
            binding.ivIconCategoriesLegend4HomeEarnings,
            binding.ivIconCategoriesLegend5HomeEarnings
        )

        val textViews = listOf(
            binding.tvTextCategoriesList1HomeEarnings,
            binding.tvTextCategoriesList2HomeEarnings,
            binding.tvTextCategoriesList3HomeEarnings,
            binding.tvTextCategoriesList4HomeEarnings,
            binding.tvTextCategoriesList5HomeEarnings
        )

        val colors = viewModel.getPieChartCategoriesColors()

        for (i in imageViews.indices) {
            if (i < earningsPerCategoryList.size) {
                val (category, amount) = earningsPerCategoryList[i]
                imageViews[i].visibility = View.VISIBLE
                textViews[i].visibility = View.VISIBLE
                imageViews[i].setColorFilter(colors[i])
                val text = "$category\n${NumberFormat.getCurrencyInstance().format(amount.toFloat())}"
                textViews[i].text = text
            } else {
                imageViews[i].visibility = View.GONE
                textViews[i].visibility = View.GONE
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initEarningsPerCategoryChart(categoriesList : List<Pair<String, Double>>) {

        val pieChart = binding.pcEarningsPerCategory
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
        val paletteColors = viewModel.getPieChartCategoriesColors()
        val colors = mutableListOf<Int>()
        categoriesList.forEachIndexed { index, category ->
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
        pieChart.setHoleRadius(55f) // middle chart hole size
        pieChart.setTransparentCircleRadius(60f) // Transparent area size
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