package com.gtel.ekyc.vision.qrcode

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.SystemClock
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis.Analyzer
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.common.internal.ImageUtils
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.max

class CardDetector(private var context: Context, private val callback: CardDetectorListener) {
    companion object {
        private val TAG = CardDetector::class.java.name
    }

    var KEEP_CAMERA = 2000L
    private var isAllowDetect = false
    private var firstDetectedTime = AtomicLong(0L)

    /** min aspect ratio for card shape */
    var minAreaPercentage = 25
        private set

    private val objectDetector = ObjectDetection.getClient(
        ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .build()
    )


    //=========================================
    // region PUBLIC
    //============================================

    fun setAllowDetect(isAllow: Boolean) {
        isAllowDetect = isAllow
    }

    fun setTimeKeepFrame(time: Long) {
        KEEP_CAMERA = time
    }

    fun setMinAreaPercentage(minAreaPercentage: Int) {
        this.minAreaPercentage = minAreaPercentage
    }

    // endregion PUBLIC


    @OptIn(ExperimentalGetImage::class)
    private fun detector(imageProxy: ImageProxy) {
        if (!isAllowDetect) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image ?: return
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        val detectionSize = max(imageProxy.width, imageProxy.height)
        objectDetector.process(image)
            .addOnSuccessListener { detectedObjects ->
                for (detectedObject in detectedObjects) {
                    if (cardInFrame(detectedObject, detectionSize) && isCardShape(detectedObject)) {
                        onCardDetected(imageProxy)
                        break
                    }
                }
            }
            .addOnFailureListener { e ->
                callback.onFailure("Object detection failed")
                Log.e(TAG, "Object detection failed", e)
            }
            .addOnCompleteListener {
                try {
                    mediaImage.close()
                    imageProxy.close() // Đảm bảo đóng imageProxy khi hoàn thành
                } catch (e: Exception) {
                    Log.e(TAG, "Error closing resources: ${e.message}")
                }
            }
    }


    private fun onCardDetected(imageProxy: ImageProxy) {
        callback.onCardDetection()

        if (firstDetectedTime.get() == 0L) {
            firstDetectedTime.set(SystemClock.elapsedRealtime())
        }

        val currentTime = SystemClock.elapsedRealtime()
        if (currentTime - firstDetectedTime.get() >= KEEP_CAMERA) {
            val bitmap = imageProxy.toBitmap()
            val rotationBitmap = fixOrientation(
                bitmap,
                imageProxy.imageInfo.rotationDegrees
            )
            callback.onSuccess(rotationBitmap)
            firstDetectedTime.set(0L)
        }
    }

    fun fixOrientation(bitmap: Bitmap, rotation: Int) : Bitmap {
        val matrix = Matrix()
        matrix.postRotate(rotation.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun isCardShape(detectedObject: DetectedObject): Boolean {
        val boundingBox = detectedObject.boundingBox
        val width = boundingBox.width().toFloat()
        val height = boundingBox.height().toFloat()
        val aspectRatio = width / height
        val boxArea = width * height
        val screenSize = context.resources.displayMetrics.let {
            it.widthPixels.toFloat() * it.heightPixels.toFloat()
        }
        val areaPercentage = (boxArea / screenSize) * 100
        Log.w(TAG, "aspectRatio = $aspectRatio - areaPercentage = $areaPercentage")
        return (aspectRatio > 1.4 && aspectRatio < 1.7) || (areaPercentage >= minAreaPercentage)
    }

    private fun cardInFrame(detectedObject: DetectedObject, detectionSize: Int): Boolean {
        val fRect = detectedObject.boundingBox
        val fx = fRect.centerX()
        val fy = fRect.centerY()
        val gridSize = detectionSize / 6

        if (fx < gridSize * 2 || fx > gridSize * 4 || fy < gridSize * 2 || fy > gridSize * 4) {
            Log.d(TAG, "Card center out of bounds: ($fx, $fy)")
            callback.onFailure("Card center out of bounds")
            return false
        }

//        val fw = fRect.width()
//        val fh = fRect.height()

//            Log.w("Phuc","gridSize = $gridSize")
//            if (fw < gridSize * 3 || fw > gridSize * 5 || fh < gridSize * 3 || fh > gridSize *5) {
//                Log.d("Phuc", "Unexpected card size: $fw x $fh")
//                return false
//            }
        return true
    }

    val cardAnalyzer = Analyzer { imageProxy -> detector(imageProxy) }


    interface CardDetectorListener {
        fun onCardDetection() {}
        fun onSuccess(bitmap: Bitmap)
        fun onFailure(message: String)
    }

}