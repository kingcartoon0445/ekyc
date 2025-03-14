package com.gtel.ekyc.fragments

import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.gtel.ekyc.R
import io.reactivex.disposables.CompositeDisposable
import net.sf.scuba.smartcards.CardServiceException
import net.sf.scuba.smartcards.ISO7816
import org.jmrtd.BACDeniedException
import org.jmrtd.PACEException
import org.jmrtd.lds.icao.MRZInfo
import com.gtel.ekyc.common.IntentData
import com.gtel.ekyc.databinding.FragmentNfcBinding
import org.jmrtd.TagLostConnectException
import vn.jth.xverifysdk.data.Eid
import vn.jth.xverifysdk.card.EidCallback
import vn.jth.xverifysdk.card.EidFacade
import java.security.Security

class NfcFragment : androidx.fragment.app.Fragment() {

    var mrzInfo: MRZInfo? = null
    private var nfcFragmentListener: NfcFragmentListener? = null

    internal var mHandler = Handler(Looper.getMainLooper())
    var disposable = CompositeDisposable()

    private var binding: FragmentNfcBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentNfcBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val arguments = arguments
        if (arguments != null) {
            if (arguments.containsKey(IntentData.KEY_MRZ_INFO)) {
                mrzInfo = arguments.getSerializable(IntentData.KEY_MRZ_INFO) as MRZInfo
            }
        }
    }

    fun handleNfcTag(intent: Intent?) {
        if (intent == null || intent.extras == null) {
            return
        }
        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG) ?: return

        val subscribe = EidFacade.readChipNfc(requireContext(), tag,
            mrzInfo!!,
            object : EidCallback {

                override fun onEidReadStart() {
                    onNFCReadStart()
                }

                override fun onEidReadFinish() {
                    onNFCReadFinish()
                }

                override fun onEidRead(eid: Eid?) {
                    this@NfcFragment.onEidRead(eid)

                }

                override fun onBACDeniedException(exception: BACDeniedException) {
                    Toast.makeText(context, exception.toString(), Toast.LENGTH_SHORT).show()
                    this@NfcFragment.onCardException(exception)
                }

                override fun onPACEException(exception: PACEException) {
                    Toast.makeText(context, exception.toString(), Toast.LENGTH_SHORT).show()
                    this@NfcFragment.onCardException(exception)
                }

                override fun onTagLostException(exception: TagLostConnectException) {

                }

                override fun onCardException(exception: CardServiceException) {
                    val sw = exception.sw.toShort()
                    when (sw) {
                        ISO7816.SW_CLA_NOT_SUPPORTED -> {
                            Toast.makeText(
                                context,
                                getString(R.string.warning_cla_not_supported),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else -> {
                            Toast.makeText(context, exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                    this@NfcFragment.onCardException(exception)
                }

                override fun onGeneralException(exception: Exception?) {
                    Toast.makeText(context, exception!!.toString(), Toast.LENGTH_SHORT).show()
                    this@NfcFragment.onCardException(exception)
                }
            })

        disposable.add(subscribe)

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val activity = activity
        if (activity is NfcFragmentListener) {
            nfcFragmentListener = activity
        }
    }

    override fun onDetach() {
        nfcFragmentListener = null
        super.onDetach()
    }


    override fun onResume() {
        super.onResume()
        if (nfcFragmentListener != null) {
            nfcFragmentListener!!.onEnableNfc()
        }
    }

    override fun onPause() {
        super.onPause()
        if (nfcFragmentListener != null) {
            nfcFragmentListener!!.onDisableNfc()
        }
    }

    override fun onDestroyView() {
        if (!disposable.isDisposed) {
            disposable.dispose()
        }
        binding = null
        super.onDestroyView()
    }

    fun restartUi() {
        mHandler.post {
            binding?.lavAnimScanId?.pauseAnimation()
            binding?.tvNfcInstruction?.text = getString(R.string.instruction_nfc)
            binding?.lavAnimationLoading?.visibility = View.GONE
            binding?.tvNfcInstruction?.visibility = View.GONE
        }
    }

    protected fun onNFCReadStart() {
        Log.d(TAG, "onNFCSReadStart")
        mHandler.post {
            binding?.lavAnimScanId?.playAnimation()
            binding?.tvNfcInstruction?.text = getString(R.string.instruction_nfc)
            binding?.lavAnimationLoading?.visibility = View.GONE
            binding?.tvNfcInstruction?.visibility = View.GONE
        }

    }

    protected fun onNFCReadFinish() {
        Log.d(TAG, "onNFCReadFinish")
        mHandler.post {
            binding?.lavAnimScanId?.pauseAnimation()
        }
    }

    protected fun onCardException(cardException: Exception?) {
        mHandler.post {
            if (nfcFragmentListener != null) {
                nfcFragmentListener?.onCardException(cardException)
                binding?.tvNfcInstruction?.text = getString(R.string.instruction_nfc)
                binding?.lavAnimationLoading?.visibility = View.GONE
            }
        }
    }

    protected fun onEidRead(eid: Eid?) {
        mHandler.post {
            if (nfcFragmentListener != null) {
                binding?.tvNfcInstruction?.text = getString(R.string.nfc_read)
                binding?.tvNfcInstruction?.visibility = View.VISIBLE
                binding?.lavAnimationLoading?.visibility = View.VISIBLE
                nfcFragmentListener?.onEidRead(eid)
            }
        }
    }

    interface NfcFragmentListener {
        fun onEnableNfc()
        fun onDisableNfc()
        fun onEidRead(eid: Eid?)
        fun onCardException(cardException: Exception?)
    }



    companion object {
        private val TAG = NfcFragment::class.java.simpleName

        init {
            Security.insertProviderAt(org.spongycastle.jce.provider.BouncyCastleProvider(), 1)
        }

        fun newInstance(mrzInfo: MRZInfo): NfcFragment {
            val myFragment = NfcFragment()
            val args = Bundle()
            args.putSerializable(IntentData.KEY_MRZ_INFO, mrzInfo)
            myFragment.arguments = args
            return myFragment
        }
    }
}
