package com.gtel.ekyc.activities.verifyeid

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.View
import com.gtel.ekyc.R
import org.jmrtd.lds.icao.MRZInfo

import com.gtel.ekyc.activities.common.BaseActivity
import com.gtel.ekyc.activities.common.ScanMrzActivity
import com.gtel.ekyc.common.IntentData
import com.gtel.ekyc.databinding.ActivityVerifyEidMainBinding
import com.gtel.ekyc.activities.common.NfcActivity
import vn.jth.xverifysdk.network.ApiService.APISERVICE

class VerifyEidMainActivity : BaseActivity() {

    private lateinit var mBinding: ActivityVerifyEidMainBinding

    override fun initUi() {

        val url = System.getenv("API_URL")
        val apiKey = System.getenv("API_KEY")
        val customerCode = System.getenv("CUSTOMER_CODE")
        Log.d("TonyLog", "Url: " + url)
        Log.d("TonyLog", "Key: " + apiKey)
        Log.d("TonyLog", "Code: " + customerCode)
        APISERVICE.init(apiKey, url, customerCode)
        mBinding.introView.introImage.setImageResource(R.drawable.img_splash_eid)
        mBinding.introView.introTitle.setText(R.string.intro_title_eid)
        mBinding.introView.introDescription.setText(R.string.intro_description_eid)
    }

    override fun setListeners() {
        mBinding.lheader.ivBack.setOnClickListener { finish() }
        mBinding.btnVerifyNfc.setOnClickListener {
            val intent = Intent(this, ScanMrzActivity::class.java)
            startActivityForResult(intent, REQUEST_MRZ)
        }
    }

    override fun populateData() {
    }

    override val layoutRes: Int
        get() = R.layout.activity_verify_eid_main

    override val layoutView: View
        get() {
            mBinding = ActivityVerifyEidMainBinding.inflate(layoutInflater)
            return mBinding.root
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var dataIntent = data
        if (dataIntent == null) {
            dataIntent = Intent()
        }
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_MRZ -> {
                    val mrzInfo = dataIntent.getSerializableExtra(IntentData.KEY_MRZ_INFO) as MRZInfo
                    val intent = Intent(this, NfcActivity::class.java)
                    intent.putExtra(IntentData.KEY_MRZ_INFO, mrzInfo)
                    startActivityForResult(intent, REQUEST_NFC)
                }
                REQUEST_NFC -> {
                    val intent = Intent(this@VerifyEidMainActivity, VerifyingEidActivity::class.java)
                    startActivityForResult(intent, REQUEST_VERIFY)
                }
                REQUEST_VERIFY -> {
                    setResult(Activity.RESULT_OK, dataIntent)
                    finish()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        val REQUEST_VERIFY_EKYC = 8
        val REQUEST_MRZ = 12
        val REQUEST_LOGIN = 13
        val REQUEST_NFC = 11
        val REQUEST_VERIFY = 10
        val REQUEST_IMPORT = 9
        val REQUEST_VEHICLE_SCAN = 14
    }
}