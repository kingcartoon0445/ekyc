package com.gtel.ekyc.activities.common

import android.Manifest
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Consumer
import com.gtel.ekyc.R
import com.gtel.ekyc.common.CoreConstant
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.gtel.ekyc.common.PopupDialog


abstract class BaseActivity : AppCompatActivity() {

    protected var context: Context = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (layoutView != null) {
            setContentView(layoutView)
        } else {
            setContentView(layoutRes)
        }
        supportActionBar?.hide()
        initUi()
        setListeners()
        populateData()
    }

    override fun onPause() {
        super.onPause()
    }

    protected val isAppInBackground: Boolean
        get() {
            val myProcess = RunningAppProcessInfo()
            ActivityManager.getMyMemoryState(myProcess)
            return myProcess.importance != RunningAppProcessInfo.IMPORTANCE_FOREGROUND
        }

    protected abstract fun initUi()
    protected abstract fun setListeners()
    protected abstract fun populateData()

    protected abstract val layoutRes: Int
    protected abstract val layoutView: View?

    fun finishWithResult() {
        val intent = Intent()
        setResult(RESULT_OK, intent)
        finish()
    }

    protected fun checkPermissions(runnable: Runnable) {
        val listPermission = ArrayList<String>()
        listPermission.add(Manifest.permission.CAMERA)
        Dexter.withContext(this).withPermissions(listPermission)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(multiplePermissionsReport: MultiplePermissionsReport) {
                    if (multiplePermissionsReport.areAllPermissionsGranted()) {
                        runnable.run()
                    } else {
                        CoreConstant.showAlertDialog(
                            this@BaseActivity,
                            getString(R.string.permission_please_go_to_setting_and_allow),
                            CoreConstant.DialogType.ERROR
                        )
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    list: List<PermissionRequest>,
                    permissionToken: PermissionToken
                ) {
                    permissionToken.continuePermissionRequest()
                }
            }).check()
    }

    protected fun showPopup(message: String, runnable: Runnable) {
        val popupDialog = PopupDialog(this, PopupDialog.PopupType.NOTIFICATION, Consumer {
            runnable.run()
        })
        popupDialog.setTitle(getString(R.string.error_not_success))
        popupDialog.setDescription(message)
        popupDialog.showDialog()
    }
}
