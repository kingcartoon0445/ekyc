package com.gtel.ekyc.activities.common

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Outline
import android.graphics.drawable.BitmapDrawable
import android.media.MediaPlayer
import android.view.View
import android.view.ViewOutlineProvider
import androidx.camera.core.CameraSelector
import androidx.camera.view.LifecycleCameraController
import com.gtel.ekyc.BuildConfig
import com.gtel.ekyc.MainActivity
import com.gtel.ekyc.R
import com.gtel.ekyc.common.BusinessType
import com.gtel.ekyc.common.CoreConstant
import com.gtel.ekyc.common.ONBOARDDATAMANAGER
import com.gtel.ekyc.databinding.ActivityLivenessBinding
import com.gtel.ekyc.fragments.FaceVerificationInstructionFragment
import com.gtel.ekyc.models.request.VerifyEidRequestModel
import vn.jth.xverifysdk.ekyc.ActiveEkycUtils
import vn.jth.xverifysdk.ekyc.EkycFaceResult
import vn.jth.xverifysdk.ekyc.EkycLivenessListener
import vn.jth.xverifysdk.ekyc.EkycVerificationMode
import vn.jth.xverifysdk.ekyc.EkycVerifyError
import vn.jth.xverifysdk.ekyc.EkycVerifyListener
import vn.jth.xverifysdk.ekyc.core.StepFace
import vn.jth.xverifysdk.network.ApiService.APISERVICE
import java.util.ArrayList

class LivenessActivity : BaseActivity(), FaceVerificationInstructionFragment.OnStartClickListener {

    private var mBinding: ActivityLivenessBinding? = null
    private var cameraController: LifecycleCameraController? = null
    private var mediaPlayer: MediaPlayer? = null

