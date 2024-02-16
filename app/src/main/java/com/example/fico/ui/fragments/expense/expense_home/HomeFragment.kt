package com.example.fico.ui.fragments.expense.expense_home

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.fico.R
import com.example.fico.databinding.FragmentHomeBinding
import com.example.fico.ui.viewmodel.HomeViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.NumberFormat

class HomeFragment : Fragment(){

    private var _binding : FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<HomeViewModel>()
    private var barChartEntries : ArrayList<BarEntry> = arrayListOf()
    private var barChartMonthLabels : MutableSet<String> = mutableSetOf()
    private var barChartExpenseLabels : MutableSet<String> = mutableSetOf()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val rootView = binding.root

        binding.tvTotalExpensesValue.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
        binding.tvTotalExpensesValue.transformationMethod = PasswordTransformationMethod()
        binding.tvTotalExpensesValue.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_visibility_off_24, 0)

        initExpenseEachMonthChartEmpty()
        setUpListeners()
        viewModel.getInfoPerMonth()

        if(barChartEntries.isNotEmpty()){
            initExpenseEachMonthChart()
        }

        return rootView
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpListeners(){
        binding.tvTotalExpensesValue.setOnClickListener {
            viewModel.ShowHideValue(binding.tvTotalExpensesValue)
        }
        viewModel.infoPerMonthLiveData.observe(viewLifecycleOwner){ infoPerMonthList ->
            var i = 0f
            for (infoPerMonth in infoPerMonthList){
                val monthExpense = infoPerMonth.monthExpense.toFloat()
                barChartEntries.add(BarEntry(i, monthExpense))
                i += 1f
            }
            viewModel.formatInfoPerMonthToLabel()
        }
        viewModel.infoPerMonthLabelLiveData.observe(viewLifecycleOwner){ infoPerMonthLabelList ->
            for(infoPerMonthLabel in infoPerMonthLabelList){
                barChartMonthLabels.add(infoPerMonthLabel.date)
                barChartExpenseLabels.add(infoPerMonthLabel.monthExpense)
            }
            initExpenseEachMonthChart()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        getTotalExpense()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun getTotalExpense(){
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val totalExpense = viewModel.getTotalExpense().await()
                binding.tvTotalExpensesValue.text = totalExpense
            }catch (exception:Exception){
            }
        }
    }

    private fun initCombinedChart() {
        val combinedChart = binding.ccExpenseEachMonth

        val barEntries = listOf(
            BarEntry(0f, 10f),
            BarEntry(1f, 20f),
            BarEntry(2f, 30f),
            BarEntry(3f, 10f),
            BarEntry(4f, 20f),
            BarEntry(5f, 30f)
            // Adicione mais entradas de acordo com os seus dados
        )

        val barDataSet = BarDataSet(barEntries, "Data")
        barDataSet.color = Color.BLUE

        val barData = BarData(barDataSet)

        val xAxis = combinedChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.valueFormatter = IndexAxisValueFormatter(arrayOf("Data1", "Data2", "Data3", "Data4", "Data5", "Data6" ))

        combinedChart.axisRight.isEnabled = false
        combinedChart.description.isEnabled = false

        combinedChart.setDrawGridBackground(false)
        combinedChart.setDrawBarShadow(false)
        combinedChart.isHighlightFullBarEnabled = false

        val combinedData = CombinedData()
        combinedData.setData(barData)

        combinedChart.data = combinedData
        combinedChart.setVisibleXRangeMaximum(3f) // Defina o número máximo de barras visíveis
        combinedChart.moveViewToX(0f) // Move o gráfico para a posição inicial

        combinedChart.invalidate()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initExpenseEachMonthChart(){
        val barChart = binding.bcExpenseEachMonth

        // Crie um conjunto de dados com a lista de entradas
        val dataSet = BarDataSet(barChartEntries, "Label") // "Label" é o nome da legenda
        //dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 12f
        dataSet.color = Color.BLUE
        // Personalize os valores que aparecem acima das barras
        val valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return NumberFormat.getCurrencyInstance().format(value) // Formate o valor como desejar
            }
        }
        dataSet.valueFormatter = valueFormatter

        // Crie um objeto BarData e defina o conjunto de dados
        val barData = BarData(dataSet)

        // Defina os dados para o gráfico de barras
        barChart.data = barData
        barChart.legend.isEnabled = false
        barChart.description.isEnabled = false
        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawLabels(true)
        xAxis.granularity = 1f
        xAxis.valueFormatter = IndexAxisValueFormatter(barChartMonthLabels)

        // Configure o espaçamento entre as barras
        barData.barWidth = 0.35f

        barChart.setVisibleXRangeMaximum(3f)
        val currentDateIndex = viewModel.getCurrentDatePositionBarChart().toFloat()
        barChart.moveViewToX(currentDateIndex)

        // Atualize o gráfico
        barChart.invalidate()

    }

    private fun initExpenseEachMonthChartEmpty(){
        val barChart = binding.bcExpenseEachMonth

        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, 0f))
        entries.add(BarEntry(0f, 0f))
        entries.add(BarEntry(0f, 0f))
        entries.add(BarEntry(0f, 0f))
        entries.add(BarEntry(0f, 0f))

        // Crie um conjunto de dados com a lista de entradas
        val dataSet = BarDataSet(entries, "Label") // "Label" é o nome da legenda
        //dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 12f
        dataSet.color = Color.BLUE
        dataSet.setDrawValues(false)

        // Crie um objeto BarData e defina o conjunto de dados
        val barData = BarData(dataSet)

        // Configure o espaçamento entre as barras
        barData.barWidth = 0.5f

        // Defina os dados para o gráfico de barras
        barChart.data = barData
        barChart.legend.isEnabled = false
        barChart.description.isEnabled = false
        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawLabels(false)

        // Atualize o gráfico
        barChart.invalidate()

    }
}