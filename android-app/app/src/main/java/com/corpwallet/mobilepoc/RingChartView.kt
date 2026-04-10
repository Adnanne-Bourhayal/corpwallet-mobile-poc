package com.corpwallet.mobilepoc

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class RingChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val rect = RectF()

    private var values = listOf(40f, 20f, 8f)
    private val colors = listOf(
        Color.parseColor("#4F7CFF"),
        Color.parseColor("#2E4CB8"),
        Color.parseColor("#1C2E6A")
    )

    fun setData(newValues: List<Float>) {
        values = newValues
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val size = Math.min(width, height)

        val strokeWidth = size * 0.12f
        val padding = strokeWidth

        rect.set(
            padding,
            padding,
            size - padding,
            size - padding
        )

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = strokeWidth
        paint.strokeCap = Paint.Cap.ROUND

        var startAngle = -90f
        val total = values.sum()

        values.forEachIndexed { index, value ->
            val sweep = (value / total) * 360f
            paint.color = colors[index % colors.size]
            canvas.drawArc(rect, startAngle, sweep, false, paint)
            startAngle += sweep
        }
    }
}
