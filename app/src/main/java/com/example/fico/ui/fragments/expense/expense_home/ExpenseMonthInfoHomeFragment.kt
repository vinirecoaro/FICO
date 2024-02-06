package com.example.fico.ui.fragments.expense.expense_home

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
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.fico.R
import com.example.fico.databinding.FragmentExpenseMonthInfoHomeBinding
import com.example.fico.ui.adapters.CategoryListAdapter
import com.example.fico.ui.adapters.ExpenseMonthsListAdapter
import com.example.fico.ui.viewmodel.HomeViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ExpenseMonthInfoHomeFragment : Fragment() {

    private var _binding : FragmentExpenseMonthInfoHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<HomeViewModel>()
    private lateinit var adapter : ExpenseMonthsListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentExpenseMonthInfoHomeBinding.inflate(inflater, container, false)
        val rootView = binding.root

        lifecycleScope.launch {
            //Create month chooser and focus in the current month
            adapter = ExpenseMonthsListAdapter(emptyList())
            binding.rvExpenseMonths.adapter = adapter
        }

        return rootView
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()

        getAvailableNow()
        getMonthExpense()
        initMonthExpenseChart()
        initAvailableNowChart()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getAvailableNow(){
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val availableNow = viewModel.getAvailableNow(viewModel.getCurrentlyDate()).await()
                val availableNowJustNumber = viewModel.getAvailableNow(viewModel.getCurrentlyDate(), formatted = false).await()
                if(availableNow == "---"){
                    binding.tvAvailableThisMonthValue.text = availableNow
                } else if(availableNowJustNumber.toFloat() < 0){
                    val myColor = ContextCompat.getColor(requireContext(), R.color.red)
                    binding.tvAvailableThisMonthValue.setTextColor(myColor)
                    binding.tvAvailableThisMonthValue.text = availableNow
                } else {
                    binding.tvAvailableThisMonthValue.text = availableNow
                }
            }catch (exception:Exception){}
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun getMonthExpense() {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val monthExpense = viewModel.getMonthExpense(viewModel.getCurrentlyDate()).await()
                binding.tvTotalExpensesThisMonthValue.text = monthExpense
            } catch (exception: Exception) {
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initMonthExpenseChart(date : String = viewModel.getCurrentlyDate()) {

        val pieChart = binding.pcMonthExpense
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
        pieChart.data = emptyData

        pieChart.setUsePercentValues(false)
        pieChart.description.isEnabled = false
        pieChart.setHoleRadius(80f) // middle chart hole size
        pieChart.setTransparentCircleRadius(85f) // Transparent area size
        pieChart.setHoleColor(holeColor)
        pieChart.legend.isEnabled = false
        pieChart.data.setDrawValues(false)// Ocult label values
        pieChart.animateY(1400, Easing.EaseInOutQuad)// Circular animation on create chart

        pieChart.invalidate()

        lifecycleScope.launch{

            val monthExpenseValue = viewModel.getMonthExpense(date, formatted = false).await()
            var monthExpenseValueFormatted = 0f
            if(monthExpenseValue != "---"){
                monthExpenseValueFormatted = monthExpenseValue.toFloat()
            }
            var monthExpenseColor = ""

            val availableNowValue = viewModel.getAvailableNow(date, formatted = false).await()
            var availableNowValueFormatted = 1f
            if(availableNowValue != "---"){
                availableNowValueFormatted = availableNowValue.toFloat()
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

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initAvailableNowChart(date : String = viewModel.getCurrentlyDate()) {

        val pieChart = binding.pcAvailableNow
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
        pieChart.data = emptyData

        pieChart.setUsePercentValues(false)
        pieChart.description.isEnabled = false
        pieChart.setHoleRadius(80f) // middle chart hole size
        pieChart.setTransparentCircleRadius(85f) // Transparent area size
        pieChart.setHoleColor(holeColor)
        pieChart.legend.isEnabled = false
        pieChart.data.setDrawValues(false)// Ocult label values
        pieChart.animateY(1400, Easing.EaseInOutQuad)// Circular animation on create chart

        pieChart.invalidate()

        lifecycleScope.launch{

            val monthExpenseValue = viewModel.getMonthExpense(date, formatted = false).await()
            var monthExpenseValueFormatted = 0f
            if(monthExpenseValue != "---"){
                monthExpenseValueFormatted = monthExpenseValue.toFloat()
            }

            val availableNowValue = viewModel.getAvailableNow(date, formatted = false).await()
            var availableNowValueFormatted = 1f
            if(availableNowValue != "---"){
                availableNowValueFormatted = availableNowValue.toFloat()
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

}