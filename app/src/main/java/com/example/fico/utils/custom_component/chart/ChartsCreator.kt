package com.example.fico.utils.custom_component.chart

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.util.TypedValue
import android.view.MotionEvent
import androidx.core.content.ContextCompat
import com.example.fico.R
import com.example.fico.utils.custom_component.chart.renderer.CustomLineChartRenderer
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import java.text.NumberFormat

class ChartsCreator {
    companion object{

        fun lineChart(
            activity : Activity,
            context : Context,
            lineChart : LineChart,
            xyEntries : List<Entry>,
            xLabels : List<String>,
            onChartSingleTapped : () -> Boolean
        ){

            val colorOnSecondary = getColorOnSecondary(activity,context)

            val dataSet = LineDataSet(xyEntries, "Net Income").apply {
                color = Color.BLUE
                setDrawFilled(true)
                fillColor = Color.BLUE
                fillAlpha = 50
                valueTextColor = colorOnSecondary
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
            lineChart.data = lineData

            //Custom renderer for line chart
            lineChart.renderer = CustomLineChartRenderer(
                lineChart,
                lineChart.animator,
                lineChart.viewPortHandler
            )

            lineChart.apply {
                isAutoScaleMinMaxEnabled = true
                setPinchZoom(true)
                setVisibleXRangeMaximum(4f)
                description.isEnabled = false
                axisRight.isEnabled = false
                legend.isEnabled = false
            }

            //Axis X config (months)
            lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(xLabels)
            lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
            lineChart.xAxis.granularity = 1f
            lineChart.xAxis.setDrawGridLines(false)
            lineChart.xAxis.textColor = colorOnSecondary
            lineChart.isDragXEnabled = true

            // Y Axis
            lineChart.axisLeft.setDrawGridLines(false)
            lineChart.axisLeft.textColor = colorOnSecondary
            lineChart.axisRight.isEnabled = false

            // Remove description and legend
            lineChart.description.isEnabled = false
            lineChart.legend.isEnabled = false
            lineChart.setDragEnabled(true)
            lineChart.setScaleEnabled(true) // allow zoom (horizontal and vertical)
            lineChart.isHighlightPerDragEnabled = true

            lineChart.animateX(1000)

            lineChart.onChartGestureListener = object : OnChartGestureListener {
                override fun onChartSingleTapped(me: MotionEvent?) {
                    // Change Y axis values visibility
                    (lineChart.renderer as? CustomLineChartRenderer)?.shouldDrawHighlightedValues = onChartSingleTapped()
                    lineChart.invalidate()
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

        private fun getColorOnSecondary(
            activity : Activity,
            context : Context
        ) : Int{
            val typedValue = TypedValue()
            val theme: Resources.Theme = activity.theme
            theme.resolveAttribute(com.google.android.material.R.attr.colorOnSecondary, typedValue, true)
            val colorOnSecondary = ContextCompat.getColor(context, typedValue.resourceId)
            return colorOnSecondary
        }
    }
}