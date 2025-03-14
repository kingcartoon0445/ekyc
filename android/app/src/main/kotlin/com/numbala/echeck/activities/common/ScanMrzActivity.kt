package com.gtel.ekyc.activities.common

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gtel.ekyc.BuildConfig
import com.gtel.ekyc.R
import com.gtel.ekyc.activities.verifyeid.VerifyEidMainActivity.Companion.REQUEST_IMPORT
import com.gtel.ekyc.common.ONBOARDDATAMANAGER
import com.gtel.ekyc.databinding.ActivityCameraBinding
import com.gtel.ekyc.fragments.CameraMLKitFragment
import com.gtel.ekyc.fragments.NfcFragment
import org.jmrtd.lds.icao.MRZInfo
import vn.jth.xverifysdk.card.EidFacade
import vn.jth.xverifysdk.card.MRZCallback
import vn.jth.xverifysdk.card.MRZException
import vn.jth.xverifysdk.data.Eid
import vn.jth.xverifysdk.network.models.request.VerifyIdResquestModel
import vn.jth.xverifysdk.utils.StringUtils
import java.util.UUID


open class ScanMrzActivity : FragmentActivity(), NfcFragment.NfcFragmentListener, MRZCallback {

    private lateinit var mBinding: ActivityCameraBinding
    private lateinit var cameraMLKitFragment : CameraMLKitFragment

    private lateinit var nfcFragment : NfcFragment


    private var mrzInfo: MRZInfo? = null
    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private var isChecking : Boolean = false
    private var tmpIntent: Intent? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        mBinding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(mBinding.root)


        EidFacade.registerMrzListener(this)

        cameraMLKitFragment = CameraMLKitFragment()
        cameraMLKitFragment.setInputTypeImageCallBack{
//            finish()
//            startActivityForResult(Intent(this, ImportMrzActivity::class.java),REQUEST_IMPORT)
        }

        nfcFragment = NfcFragment(); //NfcFragment.newInstance(mrzInfo!!)


        mBinding.lheader.ivBack.setOnClickListener { finish() }

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

        setupViewPage()
    }



    override fun onBackPressed() {
        super.onBackPressed()
        setResult(RESULT_CANCELED)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        EidFacade.unregisterMrzListener(this)
    }


    override fun completionHandler(mrzInfo: MRZInfo) {
//        val intent = Intent()
//        intent.putExtra(IntentData.KEY_MRZ_INFO, mrzInfo)
//        setResult(RESULT_OK, intent)
//        finish()

        nfcFragment.mrzInfo = mrzInfo
        //tmpIntent?.let { handleIntent(it) };
        mBinding.viewPager.setCurrentItem(1,true)
        mBinding.lineProgress.progress = Pair(2,100f)
        mBinding.txtNfcCodeTitle.setTextColor(ContextCompat.getColor(this, android.R.color.black))
        mBinding.txtMrzCodeTitle.setTextColor(ContextCompat.getColor(this, android.R.color.black))

    }

    override fun errorHandler(e: MRZException?) {
    }

    companion object {
        private val TAG = ScanMrzActivity::class.java.simpleName
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == RESULT_OK) {
            when(requestCode) {
                REQUEST_IMPORT -> {
                    setResult(RESULT_OK,data)
                    finish()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)

    }

    public override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (NfcAdapter.ACTION_TAG_DISCOVERED == intent.action || NfcAdapter.ACTION_TECH_DISCOVERED == intent.action) {
            // drop NFC events
            if (nfcFragment.mrzInfo != null) {
                handleIntent(intent);
            }
        }else{
            super.onNewIntent(intent)
        }
    }

    protected fun handleIntent(intent: Intent) {
        if (isChecking) {
            return
        }
        nfcFragment.handleNfcTag(intent)

    }

    private fun  setupViewPage() {
        val fragmentList = listOf(cameraMLKitFragment, nfcFragment)
        val adapter = ViewPagerAdapter(this, fragmentList)
        mBinding.viewPager.adapter = adapter
    }

    class ViewPagerAdapter(activity: FragmentActivity, private val fragments: List<Fragment>) : FragmentStateAdapter(activity) {

        override fun getItemCount(): Int {
            return fragments.size
        }

        override fun createFragment(position: Int): Fragment {
            return fragments[position]
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

        nfcFragment.restartUi()

    }

    override fun onEidRead(eid: Eid?) {
        Log.d("TAG", "onEidRead")
        isChecking = true

        ONBOARDDATAMANAGER.eid = eid

        Log.d("TAG SCANMRZ", "onEidRead: ${eid?.DG15FileEncode.toString()}")



        val requestModel = VerifyIdResquestModel()
        requestModel.dsCert = StringUtils.encodeToBase64String(eid?.sod?.docSigningCertificate)
        requestModel.code = BuildConfig.CUSTOMER_CODE
        requestModel.province = StringUtils.getProvince(StringUtils.getProvince(eid?.personOptionalDetails?.placeOfResidence))
        requestModel.idCard = eid?.personOptionalDetails?.eidNumber
        requestModel.deviceType = "Android"
        requestModel.requestId = UUID.randomUUID().toString()
        ONBOARDDATAMANAGER.verifyIdRequestModel = requestModel

        setResult(RESULT_OK)
        finish()
    }

    private fun verifyFailed() {
        isChecking = false
        onReadFailed()
    }

    override fun onCardException(cardException: Exception?) {
        Toast.makeText(this, getString(R.string.error_read_card), Toast.LENGTH_LONG).show();
    }

    private fun showWirelessSettings() {
        Toast.makeText(this, getString(R.string.warning_enable_nfc), Toast.LENGTH_LONG).show()
        val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
        startActivity(intent)
    }

}
