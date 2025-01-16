package com.example.fico.presentation.fragments.home

import android.content.res.Configuration
import android.graphics.BlurMaskFilter
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.fico.R
import com.example.fico.databinding.FragmentHomeBinding
import com.example.fico.model.BarChartParams
import com.example.fico.presentation.viewmodel.HomeViewModel
import com.example.fico.utils.custom_component.RoundedBarChartRenderer
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.text.NumberFormat

class HomeFragment : Fragment(){

    private var _binding : FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel : HomeViewModel by inject()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val rootView = binding.root

        initExpenseEachMonthChartEmpty()
        setUpListeners()

        // Blur total value field configuration
        binding.tvTotalExpensesValue.setLayerType(TextView.LAYER_TYPE_SOFTWARE, null)

        return rootView
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        viewModel.getTotalExpense()
        viewModel.getExpenseBarChartParams()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUpListeners(){

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                when(state){
                    is HomeFragmentState.Loading -> {
                        binding.clExpensePerMonth.visibility = View.GONE
                        binding.tvTotalExpenses.visibility = View.GONE
                        binding.tvTotalExpensesValue.visibility = View.GONE
                        binding.viewSpace.visibility = View.GONE
                        binding.cvInfoPerMonth.visibility = View.GONE
                        binding.ivNoInfoAvailable.visibility = View.GONE
                        binding.tvNoInfoAvailable.visibility = View.GONE
                        binding.pbExpensePerMonth.visibility = View.VISIBLE
                    }
                    is HomeFragmentState.Empty ->{
                        binding.clExpensePerMonth.visibility = View.GONE
                        binding.tvTotalExpenses.visibility = View.GONE
                        binding.tvTotalExpensesValue.visibility = View.GONE
                        binding.viewSpace.visibility = View.GONE
                        binding.cvInfoPerMonth.visibility = View.GONE
                        binding.ivNoInfoAvailable.visibility = View.VISIBLE
                        binding.tvNoInfoAvailable.visibility = View.VISIBLE
                        binding.pbExpensePerMonth.visibility = View.GONE
                    }
                    is HomeFragmentState.Error -> {

                    }
                    is HomeFragmentState.Success -> {
                        binding.clExpensePerMonth.visibility = View.VISIBLE
                        binding.tvTotalExpenses.visibility = View.VISIBLE
                        binding.tvTotalExpensesValue.visibility = View.VISIBLE
                        binding.viewSpace.visibility = View.VISIBLE
                        binding.cvInfoPerMonth.visibility = View.VISIBLE
                        binding.ivNoInfoAvailable.visibility = View.GONE
                        binding.tvNoInfoAvailable.visibility = View.GONE
                        binding.pbExpensePerMonth.visibility = View.GONE
                    }
                }
            }

        }

        binding.tvTotalExpensesValue.setOnClickListener {
            viewModel.changeBlurState()
        }

        viewModel.isBlurred.observe(viewLifecycleOwner){ state ->
            if (state) {
                val blurMaskFilter = BlurMaskFilter(30f, BlurMaskFilter.Blur.NORMAL) // Intensidade do desfoque
                binding.tvTotalExpensesValue.paint.maskFilter = blurMaskFilter
                binding.tvTotalExpensesValue.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_visibility_off_24, 0)
            } else {
                binding.tvTotalExpensesValue.paint.maskFilter = null
                binding.tvTotalExpensesValue.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_visibility_24, 0)
            }
            binding.tvTotalExpensesValue.invalidate()
        }

       /* viewModel.infoPerMonthLiveData.observe(viewLifecycleOwner){ infoPerMonthList ->
            barChartEntries.clear()
            var i = 0f
            for (infoPerMonth in infoPerMonthList){
                val monthExpense = infoPerMonth.monthExpense.toFloat()
                barChartEntries.add(BarEntry(i, monthExpense))
                i += 1f
            }
            viewModel.formatInfoPerMonthToLabel()
        }

        viewModel.infoPerMonthLabelLiveData.observe(viewLifecycleOwner){ infoPerMonthLabelList ->
            barChartMonthLabels.clear()
            barChartExpenseLabels.clear()
            for(infoPerMonthLabel in infoPerMonthLabelList){
                barChartMonthLabels.add(infoPerMonthLabel.date)
                barChartExpenseLabels.add(infoPerMonthLabel.monthExpense)
            }
            if(barChartEntries.isNotEmpty() && barChartMonthLabels.isNotEmpty() && barChartExpenseLabels.isNotEmpty() && viewModel.isFirstLoad.value!!){
                initExpenseEachMonthChart()
                viewModel.changeFirstLoadState()
            }
        }*/

        viewModel.totalExpenseLiveData.observe(viewLifecycleOwner){totalExpense ->
            binding.tvTotalExpensesValue.text = totalExpense
        }

        viewModel.expenseBarChartParams.observe(viewLifecycleOwner){ barChartParams ->
            if(barChartParams.entries.isNotEmpty()){
                initExpenseEachMonthChart(barChartParams)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initExpenseEachMonthChart(barChartParams : BarChartParams){
        val barChart = binding.bcExpenseEachMonth

        // Create a data set with entry list
        val dataSet = BarDataSet(barChartParams.entries, "Label")
        dataSet.valueTextSize = 12f
        dataSet.color = Color.BLUE

        //Customize values that appear on top of the bars
        val valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return NumberFormat.getCurrencyInstance().format(value)
            }
        }
        dataSet.valueFormatter = valueFormatter

        // Create an object BarData and define data set
        val barData = BarData(dataSet)

        // Define data to bar chart
        barChart.data = barData
        barChart.legend.isEnabled = false
        barChart.description.isEnabled = false
        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawLabels(true)
        xAxis.setDrawAxisLine(false)
        xAxis.granularity = 1f
        xAxis.valueFormatter = IndexAxisValueFormatter(barChartParams.xBarLabels)
        barChart.axisLeft.setDrawGridLines(false)
        barChart.axisRight.setDrawGridLines(false)

        // Format text color based on theme
        when (context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> {
                xAxis.textColor = Color.WHITE
                dataSet.valueTextColor = Color.WHITE
                barChart.axisLeft.textColor = Color.WHITE
                barChart.axisRight.textColor = Color.WHITE
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                xAxis.textColor = Color.BLACK
                dataSet.valueTextColor = Color.BLACK
                barChart.axisLeft.textColor = Color.BLACK
                barChart.axisRight.textColor = Color.BLACK
            }
            Configuration.UI_MODE_NIGHT_UNDEFINED -> {}
        }

        // Configure bar width
        barData.barWidth = 0.35f

        // Define number of visible bar
        barChart.setVisibleXRangeMaximum(3f)

        // Get current month position on chart and move chart to it
        val currentDateIndex = viewModel.getCurrentDatePositionBarChart().toFloat()

        barChart.moveViewToX(currentDateIndex)

        // Add animation of increasing bars
        barChart.animateY(1500, Easing.EaseInOutQuad)

        barChart.renderer = RoundedBarChartRenderer(barChart, barChart.animator, barChart.viewPortHandler)
        barChart.renderer.initBuffers()

        // Update chart
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