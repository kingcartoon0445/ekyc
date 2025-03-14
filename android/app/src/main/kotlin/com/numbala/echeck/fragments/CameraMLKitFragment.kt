package com.gtel.ekyc.fragments

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Outline
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.gtel.ekyc.R
import com.gtel.ekyc.databinding.FragmentCameraMrzBinding
import io.fotoapparat.preview.FrameProcessor
import io.fotoapparat.view.CameraView
import vn.jth.xverifysdk.card.EidFacade
import vn.jth.xverifysdk.card.VPNFacade

class CameraMLKitFragment : CameraFragment() {

    private var isDecoding = false
    private var binding: FragmentCameraMrzBinding? = null
    private var runnableInputTypeImage : Runnable? = null
    private var isFlash: Boolean = false
    var isForVehiclePlat: Boolean = false

    fun setInputTypeImageCallBack(runnable: Runnable) {
        this.runnableInputTypeImage = runnable
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCameraMrzBinding.inflate(inflater, container, false)
        binding!!.framePreview.clipToOutline = true
        binding!!.framePreview.outlineProvider = object: ViewOutlineProvider() {
            override fun getOutline(view: View?, outline: Outline?) {
                view?.let {
                    outline?.setRoundRect(0, 0, it.width, (view.height), 42F)
                }
            }
        }

        binding!!.btnFromGallery.setOnClickListener {
            runnableInputTypeImage?.run()
        }

        binding!!.btnFlash.setOnClickListener {
            isFlash = !isFlash
            setFlash(isFlash)
            val gradientDrawable: Drawable? = if (isFlash) {
                context?.let { it1 -> ContextCompat.getDrawable(it1, R.drawable.icon_flash_light_off) }
            } else {
                // context.resources.getDrawable(R.drawable.corner)
                context?.let { it1 -> ContextCompat.getDrawable(it1, R.drawable.icon_flash_light) }
            }
            binding!!.flashImage.setImageDrawable(gradientDrawable)
        }

        return binding?.root
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val activity = activity
    }

    override fun onDetach() {
        super.onDetach()

    }


    override val callbackFrameProcessor: FrameProcessor
        get() {
            if (isForVehiclePlat) {
                return VPNFacade.getCallbackFrameProcessor(0)
            }
            return EidFacade.getCallbackFrameProcessor(0)
        }

    override val cameraPreview: CameraView
        get() {
            return binding?.cameraPreview!!
        }

    override val requestedPermissions: ArrayList<String>
        get() {
            return ArrayList<String>()
        }

    override fun onRequestPermissionsResult(
        permissionsDenied: ArrayList<String>,
        permissionsGranted: ArrayList<String>
    ) {
    }

    private fun requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            ConfirmationDialog().show(childFragmentManager, FRAGMENT_DIALOG)
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.size != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                ErrorDialog.newInstance(getString(R.string.permission_camera_rationale))
                    .show(childFragmentManager, FRAGMENT_DIALOG)
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun showToast(text: String) {
        val activity = activity
        activity?.runOnUiThread { Toast.makeText(activity, text, Toast.LENGTH_SHORT).show() }
    }

    class ErrorDialog : androidx.fragment.app.DialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val activity = activity
            return AlertDialog.Builder(activity)
                .setMessage(requireArguments().getString(ARG_MESSAGE))
                .setPositiveButton(android.R.string.ok) { dialogInterface, i -> activity!!.finish() }
                .create()
        }

        companion object {

            private val ARG_MESSAGE = "message"

            fun newInstance(message: String): ErrorDialog {
                val dialog = ErrorDialog()
                val args = Bundle()
                args.putString(ARG_MESSAGE, message)
                dialog.arguments = args
                return dialog
            }
        }

    }

    class ConfirmationDialog : androidx.fragment.app.DialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val parent = parentFragment
            return AlertDialog.Builder(activity)
                .setMessage(R.string.permission_camera_rationale)
                .setPositiveButton(android.R.string.ok) { dialog, which ->
                    parent!!.requestPermissions(
                        arrayOf(Manifest.permission.CAMERA),
                        REQUEST_CAMERA_PERMISSION
                    )
                }
                .setNegativeButton(
                    android.R.string.cancel
                ) { dialog, which ->
                    val activity = parent!!.activity
                    activity?.finish()
                }
                .create()
        }
    }

    companion object {
        private val TAG = CameraMLKitFragment::class.java.simpleName

        private val REQUEST_CAMERA_PERMISSION = 1
        private val FRAGMENT_DIALOG = "CameraMLKitFragment"

        fun newInstance(): CameraMLKitFragment {
            return CameraMLKitFragment()
        }
    }
}
