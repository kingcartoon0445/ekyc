package com.gtel.ekyc.activities.common

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import com.gtel.ekyc.R
import com.gtel.ekyc.common.BusinessType
import com.gtel.ekyc.common.EidFacadeLocal
import com.gtel.ekyc.common.IntentData
import com.gtel.ekyc.common.ONBOARDDATAMANAGER
import com.gtel.ekyc.models.BasicInformation
import org.jmrtd.lds.icao.MRZInfo
import vn.jth.xverifysdk.data.Eid
import  com.gtel.ekyc.fragments.NfcFragment

class NfcActivity : androidx.fragment.app.FragmentActivity(), NfcFragment.NfcFragmentListener {

    private var mrzInfo: MRZInfo? = null
    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private var isChecking : Boolean = false

    private var basicInformation: BasicInformation? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_nfc)
        val intent = intent
        if (intent.hasExtra(IntentData.KEY_QRCODE_INFO)) {
            basicInformation = intent.getSerializableExtra(IntentData.KEY_QRCODE_INFO) as BasicInformation
            ONBOARDDATAMANAGER.basicInformation = basicInformation
        } else {
            onBackPressed()
        }
        val mrzInfo =
            if (basicInformation!!.dateOfExpiry == null) {
                EidFacadeLocal.createMrz(
                    basicInformation!!.eidNumber,
                    basicInformation!!.dateOfBirth,
                    basicInformation!!.dateOfIssue!!
                )
            } else {
                EidFacadeLocal.createMrzInfo(
                    basicInformation!!.eidNumber,
                    basicInformation!!.dateOfBirth,
                    basicInformation!!.dateOfExpiry!!
                )
            }

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        if (nfcAdapter == null) {
            Toast.makeText(this, getString(R.string.warning_no_nfc), Toast.LENGTH_LONG).show()
            finish()
            return
        }

        pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(this, 0, Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getActivity(this, 0, Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_UPDATE_CURRENT)
        }


        if (null == savedInstanceState) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, NfcFragment.newInstance(mrzInfo), TAG_NFC)
                .commit()
        }

        val btnBack: ImageView = findViewById(R.id.btn_back)
        btnBack.setOnClickListener {
            finish()
        }
    }

    public override fun onResume() {
        super.onResume()
        val fragmentByTag = supportFragmentManager.findFragmentByTag(TAG_NFC)
        if (fragmentByTag is NfcFragment) {
            fragmentByTag.restartUi()
        }
    }

    public override fun onPause() {
        super.onPause()

    }

    public override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (NfcAdapter.ACTION_TAG_DISCOVERED == intent.action || NfcAdapter.ACTION_TECH_DISCOVERED == intent.action) {
            // drop NFC events
            handleIntent(intent)
        }else{
            super.onNewIntent(intent)
        }
    }

    protected fun handleIntent(intent: Intent) {
        if (isChecking) {
            return
        }
        val fragmentByTag = supportFragmentManager.findFragmentByTag(TAG_NFC)
        if (fragmentByTag is NfcFragment) {
            fragmentByTag.handleNfcTag(intent)
        }
    }

    override fun onEnableNfc() {
        if (nfcAdapter != null) {
            if (!nfcAdapter!!.isEnabled)
                showWirelessSettings()

            nfcAdapter!!.enableForegroundDispatch(this, pendingIntent, null, null)
        }
    }

    override fun onDisableNfc() {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        nfcAdapter.disableForegroundDispatch(this)
    }

    private fun onReadFailed() {
        val fragmentByTag = supportFragmentManager.findFragmentByTag(TAG_NFC)
        if (fragmentByTag is NfcFragment) {
            fragmentByTag.restartUi()
        }
    }

    override fun onEidRead(eid: Eid?) {
        Log.d("TAG", "onEidRead")
        isChecking = true

        ONBOARDDATAMANAGER.eid = eid

//        val requestModel = VerifyIdResquestModel()
//        requestModel.dsCert = StringUtils.encodeToBase64String(eid?.sod?.docSigningCertificate)
//        requestModel.code = BuildConfig.CUSTOMER_CODE
//        requestModel.province = StringUtils.getProvince(StringUtils.getProvince(eid?.personOptionalDetails?.placeOfResidence))
//        requestModel.idCard = eid?.personOptionalDetails?.eidNumber
//        requestModel.deviceType = "Android"
//        requestModel.requestId = UUID.randomUUID().toString()
//        ONBOARDDATAMANAGER.verifyIdRequestModel = requestModel

        ONBOARDDATAMANAGER.businessType = BusinessType.VERIFY_EID_EKYC
        val intent = Intent(this@NfcActivity, LivenessActivity::class.java)
        startActivity(intent)
    }

    override fun onCardException(cardException: Exception?) {
        Toast.makeText(this, getString(R.string.error_read_card), Toast.LENGTH_LONG).show();
    }

    private fun showWirelessSettings() {
        Toast.makeText(this, getString(R.string.warning_enable_nfc), Toast.LENGTH_LONG).show()
        val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
        startActivity(intent)
    }

    companion object {
        private val TAG = NfcActivity::class.java.simpleName
        private val TAG_NFC = "TAG_NFC"
    }
}