package com.gtel.ekyc.activities.verifyeid

import android.content.Intent
import android.view.View
import android.view.View.GONE
import com.gtel.ekyc.MainActivity
import com.gtel.ekyc.R
import com.gtel.ekyc.Stream

import com.gtel.ekyc.models.request.VerifyEidRequestModel
import com.gtel.ekyc.activities.common.BaseActivity
import com.gtel.ekyc.common.ONBOARDDATAMANAGER
import com.gtel.ekyc.databinding.ActivityVerifyingBinding

class VerifyingEidActivity : BaseActivity() {

    private lateinit var mBinding : ActivityVerifyingBinding

    override fun initUi() {
        mBinding.layoutHeader.ivBack.visibility = GONE
    }

    override fun setListeners() {

    }

    override fun populateData() {
        requestVerifyEid()
    }

    override fun onBackPressed() {

    }

    override val layoutRes: Int
        get() = R.layout.activity_verifying
    override val layoutView: View
        get() {
            mBinding = ActivityVerifyingBinding.inflate(layoutInflater)
            return mBinding.root
        }

    private fun requestVerifyEid() {
        val data = ONBOARDDATAMANAGER.verifyIdRequestModel
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


        Stream.enqueue(intent)
        setResult(RESULT_OK, intent)
        finish()

        return
    }
}