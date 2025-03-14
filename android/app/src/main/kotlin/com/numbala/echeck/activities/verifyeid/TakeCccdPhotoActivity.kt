package com.gtel.ekyc.activities.verifyeid

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gtel.ekyc.R
import com.gtel.ekyc.activities.common.BaseActivity
import com.gtel.ekyc.activities.common.ScanMrzActivity
import com.gtel.ekyc.activities.qrscanner.QrCodeScannerEidActivity
import com.gtel.ekyc.common.ONBOARDDATAMANAGER
import com.gtel.ekyc.databinding.ActivityTakeCccdPhotoBinding
import com.gtel.ekyc.databinding.DialogCustomErrorBinding
import com.gtel.ekyc.fragments.CaptureEidFragment
import com.gtel.ekyc.service.TakeCCCDService
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import java.io.ByteArrayOutputStream


class TakeCccdPhotoActivity : BaseActivity() {
    private lateinit var mBinding : ActivityTakeCccdPhotoBinding
    val captureFrontEidFragment = CaptureEidFragment("FRONT")
    val captureBackEidFragment = CaptureEidFragment("BACK")


    override fun initUi() {
        setupViewPage()

        captureFrontEidFragment.setHandleImageCallBack{
            val bitmap = captureFrontEidFragment.getImageBitmap()
            ONBOARDDATAMANAGER.mBitmapFront = bitmap

            mBinding.viewPager.setCurrentItem(1,true)
            mBinding.lineProgress.progress = Pair(2,100f)
            mBinding.txtNfcCodeTitle.setTextColor(ContextCompat.getColor(this, android.R.color.black))
        }

        captureBackEidFragment.setHandleImageCallBack{
            val bitmap = captureBackEidFragment.getImageBitmap()
            ONBOARDDATAMANAGER.mBitmapBack = bitmap
            verifyOcr()
        }

        mBinding.lheader.ivBack.setOnClickListener { finish() }
    }
    private fun verifyOcr(retryCount: Int = 0) {
        val MAX_RETRIES = 1  // Set max retry attempts to prevent infinite loops

        // Disable the confirm button while API is calling
        captureBackEidFragment.view?.findViewById<View>(R.id.btnConfirm)?.isEnabled = false
        captureBackEidFragment.view?.findViewById<View>(R.id.btnConfirm)?.setBackgroundColor(ContextCompat.getColor(this, R.color.grey_mid))
        captureBackEidFragment.view?.findViewById<TextView>(R.id.tvButtonConfirm)?.text = "Vui lòng chờ..."

        // Compress image dynamically based on retry count
        val compressionQuality = if (retryCount == 0) 100 else 70
        val imgFrontBase64 = TakeCCCDService.bitmapToBase64JPEG(ONBOARDDATAMANAGER.mBitmapFront!!, compressionQuality)
        val imgBackBase64 = TakeCCCDService.bitmapToBase64JPEG(ONBOARDDATAMANAGER.mBitmapBack!!, compressionQuality)

        // Show loading dialog
        val loadingDialog = showLoadingDialog(this)

        // Call API verifyOcr
        TakeCCCDService.verifyOcr(imgFrontBase64, imgBackBase64) { isSuccess, errorCodes, message ->
            runOnUiThread {
                // Re-enable the confirm button
                captureBackEidFragment.view?.findViewById<View>(R.id.btnConfirm)?.isEnabled = true
                captureBackEidFragment.view?.findViewById<View>(R.id.btnConfirm)?.setBackgroundColor(Color.parseColor("#19a9df"))
                captureBackEidFragment.view?.findViewById<TextView>(R.id.tvButtonConfirm)?.text = "Xác nhận"

                loadingDialog.dismiss()

                if (isSuccess) {

                    if (errorCodes.isNullOrEmpty() && message.isNullOrEmpty()) {
                        // Go to next screen
                        val intent = Intent(this@TakeCccdPhotoActivity, QrCodeScannerEidActivity::class.java)
                        startActivity(intent)
                    } else {
                        // Convert error code to error message
                        val errorBack = errorCodes?.get("back_invalid_code")
                        val errorFront = errorCodes?.get("front_invalid_code")
                        val errorMessageBack = if (errorBack != "0") TakeCCCDService.getErrorMessage(errorBack ?: "") else null
                        val errorMessageFront = if (errorFront != "0") TakeCCCDService.getErrorMessage(errorFront ?: "") else null

                        val dialogHeader="Vui lòng kiểm tra lại ảnh chụp"
                        var dialogTitle=""
                        var dialogMessage=""

                        when {
                            errorMessageBack != null && errorMessageFront != null -> {
                                dialogTitle="Cả hai mặt"
                                dialogMessage="\nMặt trước : $errorMessageFront\nMặt sau : $errorMessageBack"
                            }
                            errorMessageFront != null -> {
                                dialogTitle="Mặt trước"
                                dialogMessage=errorMessageFront
                            }
                            errorMessageBack != null -> {
                                dialogTitle="Mặt sau"
                                dialogMessage=errorMessageBack
                            }
                        }
                        showCustomErrorDialog(this,dialogHeader,dialogTitle , dialogMessage )
                    }
                } else {
                    if (message == "TimeOutError") {
                        if (retryCount < MAX_RETRIES) {
                            // Retry with lower compression
                            Log.d("TakeCCCDService", "Timeout detected. Retrying API call with compressed images...")
                            verifyOcr(retryCount + 1)  // Recursive call to retry API
                        } else {
                            // Show error after max retries
                            showCustomErrorDialog(this, "Xác thực thất bại", "Hệ thống đang bận, vui lòng thử lại sau", "")
                        }
                        return@runOnUiThread
                    }
                }
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun showLoadingDialog(context: Context) : AlertDialog {
        val loadingDialog = AlertDialog.Builder(this)
            .setView(LayoutInflater.from(this).inflate(R.layout.loading_dialog, null))
            .setCancelable(false)
            .create()
        loadingDialog.window?.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.dialog_transparent_background))
        loadingDialog.show()
        return loadingDialog
    }

    private fun showCustomErrorDialog(context: Context,header:String,title: String, errorMessage: String?):AlertDialog {
        val binding = DialogCustomErrorBinding.inflate(LayoutInflater.from(context))

        val dialog = AlertDialog.Builder(context)
            .setView(binding.root)
            .setCancelable(false)
            .create()
        binding.tvTitle.text = header
        binding.tvIdSide.text = "$title CCCD"
        binding.tvContent.text = errorMessage
        binding.tvConfirm.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.dialog_background))
        dialog.show()
        return dialog
    }
    override fun onResume() {
        super.onResume()
        mBinding.viewPager.setCurrentItem(0,true)
        mBinding.lineProgress.progress = Pair(1,100f)
        mBinding.txtNfcCodeTitle.setTextColor(ContextCompat.getColor(this, R.color.silver))
    }

    override fun setListeners() {

    }

    override fun populateData() {

    }

    override val layoutRes: Int
        get() = R.layout.activity_take_cccd_photo

    override val layoutView: View
        get() {
            mBinding = ActivityTakeCccdPhotoBinding.inflate(layoutInflater)
            return mBinding.root
        }


    override fun onDestroy() {
        super.onDestroy()
    }

    private fun  setupViewPage() {
        val fragmentList = listOf(captureFrontEidFragment, captureBackEidFragment)
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

    companion object{
        private val TAG = TakeCccdPhotoActivity::class.java.simpleName
    }

}