    private val faceListener: EkycLivenessListener = object : EkycLivenessListener {
        override fun onStepLeft() {
            mBinding!!.tvStepInstruction.text = getString(R.string.please_tilt_your_face_to_the_left)
        }

        override fun onStepCenter() {
            mBinding!!.tvStepInstruction.text = getString(R.string.please_look_straight)
        }

        override fun onStepRight() {
            mBinding!!.tvStepInstruction.text = getString(R.string.please_tilt_your_face_to_the_right)
        }

        override fun onMultiFace() {
            mBinding!!.tvStepInstruction.text = getString(R.string.multi_face)
        }

        override fun onNoFace() {
            mBinding!!.tvStepInstruction.text = getString(R.string.put_your_face_in_the_frame)
        }


        override fun onOpenMouthFace() {
            mBinding!!.tvStepInstruction.text = getString(R.string.please_open_your_mouth)
        }

        override fun onSmileFace() {
            mBinding!!.tvStepInstruction.text = getString(R.string.please_smile)
        }

        override fun onSadnessFace() {
            mBinding!!.tvStepInstruction.text = getString(R.string.please_sadness)

        }

        override fun onSurprisedFace() {
            mBinding!!.tvStepInstruction.text = getString(R.string.please_surprised)
        }

        override fun onNodUp() {
            mBinding!!.tvStepInstruction.text = getString(R.string.please_nod_up)

        }

        override fun onNodDown() {
            mBinding!!.tvStepInstruction.text = getString(R.string.please_nod_down)

        }
        override fun onPlaySound() {
            playSoundBeep()
        }

        override fun onFaceFar() {
        }

        override fun onFaceNear() {

        }

        override fun onStep(step: StepFace) {
            mBinding!!.tvStepInstruction.text = when(step) {
                StepFace.FACE -> getString(R.string.please_look_straight)
                StepFace.FACE_CENTER -> getString(R.string.please_look_straight)
                StepFace.LEFT -> getString(R.string.please_tilt_your_face_to_the_left)
                StepFace.RIGHT -> getString(R.string.please_tilt_your_face_to_the_right)
                StepFace.SURPRISED -> getString(R.string.please_surprised)
                StepFace.SADNESS -> getString(R.string.please_sadness)
                StepFace.SMILE -> getString(R.string.please_smile)
                StepFace.NOD_DOWN -> getString(R.string.please_nod_down)
                StepFace.NOD_UP -> getString(R.string.please_nod_up)
                StepFace.OPEN_MOUTH -> getString(R.string.please_open_your_mouth)
                StepFace.DONE -> {""}
                StepFace.FACE_FAR -> {""}
                StepFace.FACE_NEAR -> {""}
            }
        }
    }
    private val onVerifyListener: EkycVerifyListener = object : EkycVerifyListener {
        override fun onProcess() {
            showProgress()
            mBinding!!.tvStepInstruction.text = getString(R.string.nfc_verify)
        }

        override fun onFailed(error: String, capturedFace: String?, ekycVerificationMode: EkycVerificationMode, errorCodes: EkycVerifyError) {
            hideProgress()
            startCamera();
            mBinding!!.tvStepInstruction.text = getString(R.string.verify_failed_description_face)
        }


        override fun onVerifyCompleted(ekycVerificationMode: EkycVerificationMode?, verifyLiveness: Boolean, verifyFaceMatch: Boolean, capturedFace: String?, capturedSmile: String?) {
            hideProgress()
            when (ekycVerificationMode) {
                EkycVerificationMode.LIVENESS -> {
                    when (ONBOARDDATAMANAGER.businessType) {
                        BusinessType.VERIFY_EID_EKYC -> {
                            val bitmap = BitmapFactory.decodeFile(capturedFace)
                            ONBOARDDATAMANAGER.mBitmapSelfie = bitmap
                            ONBOARDDATAMANAGER.onboardFaceImagePath = capturedFace
                            ONBOARDDATAMANAGER.onboardSmileImagePath = capturedSmile
                            ONBOARDDATAMANAGER.isFaceMatch =  if(verifyFaceMatch) "1" else "0"
                            setResult(RESULT_OK)
                            finish()
                        }
                        else -> {}
                    }
                }
                EkycVerificationMode.LIVENESS_FACE_MATCHING ->{
                    // -> live face & eidFace : verifyFaceMatch
                    if (verifyFaceMatch) {
                        val bitmap = BitmapFactory.decodeFile(capturedFace)
                        ONBOARDDATAMANAGER.mBitmapSelfie = bitmap
                        ONBOARDDATAMANAGER.onboardFaceImagePath = capturedFace
                        ONBOARDDATAMANAGER.onboardSmileImagePath = capturedSmile
                        ONBOARDDATAMANAGER.isFaceMatch =  "1"
                        startActivity(Intent(this@LivenessActivity, SummaryActivity::class.java))
                    } else {
                        startCamera();
                        mBinding!!.tvStepInstruction.text = getString(R.string.verify_failed_description_face)
                    }
                }
                EkycVerificationMode.VERIFY_LIVENESS -> {

                }
                // -> (verify left - right - center) vs (live face & eidFace) : verifyFaceMatch
                EkycVerificationMode.VERIFY_LIVENESS_FACE_MATCHING -> {
                    when (ONBOARDDATAMANAGER.businessType) {
                        BusinessType.VERIFY_EID_EKYC -> {
                            val data = ONBOARDDATAMANAGER.verifyIdRequestModel
                            ONBOARDDATAMANAGER.onboardFaceImagePath = capturedFace
                            ONBOARDDATAMANAGER.isFaceMatch =  if(verifyFaceMatch) "1" else "0"
                            val intent = Intent()
                            intent.putExtra(
                                MainActivity.KEY_REQ_MODEL,
                                VerifyEidRequestModel( dsCert =  data!!.dsCert,
                                    code = data.code,
                                    province = data.province,
                                    idCard = data.idCard,
                                    deviceType = data.deviceType,
                                    requestId = data.requestId)
                            )
                            setResult(RESULT_OK,intent)
                            finish()
                        }

                        else -> {}
                    }
                }
                else -> {}
            }
        }
    }

