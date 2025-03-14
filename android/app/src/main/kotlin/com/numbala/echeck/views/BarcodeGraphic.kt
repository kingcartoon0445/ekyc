/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gtel.ekyc.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.graphics.Paint.Style
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.gtel.ekyc.R

class BarcodeGraphic @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private var boundingBox: Rect? = null
    private var previewWidth = 0
    private var previewHeight = 0


    private val boxCornerRadius: Float =
        context.resources.getDimensionPixelOffset(com.intuit.sdp.R.dimen._6sdp).toFloat()


    private val pathPaint: Paint = Paint().apply {
        color =  ContextCompat.getColor(context, R.color.colorPrimary)
        style = Style.STROKE
        strokeWidth = context.resources.getDimensionPixelOffset(com.intuit.sdp.R.dimen._2sdp).toFloat()
        pathEffect = CornerPathEffect(boxCornerRadius)
    }

    fun updateBoundingBox(box: Rect?, previewWidth: Int, previewHeight: Int) {
        if (box == null) {
            postInvalidate()
            return
        }
        boundingBox = box
        this.previewWidth = previewWidth
        this.previewHeight = previewHeight
        invalidate()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        boundingBox?.let { box ->
            val scaleX = width.toFloat() / previewWidth
            val scaleY = height.toFloat() / previewHeight

            val adjustedBox = Rect(
                (box.left * scaleX).toInt(),
                (box.top * scaleY).toInt(),
                (box.right * scaleX).toInt(),
                (box.bottom * scaleY).toInt()
            )

            canvas.drawRect(adjustedBox, pathPaint)
        }
    }
}


