package com.example.fico.utils.custom_component.chart.renderer

import android.graphics.Canvas
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.renderer.LineChartRenderer
import com.github.mikephil.charting.utils.ViewPortHandler

class CustomLineChartRenderer(
    chart: LineChart,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler,
    var shouldDrawHighlightedValues: Boolean = true
) : LineChartRenderer(chart, animator, viewPortHandler) {

    override fun drawValues(c: Canvas) {

        if (!shouldDrawHighlightedValues) return

        val dataSets = mChart.lineData.dataSets

        for (i in dataSets.indices) {
            val dataSet = dataSets[i]

            if (!shouldDrawValues(dataSet) || dataSet.entryCount == 0) continue

            applyValueTextStyle(dataSet)

            val trans = mChart.getTransformer(dataSet.axisDependency)

            val positions = trans.generateTransformedValuesLine(dataSet, mAnimator.phaseX, mAnimator.phaseY, 0, dataSet.entryCount-1)

            val count = minOf(positions.size / 2, dataSet.entryCount)

            for (j in 0 until count) {
                val index = j * 2
                val x = positions[index]
                val y = positions[index + 1]

                if (!mViewPortHandler.isInBoundsRight(x)) break
                if (!mViewPortHandler.isInBoundsLeft(x) || !mViewPortHandler.isInBoundsY(y)) continue

                val entry = dataSet.getEntryForIndex(j)
                val yValue = entry.y
                val valueOffset = if (yValue >= 0) -20f else 40f

                val value = dataSet.valueFormatter.getPointLabel(entry)
                c.drawText(
                    value,
                    x,
                    y + valueOffset,
                    mValuePaint
                )
            }
        }
    }


}
