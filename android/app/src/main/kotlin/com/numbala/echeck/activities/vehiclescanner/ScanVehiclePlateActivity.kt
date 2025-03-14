package com.gtel.ekyc.activities.vehiclescanner

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.gtel.ekyc.common.IntentData
import com.gtel.ekyc.databinding.ActivityScanVehiclePlateBinding
import com.gtel.ekyc.fragments.CameraVpsFragment
import vn.jth.xverifysdk.card.VPNCallback
import vn.jth.xverifysdk.card.VPNFacade


class ScanVehiclePlateActivity : AppCompatActivity(), VPNCallback {

    private lateinit var mBinding: ActivityScanVehiclePlateBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        mBinding = ActivityScanVehiclePlateBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        VPNFacade.clearData()
        VPNFacade.registerVpnListener(this)
        val cameraMrzFragment = CameraVpsFragment()
        cameraMrzFragment.isForVehiclePlat = true
        supportFragmentManager.beginTransaction()
            .replace(mBinding.container.id, cameraMrzFragment)
            .commit()

        mBinding.btnBack.setOnClickListener { finish() }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(RESULT_CANCELED)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        VPNFacade.unregisterVpnListener(this)
    }


    companion object {
        private val TAG = ScanVehiclePlateActivity::class.java.simpleName
    }

    override fun completionHandler(plateNumber: String) {
        val intent = Intent()
        intent.putExtra(IntentData.KEY_VEHICLE_PLATE_INFO, plateNumber)
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun errorHandler(e: Exception?) {
        android.util.Log.d("TAG", "errorHandler: " + e?.message)
    }
}