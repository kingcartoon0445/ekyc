package com.gtel.ekyc.echeck.fragments


//import android.content.Context
//import android.graphics.*
//import android.util.AttributeSet
//import android.view.View
//
//class CameraOverlayView @JvmOverloads constructor(
//    context: Context,
//    attrs: AttributeSet? = null,
//    defStyleAttr: Int = 0
//) : View(context, attrs, defStyleAttr) {
//
//    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
//        color = Color.parseColor("#88000000") // Semi-transparent black
//    }
//
//    private val clearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
//        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
//    }
//
//    init {
//        // Force the view to render using software rendering
//        setLayerType(LAYER_TYPE_SOFTWARE, null)
//    }
//
//    override fun onDraw(canvas: Canvas) {
//        super.onDraw(canvas)
//
//        // Draw the semi-transparent background
//        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
//
//        val size = width * 0.6;
//
//        // Define the dimensions and position of the rounded rectangle cut-out
//        val left = (width / 2f) - size
//        val top = (height / 2f) - size
//        val right = (width / 2f) + size
//        val bottom = (height / 2f) + size
//        val radius = 50f
//
//        // Draw the transparent rounded rectangle
//        val rect = RectF(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
//        canvas.drawRoundRect(rect, radius, radius, clearPaint)
//    }
//}


import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.util.DisplayMetrics
import androidx.annotation.RequiresApi


@RequiresApi(Build.VERSION_CODES.R)
class CameraOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Default values for box width, height, and corner radius
    var boxWidth: Float
    var boxHeight: Float
    var cornerRadius = 20f

    init {
        // Get screen width
        val metrics = DisplayMetrics()
        val display = context.display
        display?.getRealMetrics(metrics)
        val screenWidth = metrics.widthPixels

        // Calculate 60% of screen width
        boxWidth = (screenWidth * 0.7f) - 50
        boxHeight = boxWidth // To keep it a square

        // Force the view to render using software rendering
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#88000000") // Semi-transparent black
    }

    private val clearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw the semi-transparent background
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        // Calculate the position of the cut-out box (centered)
        val left = (width / 2f) - (boxWidth / 2f)
        val top = (height / 2f) - (boxHeight / 2f)
        val right = (width / 2f) + (boxWidth / 2f)
        val bottom = (height / 2f) + (boxHeight / 2f)

        // Draw the transparent rounded rectangle
        val rect = RectF(left, top, right, bottom)
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, clearPaint)
    }
}