    private val eKycFaceResult: EkycFaceResult = object : EkycFaceResult {
        override fun onFaceLeft(faceLeft: String?) {}

        override fun onFaceCenter(faceCenter: String?) {}

        override fun onFaceRight(faceRight: String?) {}

        override fun onOpenMouth(faceSmile: String?) {}

        override fun onFaceSmile(faceSmile: String?) {
        }

        override fun onFaceSurprised(faceSurprised: String?) {}

        override fun onFaceSadness(faceSadness: String?) {}

        override fun onNodUp(faceNodUp: String?) {}

        override fun onNodDown(faceNodDown: String?) {}

        override fun onFaceFar(faceFar: String?) {}

        override fun onFaceNear(faceNear: String?) {}
    }

    // =================================
    // region Life Cycle
    // =================================
    override fun initUi() {

        val bottomSheetFragment = FaceVerificationInstructionFragment()
        bottomSheetFragment.setOnStartClickListener(this@LivenessActivity)
        bottomSheetFragment.show(supportFragmentManager, "MyBottomSheetFragment")

        mBinding!!.cameraPreview.clipToOutline = true
        mBinding!!.cameraPreview.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, 0f)
            }
        }
        mBinding!!.tvStepInstruction.text = getString(R.string.put_your_face_in_the_frame)
        mediaPlayer = MediaPlayer.create(this, R.raw.sound_beep)
    }

    override fun setListeners() {
        mBinding!!.btnBack.setOnClickListener { v: View? -> finish() }
//        mBinding!!.ivChangeCamera.setOnClickListener { v: View? ->
//            if (cameraController?.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
//                cameraController?.cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
//            } else {
//                cameraController?.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
//            }
//        }

        mBinding!!.instructionBtnContainer.setOnClickListener {  v: View? ->
            val bottomSheetFragment = FaceVerificationInstructionFragment()
            bottomSheetFragment.setOnStartClickListener(this@LivenessActivity)
            bottomSheetFragment.show(supportFragmentManager, "MyBottomSheetFragment")
        }
    }

    override fun onStartClicked() {
        checkPermissions { startCamera() }
    }

    override fun populateData() {

    }

    override val layoutRes: Int
        get() = R.layout.activity_liveness
    override val layoutView: View
        get() {
            mBinding = ActivityLivenessBinding.inflate(layoutInflater)
            return mBinding!!.root
        }

    override fun onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }
        super.onDestroy()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Do nothing or show a custom message
    }

    // =================================
    // endregion
    // =================================

    // =================================
    // region Private Liveness
    // =================================
    @SuppressLint("SetTextI18n")
    private fun startCamera() {

        cameraController = LifecycleCameraController(this)
        cameraController?.cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        cameraController?.bindToLifecycle(this)
        mBinding!!.cameraPreview.controller = cameraController
        APISERVICE.init(BuildConfig.API_KEY, BuildConfig.API_BASE_URL, BuildConfig.CUSTOMER_CODE)
        val drawable = getResources().getDrawable(R.drawable.img_nikunj_face)
        val bitmap: Bitmap = (drawable as BitmapDrawable).bitmap
        //ONBOARDDATAMANAGER.eid?.faceImage
        ActiveEkycUtils.EKYCSERVICE.init(
            this, cameraController, ONBOARDDATAMANAGER.eid?.faceImage,
            EkycVerificationMode.LIVENESS_FACE_MATCHING, faceListener, eKycFaceResult, onVerifyListener,
            arrayListOf(
                StepFace.FACE_CENTER,
                StepFace.LEFT,
                StepFace.RIGHT,
                ), true
        )
        ActiveEkycUtils.EKYCSERVICE.setCameraController(cameraController)
        ActiveEkycUtils.EKYCSERVICE.startAnalysis()
    }

    private fun playSoundBeep() {
        if (mediaPlayer != null && !mediaPlayer?.isPlaying!!) {
            mediaPlayer?.start()
        }
    }

    protected fun showProgress() {
        runOnUiThread { mBinding!!.lavAnimationLoading.visibility = View.VISIBLE }
    }

    protected fun hideProgress() {
        runOnUiThread { mBinding!!.lavAnimationLoading.visibility = View.INVISIBLE }
    }

    // =================================
    // endregion
    // =================================

}
