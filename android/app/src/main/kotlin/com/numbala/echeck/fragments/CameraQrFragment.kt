package com.gtel.ekyc.fragments

import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.ImageAnalysis
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.gtel.ekyc.R
import com.gtel.ekyc.databinding.FragmentCameraQrBinding
import com.gtel.ekyc.vision.qrcode.QRCodeAndBarcodeAnalyzer
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.ZoomSuggestionOptions
import com.google.mlkit.vision.barcode.common.Barcode

class CameraQrFragment : CameraAdvanceFragment() {
    private var binding: FragmentCameraQrBinding? = null

    private var options = BarcodeScannerOptions.Builder()
        .enableAllPotentialBarcodes()
        .setBarcodeFormats(
            Barcode.FORMAT_QR_CODE
        )


    override val imageAnalyzer: ImageAnalysis
        get() { return ImageAnalysis.Builder()
            .setTargetResolution(Size(1280, 720))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build().apply {
                val zoomCallback = ZoomSuggestionOptions.ZoomCallback { zoomLevel: Float ->
                    Log.i(TAG, "Set zoom ratio $zoomLevel")
                    val ignored = setZoomRatioCamera(zoomLevel)
                    true
                }
                options.setZoomSuggestionOptions(ZoomSuggestionOptions.Builder(zoomCallback).build())
                setAnalyzer(ContextCompat.getMainExecutor(requireContext()),
                    QRCodeAndBarcodeAnalyzer(options.build())
                )
            }
        }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCameraQrBinding.inflate(inflater, container, false)
        activity?.window?.statusBarColor = ContextCompat.getColor(requireContext(), R.color.grey_2)
        binding!!.btnBack.setOnClickListener {
            activity?.finish()
        }

        binding!!.icFlash.setOnClickListener {
            toggleFlash()
        }

        return binding?.root
    }


    override val cameraView: PreviewView
        get() {
            return binding?.cameraPreview!!
        }


    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    fun drawFrameBarcode(barcodes: List<Barcode>?){
      //  binding?.graphicOverlay?.updateBoundingBox(barcodes?.get(0)?.boundingBox,binding?.cameraPreview?.width?:0,binding?.cameraPreview?.height?:0)
    }

    companion object{
        private val TAG = CameraQrFragment::class.java.name
    }
}