package com.example.fico.utils.custom_component

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.buffer.BarBuffer
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.renderer.BarChartRenderer
import com.github.mikephil.charting.utils.ViewPortHandler

class RoundedBarChartRenderer(
    chart: BarDataProvider,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler
) : BarChartRenderer(chart, animator, viewPortHandler) {

    override fun drawDataSet(c: Canvas, dataSet: IBarDataSet, index: Int) {
        val trans = mChart.getTransformer(dataSet.axisDependency)

        // Obter a largura configurada no barData
        val barWidth = mChart.barData.barWidth / 2f * 200

        if(mBarBuffers.isEmpty()){
            return
        }

        // Pegue o buffer específico para o dataset em questão
        val buffer = mBarBuffers[index]

        buffer.setPhases(mAnimator.phaseX, mAnimator.phaseY)
        buffer.setInverted(mChart.isInverted(dataSet.axisDependency))
        buffer.feed(dataSet) // Preenche os dados no buffer

        trans.pointValuesToPixel(buffer.buffer)

        mRenderPaint.color = dataSet.color // Configurar a cor do dataset

        for (j in 0 until buffer.buffer.size step 4) {
            // Calcula a largura customizada da barra
            val centerX = (buffer.buffer[j] + buffer.buffer[j + 2]) / 2f
            val left = centerX - barWidth
            val right = centerX + barWidth
            val top = buffer.buffer[j + 1]
            val bottom = buffer.buffer[j + 3]

            // Criar forma arredondada para a barra
            val rectF = RectF(left, top, right, bottom)

            // Definir cantos arredondados (raio)
            c.drawRoundRect(rectF, 40f, 40f, mRenderPaint)
        }
    }

    override fun drawValue(
        c: Canvas,
        valueText: String,
        x: Float,
        y: Float,
        color: Int
    ) {
        val adjustedY = y - 15f // Ajuste para aumentar o espaçamento. Pode ser positivo ou negativo.
        super.drawValue(c, valueText, x, adjustedY, color)
    }
}

