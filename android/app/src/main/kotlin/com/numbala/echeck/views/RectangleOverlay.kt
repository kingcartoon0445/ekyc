package com.gtel.ekyc.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.graphics.Paint.Style
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.gtel.ekyc.R


class RectangleOverlay(context: Context, attrs: AttributeSet?): View(context,attrs) {
    companion object{
        private val TAG = RectangleOverlay::class.java.simpleName
    }

    private val boxPaint: Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.white)
        style = Style.STROKE
        strokeWidth = context.resources.getDimensionPixelOffset(com.intuit.sdp.R.dimen._2sdp).toFloat()
    }

    private val scrimPaint: Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.black_50)
    }

    private val eraserPaint: Paint = Paint().apply {
        strokeWidth = boxPaint.strokeWidth
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private val boxCornerRadius: Float =
        context.resources.getDimensionPixelOffset(com.intuit.sdp.R.dimen._10sdp).toFloat()

    val pathPaint: Paint = Paint().apply {
        color = Color.WHITE
        style = Style.STROKE
        strokeWidth = boxPaint.strokeWidth
        pathEffect = CornerPathEffect(boxCornerRadius)
    }

    private val boxRect = RectF()


    fun getOverLay(): RectF {
        return boxRect
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX = width / 2f
        val centerY = height / 2f
        val squareSize = 750f // Adjust this size for a smaller or larger square

        boxRect.set(
            centerX - squareSize / 2f, // Left
            centerY - squareSize / 2f, // Top
            centerX + squareSize / 2f, // Right
            centerY + squareSize / 2f  // Bottom
        )

        // Draws the dark background scrim and leaves the box area clear.
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), scrimPaint)
        // As the stroke is always centered, so erase twice with FILL and STROKE respectively to clear
        // all area that the box rect would occupy.
        eraserPaint.style = Style.FILL
        canvas.drawRoundRect(boxRect, boxCornerRadius, boxCornerRadius, eraserPaint)
        eraserPaint.style = Style.STROKE
        canvas.drawRoundRect(boxRect, boxCornerRadius, boxCornerRadius, eraserPaint)
        // Draws the box.
        canvas.drawRoundRect(boxRect, boxCornerRadius, boxCornerRadius, boxPaint)
    }
}
