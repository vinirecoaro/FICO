package com.example.fico.presentation.fragments.home

import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
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
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.text.NumberFormat

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

                        initLineChart()

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

    private fun initLineChart(){

        // Dados fictícios do gráfico
        val entries = listOf(
            Entry(0f, 7000f),
            Entry(1f, -6000f),
            Entry(2f, 6500f),
            Entry(3f, -8000f),
            Entry(4f, 7500f),
            Entry(5f, -9000f),
            Entry(6f, 12000f),
            Entry(7f, -8500f),
            Entry(8f, 9500f),
            Entry(9f, -11000f),
            Entry(10f, 10000f)
        )

        val dataSet = LineDataSet(entries, "Net Income").apply {
            color = Color.BLUE
            setDrawFilled(true)
            fillColor = Color.BLUE
            fillAlpha = 50
            setDrawValues(false)
            valueTextSize = 14f
            setDrawCircles(false)
            lineWidth = 2f
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getPointLabel(entry: Entry?): String {
                return NumberFormat.getCurrencyInstance().format(entry?.y?.toFloat())
            }
        }


        val lineData = LineData(dataSet)
        binding.lineChart.data = lineData

        // Configurações do eixo X (meses)
        val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov")
        binding.lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(months)
        binding.lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        binding.lineChart.xAxis.granularity = 1f
        binding.lineChart.xAxis.setDrawGridLines(false)

        binding.lineChart.setDragEnabled(true)
        binding.lineChart.setScaleEnabled(true) // Permite zoom (horizontal e vertical)
        binding.lineChart.isScaleXEnabled = true // Zoom horizontal
        binding.lineChart.isScaleYEnabled = false // (opcional) desativa zoom vertical
        binding.lineChart.isDragXEnabled = true // Garante arrasto no eixo X
        binding.lineChart.isHighlightPerDragEnabled = true
        binding.lineChart.setVisibleXRangeMaximum(4f) // Mostra 6 meses por vez

        // Eixo Y
        binding.lineChart.axisLeft.setDrawGridLines(false)
        binding.lineChart.axisRight.isEnabled = false

        // Remover descrição e legenda
        binding.lineChart.description.isEnabled = false
        binding.lineChart.legend.isEnabled = false

        binding.lineChart.animateX(1000)
        binding.lineChart.invalidate()

        binding.lineChart.onChartGestureListener = object : OnChartGestureListener {
            override fun onChartSingleTapped(me: MotionEvent?) {
                // Change Y axis values visibility
                if(viewModel.getLineChartYAxisVisible()){
                    viewModel.setLineChartYAxisVisible(false)
                    dataSet.setDrawValues(false)
                    binding.lineChart.invalidate()
                }else{
                    viewModel.setLineChartYAxisVisible(true)
                    dataSet.setDrawValues(true)
                    binding.lineChart.invalidate()
                }
            }

            // Outros métodos obrigatórios, mas que podem ficar vazios
            override fun onChartGestureStart(me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?) {}
            override fun onChartGestureEnd(me: MotionEvent?, lastPerformedGesture: ChartTouchListener.ChartGesture?) {}
            override fun onChartLongPressed(me: MotionEvent?) {}
            override fun onChartDoubleTapped(me: MotionEvent?) {}
            override fun onChartFling(me1: MotionEvent?, me2: MotionEvent?, velocityX: Float, velocityY: Float) {}
            override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {}
            override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {}
        }

    }
}