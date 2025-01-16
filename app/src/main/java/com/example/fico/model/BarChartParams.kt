package com.example.fico.model

import com.github.mikephil.charting.data.BarEntry

data class BarChartParams(
    val entries: ArrayList<BarEntry>,
    val xBarLabels: Set<String>,
    val yBarLabels: Set<String>
)