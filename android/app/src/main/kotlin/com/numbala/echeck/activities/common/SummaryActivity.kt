package com.gtel.ekyc.activities.common

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat.finishAffinity
import com.gtel.ekyc.R
import com.gtel.ekyc.common.ONBOARDDATAMANAGER
import com.gtel.ekyc.databinding.ActivitySummaryBinding
import vn.jth.xverifysdk.data.Eid


class SummaryActivity : BaseActivity() {

    private lateinit var binding: ActivitySummaryBinding

    companion object {
        var eid: Eid? = null
    }

    override fun initUi() {
        eid = ONBOARDDATAMANAGER.eid

        binding.tvFullName.text = eid?.personOptionalDetails?.fullName
        binding.tvFatherName.text = eid?.personOptionalDetails?.fatherName
        binding.tvMotherName.text = eid?.personOptionalDetails?.motherName
        binding.tvDob.text = eid?.personOptionalDetails?.dateOfBirth
        binding.tvGender.text = eid?.personOptionalDetails?.gender
        binding.tvDateOfIssue.text = eid?.personOptionalDetails?.dateOfIssue
        binding.tvDateOfExpiry.text = eid?.personOptionalDetails?.dateOfExpiry
        binding.tvNationality.text = eid?.personOptionalDetails?.nationality
        binding.tvPlaceOfOrigin.text = eid?.personOptionalDetails?.placeOfOrigin
        binding.tvPlaceOfResidence.text = eid?.personOptionalDetails?.placeOfResidence
        binding.tvDocumentNumber.text = eid?.personOptionalDetails?.eidNumber
        binding.tvPersonalIdentification.text = eid?.personOptionalDetails?.personalIdentification

        binding.ivBackEid.setImageBitmap(ONBOARDDATAMANAGER.mBitmapBack)
        binding.ivFrontEid.setImageBitmap(ONBOARDDATAMANAGER.mBitmapFront)
        binding.ivSelfie.setImageBitmap(ONBOARDDATAMANAGER.mBitmapSelfie)


    }

    override fun setListeners() {
        binding.btnBack.setOnClickListener {
            ONBOARDDATAMANAGER.isVerified = false
            finish()
        }

        binding.agreeBtn.setOnClickListener {
//            val resultIntent = Intent()
//            resultIntent.putExtra("isVerified", true)
//            setResult(Activity.RESULT_OK, resultIntent)


            ONBOARDDATAMANAGER.isVerified = true
            finishAffinity()
        }
    }

    override fun populateData() {

    }

    override val layoutRes: Int
        get() = R.layout.activity_summary

    override val layoutView: View
        get() {
            binding = ActivitySummaryBinding.inflate(layoutInflater)
            return binding.root
        }

    override fun onBackPressed() {
        super.onBackPressed()
        ONBOARDDATAMANAGER.isVerified = false
        finishAffinity()
    }
}