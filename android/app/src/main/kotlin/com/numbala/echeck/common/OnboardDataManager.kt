package com.gtel.ekyc.common

import android.content.Context
import android.graphics.Bitmap
import com.gtel.ekyc.models.BasicInformation
import vn.jth.xverifysdk.data.Eid
import vn.jth.xverifysdk.jmrtd.VerificationStatus
import vn.jth.xverifysdk.network.models.request.VerifyIdResquestModel
import vn.jth.xverifysdk.utils.HashedUtils
import java.io.File

val ONBOARDDATAMANAGER = OnboardDataManager.shared

class OnboardDataManager {

    var eid: Eid? = null
    var verifyIdRequestModel: VerifyIdResquestModel? = null
    var businessType: BusinessType? = null
    var onboardFaceImagePath: String? = null
    var onboardSmileImagePath: String? = null
    var isFaceMatch: String = "0"
    var mFileFront: File? = null
    var mFileBack: File? = null

    var mBitmapFront: Bitmap? = null
    var mBitmapBack: Bitmap? = null
    var mBitmapSelfie: Bitmap? = null
    var basicInformation: BasicInformation?=null

    var rawQrCode: String = "";

    var isVerified : Boolean = false

    fun clear() {
        eid = null
        verifyIdRequestModel = null
    }

    companion object {
        @get:Synchronized
        var shared: OnboardDataManager = OnboardDataManager()
    }
    fun reset() {
        eid = null
        verifyIdRequestModel = null
        businessType = null
        onboardFaceImagePath = null
        onboardSmileImagePath = null
        isFaceMatch = "0"
        mFileFront = null
        mFileBack = null
        mBitmapFront = null
        mBitmapBack = null
        mBitmapSelfie = null
        basicInformation = null
        rawQrCode = ""
        isVerified = false
    }
    fun decodeToEid(context: Context, respondsMsg:String, signature: String) : Eid? {
        eid?.verificationStatus?.setMOS(
            VerificationStatus.Verdict.SUCCEEDED,
            "Verified Matching-On-Server"
        )
        val checkSignature = HashedUtils.verifyRsaSignature(
            context,
            respondsMsg,
            signature,
            "public.pem"
        )
        if (checkSignature) {
            eid?.verificationStatus?.setSS(
                VerificationStatus.Verdict.SUCCEEDED,
                "Verified Payload Signature"
            )
        } else {
            eid?.verificationStatus?.setSS(
                VerificationStatus.Verdict.FAILED,
                "Not Verified Payload Signature"
            )
        }
        return eid
    }
}