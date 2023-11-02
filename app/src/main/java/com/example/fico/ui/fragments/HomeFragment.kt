package com.example.fico.ui.fragments

import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.fico.R
import com.example.fico.databinding.FragmentHomeBinding
import com.example.fico.ui.viewmodel.HomeViewModel
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeFragment : Fragment(){

    private var _binding : FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<HomeViewModel>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater,container,false)
        val rootView = binding.root
        binding.tvTotalExpensesValue.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
        binding.tvTotalExpensesValue.transformationMethod = PasswordTransformationMethod()
        binding.tvTotalExpensesValue.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_visibility_off_24, 0)
        setUpListeners()

        return rootView
    }

    private fun setUpListeners(){
        binding.tvTotalExpensesValue.setOnClickListener {
            viewModel.ShowHideValue(binding.tvTotalExpensesValue)
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        getAvailableNow()
        getTotalExpense()
        getMonthExpense()
        initMonthExpenseChart()
        initAvailableNowChart()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getAvailableNow(){
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val availableNow = viewModel.getAvailableNow(viewModel.getCurrentlyDate()).await()
                if(availableNow == "---"){
                    binding.tvAvailableThisMonthValue.text = availableNow
                } else if(availableNow.substring(2,7).replace(",",".").toFloat() < 0){
                    val myColor = ContextCompat.getColor(requireContext(), R.color.red)
                    binding.tvAvailableThisMonthValue.setTextColor(myColor)
                    binding.tvAvailableThisMonthValue.text = availableNow
                } else {
                    binding.tvAvailableThisMonthValue.text = availableNow
                }
            }catch (exception:Exception){}
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun getTotalExpense(){
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val totalExpense = viewModel.getTotalExpense().await()
                binding.tvTotalExpensesValue.text = totalExpense
            }catch (exception:Exception){
            } }
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
    private fun initMonthExpenseChart() = lifecycleScope.launch{
        val pieChart = binding.pcMonthExpense
        var holeColor = 1

        val monthExpenseValue = viewModel.getMonthExpense(viewModel.getCurrentlyDate()).await()
        var monthExpenseValueFormatted = 0f
        if(monthExpenseValue != "---"){
            monthExpenseValueFormatted = monthExpenseValue.replace("R$","").replace(",00","").toFloat()
        }
        var monthExpenseColor = ""
        val availableNowValue = viewModel.getAvailableNow(viewModel.getCurrentlyDate()).await()
        var availableNowValueFormatted = 1f
        if(availableNowValue != "---"){
            availableNowValueFormatted = availableNowValue.replace("R$","").replace(",00","").toFloat()
        }

        val budget = monthExpenseValueFormatted + availableNowValueFormatted

        // Defining color of availableNow part
        if(monthExpenseValueFormatted <= (budget/2)){
            monthExpenseColor = "#19d14e" // Green
        }else if(monthExpenseValueFormatted <= (budget*0.85)){
            monthExpenseColor = "#ebe23b" // Yellow
        }else if(monthExpenseValueFormatted > (budget*0.85)){
            monthExpenseColor = "#ed2b15" // Red
        }

        // Defining chart insede hole color
        when (context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                holeColor = Color.rgb(104,110,106)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                holeColor = Color.WHITE
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }

        // Create a entries list for Pie Chart
        val entries = mutableListOf<PieEntry>()
        entries.add(PieEntry(monthExpenseValueFormatted))
        entries.add(PieEntry(availableNowValueFormatted))

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
    private fun initAvailableNowChart() = lifecycleScope.launch{
        val pieChart = binding.pcAvailableNow
        var holeColor = 1

        val monthExpenseValue = viewModel.getMonthExpense(viewModel.getCurrentlyDate()).await()
        var monthExpenseValueFormatted = 0f
        if(monthExpenseValue != "---"){
            monthExpenseValueFormatted = monthExpenseValue.replace("R$","").replace(",00","").toFloat()
        }
        val availableNowValue = viewModel.getAvailableNow(viewModel.getCurrentlyDate()).await()
        var availableNowValueFormatted = 1f
        if(availableNowValue != "---"){
            availableNowValueFormatted = availableNowValue.replace("R$","").replace(",00","").toFloat()
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
                holeColor = Color.rgb(104,110,106)
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                holeColor = Color.WHITE
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }

        // Create a entries list for Pie Chart
        val entries = mutableListOf<PieEntry>()
        entries.add(PieEntry(availableNowValueFormatted))
        entries.add(PieEntry(monthExpenseValueFormatted))

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