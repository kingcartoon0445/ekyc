package com.gtel.ekyc.vision.qrcode

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

/**
 * Class handle detect and parser QRCODE
 * * If not transmit special data to constructor, default SDK will detect all type QRCODE
 * * With options, you can set zoom camera.
 * ```kotlin
 * private var options = BarcodeScannerOptions.Builder()
 *         .enableAllPotentialBarcodes()
 *         .setBarcodeFormats(
 *             Barcode.FORMAT_QR_CODE
 *         )
 * options.setZoomSuggestionOptions(ZoomSuggestionOptions.Builder(zoomCallback).build())
 * scanner = BarcodeScanning.getClient(options.build())
 * ```
 */
class QRCodeAndBarcodeAnalyzer(private var options: BarcodeScannerOptions?=null): ImageAnalysis.Analyzer {

    private var barcodeScanner = if(options==null){
        val default = BarcodeScannerOptions.Builder()
            .enableAllPotentialBarcodes()
            .setBarcodeFormats(
                Barcode.FORMAT_ALL_FORMATS
            )
        BarcodeScanning.getClient(default.build())
    }else {
        BarcodeScanning.getClient(options!!)
    }

    @OptIn(ExperimentalGetImage::class)
    private fun handleImage(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val result = barcodeScanner.process(image)
                .addOnSuccessListener { barcodes ->
                    processBarcodes(barcodes)
                }
                .addOnFailureListener {
                    Log.e(TAG, "Fail: ${it.message}")
                }.addOnCompleteListener {
                    imageProxy.close()
                    mediaImage.close()
                }
        }
    }

    override fun analyze(image: ImageProxy) {
        handleImage(image)
    }

    private fun processBarcodes(barcodes: List<Barcode>) {
        if (barcodes.isNotEmpty()) {
            QRCodeFacade.broadcastEvent(barcodes);
        }
    }

    companion object {
        private val TAG = QRCodeAndBarcodeAnalyzer::class.java.name
    }
}
