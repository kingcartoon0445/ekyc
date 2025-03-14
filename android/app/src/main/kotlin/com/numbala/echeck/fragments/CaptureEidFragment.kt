package com.gtel.ekyc.fragments

import android.graphics.Bitmap
import android.graphics.Outline
import android.os.Bundle
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.Toast
import androidx.camera.core.ImageAnalysis
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.gtel.ekyc.R
import com.gtel.ekyc.databinding.FragmentCaptureEidBinding
import com.gtel.ekyc.vision.qrcode.CardDetector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class CaptureEidFragment(private val typeImage:String) : CameraAdvanceFragment() {

    private lateinit var _binding: FragmentCaptureEidBinding
    private val binding get() = _binding
    private var isAutoCapture = false
    private var bitmapImage:Bitmap?=null
    private var runnableHandleImage: Runnable? = null
    private var isCaptureSuccess = false
    private var timeoutJob: Job? = null
    private lateinit var cardDetector: CardDetector

    fun setHandleImageCallBack(runnable: Runnable) {
        this.runnableHandleImage = runnable
    }

    private val cardListener = object : CardDetector.CardDetectorListener{
        override fun onSuccess(bitmap: Bitmap) {
            takePictureSuccess(bitmap)
        }

        override fun onFailure(message: String) {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCaptureEidBinding.inflate(inflater,container,false)
        activity?.window?.statusBarColor = ContextCompat.getColor(requireContext(), R.color.grey_2)
        cardDetector = CardDetector(requireContext(),cardListener)

        if(typeImage == "FRONT"){
            binding.tvTypeCapture.text = getString(R.string.label_front_id_card)
            binding.txtInstruction.text =getString(R.string.label_guide_please_put_in_eid_in_frame)
        }else{
            binding.tvTypeCapture.text = getString(R.string.label_back_id_card)
            binding.txtInstruction.text =getString(R.string.label_guide_please_put_in_back_eid_in_frame)
        }

        binding.cameraPreview.clipToOutline = true
        binding.cameraPreview.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View?, outline: Outline?) {
                view?.let {
                    outline?.setRoundRect(0, 0, it.width, (view.height), 20F)
                }
            }
        }
        setListener()
        return binding.root
    }


    override val cameraView: PreviewView
        get() {
            return binding.cameraPreview
        }

    override val imageAnalyzer: ImageAnalysis?
        get() { return ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
            .setTargetResolution(Size(600, 600))
            .build().apply {
                setAnalyzer(ContextCompat.getMainExecutor(requireContext()),cardDetector.cardAnalyzer)
            }
        }


    private fun setListener(){
        binding.takePictureAgain.setOnClickListener {
            bindAll()
            reTakePhoto()
        }



        binding.autoShootToggle.setOnCheckedChangeListener { compoundButton, isChecked ->
            isAutoCapture = isChecked
            reTakePhoto()
        }

        binding.btnCapture.setOnClickListener {
            takePicture(File(requireActivity().cacheDir, "${System.currentTimeMillis()}.jpg")){
                takePictureSuccess(it)
            }

        }


        binding.btnConfirm.setOnClickListener {
            if(bitmapImage != null) {
                runnableHandleImage?.run()
            } else {
                captureImageFailure()
            }
        }
    }

    private fun reTakePhoto(){
        isCaptureSuccess = false
        binding.imgPreview.setImageBitmap(null)
        binding.btnCapture.visibility = View.VISIBLE
        binding.optionContainer.visibility = View.GONE
        binding.autoShootContainer.visibility = View.VISIBLE
        binding.icStatus.visibility = View.GONE
        cardDetector.setAllowDetect(isAutoCapture)
        if(isAutoCapture){
            timeoutJob?.cancel()
            timeoutJob = CoroutineScope(Dispatchers.Main).launch {
                delay(6000)
                if (!isCaptureSuccess && context!=null && isAdded) {
                    Toast.makeText(context, getString(R.string.not_detect_card), Toast.LENGTH_SHORT)
                        .show()
                }
            }
            isAutoCapture = true
            binding.txtInstruction.text = if(typeImage == "FRONT")getString(R.string.label_guide_capture_id_card) else getString(R.string.label_guide_capture_back_id_card)
            binding.btnCapture.visibility = View.GONE
        }else{
            isAutoCapture = false
            binding.txtInstruction.text =if(typeImage == "FRONT") getString(R.string.label_guide_please_put_in_eid_in_frame) else getString(R.string.label_guide_please_put_in_back_eid_in_frame)
            binding.btnCapture.visibility = View.VISIBLE
        }
    }


    private fun takePictureSuccess(bitmap: Bitmap){
        bitmapImage = bitmap
        binding.imgPreview.setImageBitmap(bitmap)
        timeoutJob?.cancel()
        isCaptureSuccess = true
        cardDetector.setAllowDetect(false)
        binding.btnCapture.visibility = View.GONE
        binding.optionContainer.visibility = View.VISIBLE
        binding.txtInstruction.text = getString(R.string.label_take_photo_success)
        binding.autoShootContainer.visibility = View.GONE
        binding.icStatus.visibility = View.VISIBLE
        binding.icStatus.setImageResource(R.drawable.ic_checkmark)
        binding.icStatus.imageTintList =
            ContextCompat.getColorStateList(requireContext(), R.color.success)
        unbindAll()
    }

    private fun captureImageFailure(){
        binding.txtInstruction.text = getString(R.string.label_take_photo_fail)
        binding.icStatus.visibility = View.VISIBLE
        binding.icStatus.setImageResource(R.drawable.baseline_cancel_24)
        binding.icStatus.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.failed)
        binding.optionContainer.visibility = View.VISIBLE
        binding.autoShootContainer.visibility = View.GONE
        binding.btnCapture.visibility = View.GONE
    }

    fun getImageBitmap() = bitmapImage

    override fun onResume() {
        super.onResume()
        reTakePhoto()

    }

    override fun onPause() {
        super.onPause()
        timeoutJob?.cancel()
    }

    override fun onDestroyView() {
        timeoutJob?.cancel()
        timeoutJob = null
        super.onDestroyView()
    }

    companion object{
        private val TAG = CaptureEidFragment::class.java.name
    }
}