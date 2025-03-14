package com.gtel.ekyc.common

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.WindowManager.BadTokenException
import androidx.annotation.StringRes
import androidx.core.util.Consumer
import com.gtel.ekyc.R
import com.gtel.ekyc.databinding.LayoutPopupBinding

class PopupDialog(private val mContext: Context, popupType: PopupType, private val mConfirmCallback: Consumer<Boolean>?) {
    private var mDialog: Dialog? = null
    private var mBinding: LayoutPopupBinding? = null
    private var mPopupType = PopupType.CONFIRM

    enum class PopupType {
        NOTIFICATION,
        CONFIRM
    }

    init {
        mPopupType = popupType
        createDialog()
    }

    private fun createDialog() {
        mDialog = Dialog(mContext)
        mDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mDialog!!.window!!.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        mDialog!!.window!!.setBackgroundDrawableResource(R.color.transparent)
        mBinding = LayoutPopupBinding.inflate(LayoutInflater.from(mContext))
        mDialog!!.setContentView(mBinding!!.root)
        if (mPopupType == PopupType.NOTIFICATION) {
            mBinding!!.btnCancel.visibility = View.GONE
        }
        mBinding!!.btnCancel.setOnClickListener { v: View? ->
            if (mDialog != null && mDialog!!.isShowing) {
                mDialog!!.dismiss()
            }
        }
        mBinding!!.btnOk.setOnClickListener { v: View? ->
            if (mDialog != null && mDialog!!.isShowing) {
                mDialog!!.dismiss()
            }
            mConfirmCallback?.accept(true)
        }
    }

    fun setTitle(title: String?) {
        if (mBinding != null) {
            mBinding!!.tvTitle.text = title
        }
    }

    fun setTitle(@StringRes title: Int) {
        if (mBinding != null) {
            mBinding!!.tvTitle.setText(title)
        }
    }

    fun setDescription(description: String?) {
        if (mBinding != null) {
            mBinding!!.tvDescription.text = description
        }
    }

    fun setDescription(@StringRes description: Int) {
        if (mBinding != null) {
            mBinding!!.tvDescription.setText(description)
        }
    }

    fun setTextCancel(textCancel: String?) {
        if (mBinding != null) {
            mBinding!!.btnCancel.text = textCancel
        }
    }

    fun setTextOk(textOk: String?) {
        if (mBinding != null) {
            mBinding!!.btnOk.text = textOk
        }
    }

    fun setTextOk(@StringRes textOk: Int) {
        if (mBinding != null) {
            mBinding!!.btnOk.setText(textOk)
        }
    }

    fun showDialog() {
        if (mDialog != null) {
            try {
                mDialog!!.show()
            } catch (e: BadTokenException) {
            }
        }
    }
}
