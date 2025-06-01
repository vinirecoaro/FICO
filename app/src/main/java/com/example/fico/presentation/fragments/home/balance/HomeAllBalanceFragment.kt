package com.example.fico.presentation.fragments.home.balance

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.example.fico.databinding.FragmentHomeAllBalanceBinding
import com.example.fico.presentation.viewmodel.HomeMonthBalanceViewModel
import com.example.fico.utils.custom_component.CustomLineChartRenderer
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import org.koin.android.ext.android.inject
import java.text.NumberFormat

class HomeAllBalanceFragment : Fragment() {

    private var _binding : FragmentHomeAllBalanceBinding? = null
    private val binding get() = _binding!!
    private val viewModel : HomeMonthBalanceViewModel by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeAllBalanceBinding.inflate(inflater, container, false)
        val rootView = binding.root

        setupListeners()

        initLineChart()

        return rootView
    }

    private fun setupListeners(){

    }

    private fun initLineChart(){

        // Dados fictícios do gráfico
        val entries = listOf(
            Entry(0f, 6000f),
            Entry(1f, 6000f),
            Entry(2f, 6200f),
            Entry(3f, 6400f),
            Entry(4f, 6200f),
            Entry(5f, 5800f),
            Entry(6f, 6000f),
            Entry(7f, 5900f),
            Entry(8f, 9500f),
            Entry(9f, 11000f),
            Entry(10f, 10000f)
        )

        val dataSet = LineDataSet(entries, "Net Income").apply {
            color = Color.BLUE
            setDrawFilled(true)
            fillColor = Color.BLUE
            fillAlpha = 50
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
        binding.lcBalancePerMonth.data = lineData

        //Custom renderer for line chart
        binding.lcBalancePerMonth.renderer = CustomLineChartRenderer(
            binding.lcBalancePerMonth,
            binding.lcBalancePerMonth.animator,
            binding.lcBalancePerMonth.viewPortHandler
        )

        binding.lcBalancePerMonth.apply {
            isAutoScaleMinMaxEnabled = true
            setPinchZoom(true)
            setVisibleXRangeMaximum(4f) // você já usa isso
            description.isEnabled = false
            axisRight.isEnabled = false
            legend.isEnabled = false
        }

        //Axis X config (months)
        val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov")
        binding.lcBalancePerMonth.xAxis.valueFormatter = IndexAxisValueFormatter(months)
        binding.lcBalancePerMonth.xAxis.position = XAxis.XAxisPosition.BOTTOM
        binding.lcBalancePerMonth.xAxis.granularity = 1f
        binding.lcBalancePerMonth.xAxis.setDrawGridLines(false)

        binding.lcBalancePerMonth.setDragEnabled(true)
        binding.lcBalancePerMonth.setScaleEnabled(true) // Permite zoom (horizontal e vertical)
        binding.lcBalancePerMonth.isScaleXEnabled = true // Zoom horizontal
        binding.lcBalancePerMonth.isScaleYEnabled = false // (opcional) desativa zoom vertical
        binding.lcBalancePerMonth.isDragXEnabled = true // Garante arrasto no eixo X
        binding.lcBalancePerMonth.isHighlightPerDragEnabled = true

        // Y Axis
        binding.lcBalancePerMonth.axisLeft.setDrawGridLines(false)
        binding.lcBalancePerMonth.axisRight.isEnabled = false

        // Remove description and legend
        binding.lcBalancePerMonth.description.isEnabled = false
        binding.lcBalancePerMonth.legend.isEnabled = false

        binding.lcBalancePerMonth.animateX(1000)
        binding.lcBalancePerMonth.invalidate()

        binding.lcBalancePerMonth.onChartGestureListener = object : OnChartGestureListener {
            override fun onChartSingleTapped(me: MotionEvent?) {
                // Change Y axis values visibility
                if(viewModel.getLineChartYAxisVisible()){
                    viewModel.setLineChartYAxisVisible(false)
                    (binding.lcBalancePerMonth.renderer as? CustomLineChartRenderer)?.shouldDrawHighlightedValues = false
                    binding.lcBalancePerMonth.invalidate()
                }else{
                    viewModel.setLineChartYAxisVisible(true)
                    (binding.lcBalancePerMonth.renderer as? CustomLineChartRenderer)?.shouldDrawHighlightedValues = true
                    binding.lcBalancePerMonth.invalidate()
                }
            }

            // Another methods that must be implemented, but can be left empty
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