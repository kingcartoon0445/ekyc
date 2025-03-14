package com.gtel.ekyc.fragments

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.TorchState
import androidx.camera.core.UseCase
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import com.gtel.ekyc.R
import com.gtel.ekyc.common.CoreConstant
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.common.internal.ImageUtils
import vn.jth.xverifysdk.ekyc.core.EkycUtils.fixOrientation
import java.io.File

abstract class CameraAdvanceFragment:androidx.fragment.app.Fragment(), ActivityCompat.OnRequestPermissionsResultCallback  {

    abstract val cameraView: PreviewView
    private var cameraProvider: ProcessCameraProvider?=null
    var camera: Camera?=null
        private set
    private var cameraSelector:CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    abstract val imageAnalyzer:ImageAnalysis?
    private var imageCapture:ImageCapture?=null
    private var useCases = mutableSetOf<UseCase>()


    //==============================================================================================
    // region CAMERA
    //==============================================================================================
    @OptIn(ExperimentalGetImage::class)
    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            imageAnalyzer?.clearAnalyzer()
            useCases.clear()
            // Tạo Preview UseCase
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(cameraView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder()
                .setTargetResolution(Size(1280, 720))
                .build()

            imageAnalyzer?.let { useCases.add(it) }
            useCases.add(preview)
            useCases.add(imageCapture!!)

            try {
                cameraProvider!!.unbindAll()
                camera = cameraProvider!!.bindToLifecycle(this, cameraSelector, *useCases.toTypedArray())

            } catch (exc: Exception) {
                Log.e(TAG, "Not init camera.", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    fun takePicture(file: File?, callBack: Consumer<Bitmap>) {
        file?.let { fileResult ->
            captureImage(fileResult) { filePath ->
                if(filePath!=null){
                    val bitmapScaleDown = scaleDown(BitmapFactory.decodeFile(filePath), 1000f, true)
                    val bitmap = fixOrientation(bitmapScaleDown, filePath)
                    if (bitmap != null) {
                        callBack.accept(bitmap)
                    } else {
                        CoreConstant.showAlertDialog(requireContext(), "Capture image failure",CoreConstant.DialogType.ERROR)
                    }
                }
            }
        }

    }

    fun scaleDown(realImage: Bitmap, maxImageSize: Float, filter: Boolean): Bitmap {
        val ratio = Math.min(maxImageSize / realImage.width, maxImageSize / realImage.height)
        val width = Math.round(ratio * realImage.width)
        val height = Math.round(ratio * realImage.height)
        return Bitmap.createScaledBitmap(realImage, width, height, filter)
    }

    private fun captureImage(file: File, onSave: Consumer<String?>) {
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()
        imageCapture?.takePicture(
            outputFileOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    onSave.accept(file.absolutePath)
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, exception.message ?: "Unknown error")
                    onSave.accept(null)
                }
            }
        )
    }

    fun unbindAll(){
        cameraProvider?.unbindAll()
    }

    fun bindAll(){
        try {
            imageAnalyzer?.clearAnalyzer()
            useCases.clear()
            imageAnalyzer?.let { useCases.add(it) }

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(cameraView.surfaceProvider)
            }
            imageCapture = ImageCapture.Builder()
                .setTargetResolution(Size(1280, 720))
                .build()
            useCases.add(preview)
            useCases.add(imageCapture!!)

            cameraProvider!!.unbindAll()
            camera = cameraProvider!!.bindToLifecycle(this, cameraSelector, *useCases.toTypedArray())

        } catch (exc: Exception) {
            Log.e(TAG, "Not init camera.", exc)
        }
    }

    fun toggleFlash() {
        val hasFlash = camera?.cameraInfo?.hasFlashUnit() ?: false

        if (hasFlash) {
            val isTorchOn = camera?.cameraInfo?.torchState?.value == TorchState.ON
            camera?.cameraControl?.enableTorch(!isTorchOn)
        } else {
            Log.d("Camera", "Flash không được hỗ trợ trên thiết bị này.")
        }
    }


    fun setZoomRatioCamera(zoomLevel:Float): ListenableFuture<Void>? {
      return camera?.cameraControl?.setZoomRatio(zoomLevel)
    }

    //==============================================================================================
    // endregion CAMERA
    //==============================================================================================


    ////////////////////////////////////////////////////////////////////////////////////////
    //
    //        Permissions
    //
    ////////////////////////////////////////////////////////////////////////////////////////

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }
    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                showErrorCameraPermissionDenied()
            } else {
                startCamera()
            }
        }
    protected fun showErrorCameraPermissionDenied() {
        ErrorDialog.newInstance(getString(R.string.permission_camera_rationale)).show(childFragmentManager, FRAGMENT_DIALOG)
    }


    ////////////////////////////////////////////////////////////////////////////////////////
    //  Dialogs UI
    ////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Shows a [Toast] on the UI thread.
     *
     * @param text The message to show
     */
    private fun showToast(text: String) {
        val activity = activity
        activity?.runOnUiThread { Toast.makeText(activity, text, Toast.LENGTH_SHORT).show() }
    }

    /**
     * Shows an error message dialog.
     */
    class ErrorDialog : androidx.fragment.app.DialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val activity = activity
            return AlertDialog.Builder(activity)
                .setMessage(requireArguments().getString(ARG_MESSAGE))
                .setPositiveButton(android.R.string.ok) { dialogInterface, i -> activity!!.finish() }
                .create()
        }

        companion object {

            private val ARG_MESSAGE = "message"

            fun newInstance(message: String): ErrorDialog {
                val dialog = ErrorDialog()
                val args = Bundle()
                args.putString(ARG_MESSAGE, message)
                dialog.arguments = args
                return dialog
            }
        }

    }


    override fun onDestroyView() {

        imageCapture = null
        cameraProvider = null
        super.onDestroyView()
    }

    override fun onPause() {
        imageAnalyzer?.clearAnalyzer()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }
    }

    companion object {

        /**
         * Tag for the [Log].
         */
        private val TAG = CameraFragment::class.java.simpleName

        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private val FRAGMENT_DIALOG = TAG

    }
}