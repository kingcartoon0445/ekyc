package com.gtel.ekyc.activities.verifyeid

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import com.gtel.ekyc.R
import com.gtel.ekyc.activities.common.BaseActivity
import java.util.Calendar

import com.gtel.ekyc.common.ONBOARDDATAMANAGER
import com.gtel.ekyc.databinding.ActivityVerifyEidSuccessBinding
import com.stfalcon.imageviewer.StfalconImageViewer

class VerifyEidSuccessActivity : BaseActivity() {

    private lateinit var mBinding: ActivityVerifyEidSuccessBinding
    private var listFace : ArrayList<Bitmap> = ArrayList()
    private lateinit var imageViewTransition : StfalconImageViewer<Bitmap>

    override fun initUi() {

        val eid = ONBOARDDATAMANAGER.eid
        if (eid?.faceImage == null && eid?.portrait == null) {
            mBinding.llPersonalInfo.ivFace.visibility = View.GONE
        } else {
            eid.faceImage?.let { listFace.add(it) }
            mBinding.llPersonalInfo.ivFace.setImageBitmap(eid.faceImage)
        }

        mBinding.llPersonalInfo.tvContentFullName.text = eid?.personOptionalDetails?.fullName
        mBinding.llPersonalInfo.tvContentGender.text = eid?.personOptionalDetails?.gender
        mBinding.llPersonalInfo.tvContentDateOfBirth.text = eid?.personOptionalDetails?.dateOfBirth
        mBinding.llPersonalInfo.tvContentAge.text = eid?.personOptionalDetails?.dateOfBirth?.let { calcAge(it) }
        mBinding.llPersonalInfo.tvContentDocumentNumber.text = eid?.personOptionalDetails?.eidNumber
        mBinding.llPersonalInfo.tvContentDateOfIssue.text = eid?.personOptionalDetails?.dateOfIssue
        mBinding.llPersonalInfo.tvContentDateOfExpiry.text = eid?.personOptionalDetails?.dateOfExpiry
        mBinding.llPersonalInfo.tvEthnicity.text = eid?.personOptionalDetails?.ethnicity
        mBinding.llPersonalInfo.tvReligion.text = eid?.personOptionalDetails?.religion
        mBinding.llPersonalInfo.tvPlaceOfOrigin.text = eid?.personOptionalDetails?.placeOfOrigin
        mBinding.llPersonalInfo.tvPlaceOfResidence.text = eid?.personOptionalDetails?.placeOfResidence
        mBinding.llPersonalInfo.tvPersonalIdentification.text = eid?.personOptionalDetails?.personalIdentification
        mBinding.llPersonalInfo.tvFatherName.text = eid?.personOptionalDetails?.fatherName
        mBinding.llPersonalInfo.tvMotherName.text = eid?.personOptionalDetails?.motherName
        mBinding.llPersonalInfo.tvSpouseName.text = eid?.personOptionalDetails?.spouseName
        mBinding.llPersonalInfo.tvOldEidNumber.text = eid?.personOptionalDetails?.oldEidNumber
    }

    override fun setListeners() {
        mBinding.llPersonalInfo.ivFace.setOnClickListener {
            showImageLarge(this, listFace, mBinding.llPersonalInfo.ivFace)
        }

        mBinding.btnVerifyCompleted.setOnClickListener {
            finish()
        }
    }

    override fun populateData() {
    }

    override val layoutRes: Int
        get() = R.layout.activity_verify_eid_success

    override val layoutView: View
        get() {
            mBinding = ActivityVerifyEidSuccessBinding.inflate(layoutInflater)
            return mBinding.root
        }

    private fun showImageLarge(context: Context, listBitmap: List<Bitmap>, imageView: ImageView) {
        if (listBitmap.isEmpty()) {
            return
        }

        imageViewTransition = StfalconImageViewer.Builder(context, listBitmap) { imageViewLarge, imageLink -> viewImage(imageLink, imageViewLarge) }
            .withStartPosition(0)
            .withTransitionFrom(imageView).withImageChangeListener { position1 -> imageViewTransition.updateTransitionImage(imageView) }.show()
    }

    private fun viewImage(bitmap: Bitmap, imageViewLarge: ImageView) {
        imageViewLarge.setImageBitmap(bitmap)
    }

    private fun calcAge(date: String): String {
        val sYear = date.substring(date.lastIndexOf("/") + 1)
        val calendar = Calendar.getInstance()
        val currentYear = calendar[Calendar.YEAR]
        try {
            val age = currentYear - sYear.toInt()
            return if (age > 0) age.toString() else "-"
        } catch (ex: NumberFormatException) {
        }
        return "-"
    }
}