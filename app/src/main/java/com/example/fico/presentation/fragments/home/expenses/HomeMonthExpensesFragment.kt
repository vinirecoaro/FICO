package com.example.fico.presentation.fragments.home.expenses

import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.fico.R
import com.example.fico.api.FormatValuesFromDatabase
import com.example.fico.api.FormatValuesToDatabase
import com.example.fico.databinding.FragmentHomeMonthExpensesBinding
import com.example.fico.presentation.adapters.MonthsForHorizontalRecyclerViewAdapter
import com.example.fico.interfaces.OnMonthSelectedListener
import com.example.fico.presentation.fragments.home.HomeFragmentState
import com.example.fico.presentation.viewmodel.HomeMonthExpensesViewModel
import com.example.fico.utils.DateFunctions
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.text.NumberFormat

class HomeMonthExpensesFragment : Fragment() {

    private var _binding : FragmentHomeMonthExpensesBinding? = null
    private val binding get() = _binding!!
    private val viewModel : HomeMonthExpensesViewModel by inject()
    private lateinit var adapter : MonthsForHorizontalRecyclerViewAdapter

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeMonthExpensesBinding.inflate(inflater, container, false)
        val rootView = binding.root

        adapter = MonthsForHorizontalRecyclerViewAdapter(requireContext(),emptyList())
        binding.rvExpenseMonths.adapter = adapter

        setUpListeners()

        initEmptyChart(binding.pcExpensePerCategory, binding.pcMonthExpense, binding.pcAvailableNow)

