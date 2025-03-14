package com.gtel.ekyc.vision.qrcode

import com.gtel.ekyc.models.BasicInformation
import com.google.mlkit.vision.barcode.common.Barcode
import java.lang.ref.WeakReference

object QRCodeFacade {
    private val callbacks: MutableList<WeakReference<QRCodeCallback>> = mutableListOf()

    // =================================
    // region Public
    // =================================

    fun broadcastEvent(barcode: List<Barcode>) {
        callbacks.filter { it.get() != null }.forEach {
            it.get()?.onResult(barcode);
        }
    }

    /**
     *  Register Mrz Listener to receive result from Mrz scanner.
     *  @param callback
     */
    fun registerListener(callback: QRCodeCallback) {
        callbacks.add(WeakReference(callback))
    }

    /**
     *  Unregister all Mrz Listener to receive result from Mrz scanner.
     *
     */
    fun unregisterListener() {
        callbacks.clear()
    }

    /**
     *  Unregister specify Mrz Listener to receive result from Mrz scanner.
     *  @param callback
     */
    fun unregisterListener(callback: QRCodeCallback) {
        val i: MutableIterator<WeakReference<QRCodeCallback>> = callbacks.iterator()
        while (i.hasNext()) {
            val item = i.next()
            if (item.get() == callback) {
                i.remove()
            }
        }
    }


    /**
     * return BasicInformation
     * @param result: data string read from citizen identification card
     */
    fun parserQrCode(result: String): BasicInformation {
        if (result.isEmpty()) {
            throw NullPointerException("result is empty")
        }

        val parser = result.split("|")
        if (parser.size < 7) throw Exception("Data invalid")
        val informationQrCode = BasicInformation().apply {
            eidNumber = parser[0].trim()
            oldEidNumber = parser[1].trim()
            fullName = parser[2].trim()
            dateOfBirth = parser[3].trim()
            gender = parser[4].trim()
            placeOfResidence = parser[5].trim()
            dateOfIssue = parser[6].trim()
        }
        if(parser.size == 11){
            informationQrCode.fatherName = parser[8].trim()
            informationQrCode.motherName = parser[9].trim()
        }

        return informationQrCode
    }

}

interface QRCodeCallback {
    fun onResult(result: List<Barcode>);
}
