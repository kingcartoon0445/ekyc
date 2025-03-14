package com.gtel.ekyc.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.gtel.ekyc.R
import com.gtel.ekyc.roundcornerlayout.RoundCornerConstraintLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class FaceVerificationInstructionFragment : BottomSheetDialogFragment() {

    private var listener: OnStartClickListener? = null
    fun setOnStartClickListener(listener: OnStartClickListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view  =  inflater.inflate(R.layout.fragment_face_verification_instruction, container, false)
        val imageView = view.findViewById<ImageView>(R.id.ivClose)
        view.findViewById<RoundCornerConstraintLayout>(R.id.btnStart).setOnClickListener {
            listener?.onStartClicked()
            dismiss()
        }

        imageView.setOnClickListener {
            listener?.onCancel()
            dismiss()
        }
        isCancelable = false
        return view
    }
    override fun onStart() {
        super.onStart()
        dialog?.let { dialog ->
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            val behaviour = BottomSheetBehavior.from(bottomSheet)
            bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
            behaviour.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }



    interface OnStartClickListener {
        fun onStartClicked()
        fun onCancel(){}
    }
}