        return rootView
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        viewModel.getExpensesInfo()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpListeners(){

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when(state){
                    is HomeFragmentState.Loading -> {
                        binding.clInfo.visibility = View.GONE
                        binding.clHomeExpensesEmptyState.visibility = View.GONE
                        binding.pbExpensePerMonth.visibility = View.VISIBLE
                    }
                    is HomeFragmentState.Empty ->{
                        binding.clInfo.visibility = View.GONE
                        binding.clHomeExpensesEmptyState.visibility = View.VISIBLE
                        binding.pbExpensePerMonth.visibility = View.GONE
                    }
                    is HomeFragmentState.Error -> {

                    }
                    is HomeFragmentState.Success -> {

                        val expensesInfo = state.info

                        adapter.updateMonths(expensesInfo.expenseMonths)
                        adapter.focusOnCurrentMonth(binding.rvExpenseMonths, expensesInfo.month)

                        setAvailableNow(expensesInfo.availableNow, expensesInfo.availableNowFormattedToLocalCurrency()!!)
                        setMonthExpense(expensesInfo.monthExpenseFormattedToLocalCurrency()!!)
                        initMonthExpenseChart(expensesInfo.monthExpense, expensesInfo.availableNow)
                        initAvailableNowChart(expensesInfo.monthExpense, expensesInfo.availableNow)
                        setExpensePerCategoryChart(expensesInfo.topFiveExpenseByCategoryList)
                        setExpensePerCategoryChartLegend(expensesInfo.topFiveExpenseByCategoryList)

                        binding.clInfo.visibility = View.VISIBLE
                        binding.clHomeExpensesEmptyState.visibility = View.GONE
                        binding.pbExpensePerMonth.visibility = View.GONE
                    }
                }
            }
        }

        adapter.setOnItemClickListener(object : OnMonthSelectedListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onExpenseMonthSelected(date: String) {
                val formattedDate = FormatValuesToDatabase().formatDateFromFilterToDatabaseForInfoPerMonth(date)
                viewModel.getExpensesInfo(formattedDate)
            }
        })

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setAvailableNow(availableNow : String, availableNowFormatted : String){
        try {
            var myColor = ContextCompat.getColor(requireContext(), R.color.red)
            if(availableNowFormatted == "---"){
                binding.tvAvailableThisMonthValue.text = availableNowFormatted
            } else if(availableNow.toFloat() < 0){
                binding.tvAvailableThisMonthValue.setTextColor(myColor)
                binding.tvAvailableThisMonthValue.text = availableNowFormatted
            } else {
                when (context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        myColor = ContextCompat.getColor(requireContext(), R.color.white)
                    }
                    Configuration.UI_MODE_NIGHT_NO -> {
                        myColor = ContextCompat.getColor(requireContext(), R.color.black)
                    }
                    Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
                }
                binding.tvAvailableThisMonthValue.setTextColor(myColor)
                binding.tvAvailableThisMonthValue.text = availableNowFormatted
            }
        }catch (exception:Exception){}
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setMonthExpense(monthExpenseFormatted : String) {
        try {
            binding.tvTotalExpensesThisMonthValue.text = monthExpenseFormatted
        } catch (exception: Exception) {

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initMonthExpenseChart(monthExpense : String, availableNow : String) {
        val pieChart = binding.pcMonthExpense
        var holeColor = 1

        var monthExpenseValueFormatted = 0f
        if (monthExpense != "---") {
            monthExpenseValueFormatted = monthExpense.toFloat()
        }
        var monthExpenseColor = ""

        var availableNowValueFormatted = 1f
        if (availableNow != "---") {
            availableNowValueFormatted = availableNow.toFloat()
        }

        val budget = monthExpenseValueFormatted + availableNowValueFormatted

        // Defining color of availableNow part
        if(availableNowValueFormatted < 0){
            monthExpenseColor = "#ed2b15" // Red
        }else if(monthExpenseValueFormatted <= (budget/2)){
            monthExpenseColor = "#19d14e" // Green
        }else if(monthExpenseValueFormatted <= (budget*0.85)){
            monthExpenseColor = "#ebe23b" // Yellow
        }else if(monthExpenseValueFormatted > (budget*0.85)){
            monthExpenseColor = "#ed2b15" // Red
        }

        // Defining chart inside hole color
        when (context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                holeColor = Color.rgb(104, 110, 106)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                holeColor = Color.WHITE
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }

        // Create a entries list for Pie Chart
        val entries = mutableListOf<PieEntry>()
        if(availableNowValueFormatted < 0){
            entries.add(PieEntry(monthExpenseValueFormatted))
            entries.add(PieEntry(0f))
        }else{
            entries.add(PieEntry(monthExpenseValueFormatted))
            entries.add(PieEntry(availableNowValueFormatted))
        }

        // Colors for parts of chart
        val colors = listOf(
            Color.parseColor(monthExpenseColor), // FirstColor
            Color.parseColor("#9aa19c")  // SecondColor
        )

        // Create a data set from entries
        val dataSet = PieDataSet(entries, "Uso de Recursos")
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
        pieChart.setHoleRadius(80f) // middle chart hole size
        pieChart.setTransparentCircleRadius(85f) // Transparent area size
        pieChart.setHoleColor(holeColor)
        pieChart.legend.isEnabled = false

        // Ocult label values
        pieData.setDrawValues(false)

        // Circular animation on create chart
        pieChart.animateY(1400, Easing.EaseInOutQuad)

        // Update the chart
        pieChart.invalidate()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initAvailableNowChart(monthExpense : String, availableNow : String) {

        lifecycleScope.launch{

            val pieChart = binding.pcAvailableNow
            var holeColor = 1

            var monthExpenseValueFormatted = 0f
            if (monthExpense != "---") {
                monthExpenseValueFormatted = monthExpense.toFloat()
            }

            var availableNowValueFormatted = 1f
            if (availableNow != "---") {
                availableNowValueFormatted = availableNow.toFloat()
            }
            var availableNowColor = ""
            val budget = monthExpenseValueFormatted + availableNowValueFormatted

            // Defining color of availableNow part
            if(availableNowValueFormatted >= (budget/2)){
                availableNowColor = "#19d14e" // Green
            }else if(availableNowValueFormatted >= (budget*0.15)){
                availableNowColor = "#ebe23b" // Yellow
            }else if(availableNowValueFormatted < (budget*0.15)){
                availableNowColor = "#ed2b15" // Red
            }

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

            // Create a entries list for Pie Chart
            val entries = mutableListOf<PieEntry>()
            if(availableNowValueFormatted < 0){
                entries.add(PieEntry(0f))
                entries.add(PieEntry(monthExpenseValueFormatted))
            }else{
                entries.add(PieEntry(availableNowValueFormatted))
                entries.add(PieEntry(monthExpenseValueFormatted))
            }

            // Colors for parts of chart
            val colors = listOf(
                Color.parseColor(availableNowColor), // FirstColor
                Color.parseColor("#9aa19c")  // SecondColor
            )

            // Create a data set from entries
            val dataSet = PieDataSet(entries, "Uso de Recursos")
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
            pieChart.setHoleRadius(75f) // middle chart hole size
            pieChart.setTransparentCircleRadius(80f) // Transparent area size
            pieChart.setHoleColor(holeColor)
            pieChart.legend.isEnabled = false

            // Ocult label values
            pieData.setDrawValues(false)

            // Circular animation on create chart
            pieChart.animateY(1400, Easing.EaseInOutQuad)

            // Update the chart
            pieChart.invalidate()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setExpensePerCategoryChart(categoriesList : List<Pair<String, Double>>) {

        val pieChart = binding.pcExpensePerCategory
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

    private fun setExpensePerCategoryChartLegend(expensePerCategoryList :  List<Pair<String, Double>>){
        val imageViews = listOf(
            binding.ivIconCategoriesLegend1,
            binding.ivIconCategoriesLegend2,
            binding.ivIconCategoriesLegend3,
            binding.ivIconCategoriesLegend4,
            binding.ivIconCategoriesLegend5
        )

        val textViews = listOf(
            binding.tvTextCategoriesList1,
            binding.tvTextCategoriesList2,
            binding.tvTextCategoriesList3,
            binding.tvTextCategoriesList4,
            binding.tvTextCategoriesList5
        )

        val colors = viewModel.getPieChartCategoriesColors()

        for (i in imageViews.indices) {
            if (i < expensePerCategoryList.size) {
                val (category, amount) = expensePerCategoryList[i]
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

    private fun initEmptyChart(vararg charts : PieChart){
        for (chart in charts){
            var holeColor = 1

            when (context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    holeColor = Color.rgb(104, 110, 106)
                }
                Configuration.UI_MODE_NIGHT_NO -> {
                    holeColor = Color.WHITE
                }
                Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
            }

            val emptyEntries: ArrayList<PieEntry> = ArrayList()
            emptyEntries.add(PieEntry(0f, ""))

            val emptyChartColors = listOf(
                Color.parseColor("#19d14e"), // FirstColor
                Color.parseColor("#9aa19c")  // SecondColor
            )

            val emptyDataSet = PieDataSet(emptyEntries, "Label")
            emptyDataSet.colors = emptyChartColors

            val emptyData = PieData(emptyDataSet)
            chart.data = emptyData

            chart.setUsePercentValues(false)
            chart.description.isEnabled = false
            chart.setHoleRadius(80f) // middle chart hole size
            chart.setTransparentCircleRadius(85f) // Transparent area size
            chart.setHoleColor(holeColor)
            chart.legend.isEnabled = false
            chart.data.setDrawValues(false)// Ocult label values
            chart.animateY(1400, Easing.EaseInOutQuad)// Circular animation on create chart

            chart.invalidate()
        }
    }

}