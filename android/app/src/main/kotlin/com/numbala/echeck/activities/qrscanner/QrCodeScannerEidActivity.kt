package com.gtel.ekyc.activities.qrscanner

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.gtel.ekyc.R
import com.gtel.ekyc.activities.common.NfcActivity
import com.gtel.ekyc.common.IntentData
import com.gtel.ekyc.common.ONBOARDDATAMANAGER
import com.gtel.ekyc.fragments.CameraQrFragment
import com.gtel.ekyc.vision.qrcode.QRCodeCallback
import com.gtel.ekyc.vision.qrcode.QRCodeFacade
import com.google.gson.Gson
import com.google.mlkit.vision.barcode.common.Barcode

class QrCodeScannerEidActivity: AppCompatActivity() , QRCodeCallback {

    @SuppressLint("CommitTransaction")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_scan_qr_code)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val cameraMrzFragment = CameraQrFragment()
        supportFragmentManager.beginTransaction().replace(R.id.container, cameraMrzFragment, TAG_QR)
            .commit()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        setResult(RESULT_CANCELED)
        finish()
    }

    override fun onRestart() {
        super.onRestart()
    }

    override fun onResume() {
        super.onResume()
        QRCodeFacade.registerListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        QRCodeFacade.unregisterListener(this)
    }

    override fun onResult(result: List<Barcode>) {
        if (result.isNotEmpty()) {
            onDrawFrame(result)
            val qrValue  = result[0].displayValue;
            if (!qrValue.isNullOrEmpty()) {
               parserData(qrValue)
            }
        }
    }

    private fun parserData(string: String) {
        try {
            val result = QRCodeFacade.parserQrCode(string)
            QRCodeFacade.unregisterListener(this)

            ONBOARDDATAMANAGER.rawQrCode = Gson().toJson(result);
            val intent = Intent(this, NfcActivity::class.java)
            intent.putExtra(IntentData.KEY_QRCODE_INFO, result)
            startActivity(intent)

         //   val intent = Intent(this, UpdateBioMetricInfoActivity::class.java)
//            val intent = Intent(this, if(result.dateOfIssue.isNullOrEmpty()){
//                UpdateBioMetricInfoActivity::class.java
//            }else{
//                NfcActivity::class.java
//            })
//            intent.putExtra(IntentData.KEY_QRCODE_INFO, result)
//            startActivity(intent)
        }catch (e:Exception){
            Log.e(TAG,"Lỗi: ${e.message}")
        }
    }

    private fun onDrawFrame(barcodes: List<Barcode>?) {
        val fragmentByTag = supportFragmentManager.findFragmentByTag(TAG_QR)
        if (fragmentByTag is CameraQrFragment) {
            fragmentByTag.drawFrameBarcode(barcodes)
        }
    }

    companion object{
        private val TAG = QrCodeScannerEidActivity::class.java.name
        private const val TAG_QR = "QRCODE"
    }
}