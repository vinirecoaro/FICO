package com.example.fico.model

import com.github.mikephil.charting.data.BarEntry

data class BarChartParams(
    val entries: ArrayList<BarEntry>,
    val xBarLabels: Set<String>,
    val yBarLabels: Set<String>
){
    companion object {
        fun empty(): BarChartParams {
            return BarChartParams(
                entries = arrayListOf(),
                xBarLabels = emptySet(),
                yBarLabels = emptySet()
            )
        }
    }
}