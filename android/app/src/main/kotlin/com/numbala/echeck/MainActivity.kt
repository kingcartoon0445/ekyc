package com.gtel.ekyc

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.gtel.ekyc.Stream.register
import com.gtel.ekyc.activities.common.LivenessActivity
import com.gtel.ekyc.activities.common.ScanMrzActivity
import com.gtel.ekyc.activities.vehiclescanner.ScanVehiclePlateActivity
import com.gtel.ekyc.activities.verifyeid.TakeCccdPhotoActivity
import com.gtel.ekyc.activities.verifyeid.VerifyEidMainActivity.Companion.REQUEST_LOGIN
import com.gtel.ekyc.activities.verifyeid.VerifyEidMainActivity.Companion.REQUEST_MRZ
import com.gtel.ekyc.activities.verifyeid.VerifyEidMainActivity.Companion.REQUEST_NFC
import com.gtel.ekyc.activities.verifyeid.VerifyEidMainActivity.Companion.REQUEST_VERIFY_EKYC
import com.gtel.ekyc.common.BusinessType
import com.gtel.ekyc.common.IntentData
import com.gtel.ekyc.common.ONBOARDDATAMANAGER
import com.gtel.ekyc.models.request.VerifyEidRequestModel
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import java.io.ByteArrayOutputStream
import java.util.function.Consumer
import io.flutter.embedding.android.FlutterFragmentActivity;


class MainActivity: FlutterFragmentActivity() {

    private var methodChannel: MethodChannel? = null

    companion object {
        const val REQUEST_EID = 14

        const val CHANNEL_NAME = "GLOBEDR_CHANNEL"
        //Events
        const val ON_FINISH_EID_EVENT = "ON_FINISH_EID"
        //Data keys
        const val KEY_REQ_MODEL = "REQ_MODEL"

    }

    val consumer = Consumer<Intent> { intent ->
        this@MainActivity.intent = intent

        methodChannel?.let {
            it.invokeMethod(ON_FINISH_EID_EVENT,getRequestModel(intent).toJson())
        }

    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        methodChannel = MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL_NAME)
        register(consumer)

        methodChannel?.setMethodCallHandler { call, result ->
            when (call.method) {
                "CAPTURE_EID" -> {
                    ONBOARDDATAMANAGER.reset()
                    val intent = Intent(this@MainActivity, TakeCccdPhotoActivity::class.java)
                    startActivityForResult(intent, REQUEST_MRZ)
                    result.success(null) // Optionally send a success result or handle callback in onActivityResult
                }
                "START_VERIFY_METHOD" -> {
                    ONBOARDDATAMANAGER.reset()
                    ONBOARDDATAMANAGER.businessType = BusinessType.VERIFY_EID
                    val intent = Intent(this@MainActivity, ScanMrzActivity::class.java)
                    startActivityForResult(intent, REQUEST_LOGIN)
                    result.success(null)
                }
                else -> result.notImplemented()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when(requestCode) {
                REQUEST_MRZ -> {
                    ONBOARDDATAMANAGER.businessType = BusinessType.VERIFY_EID_EKYC
                    val intent = Intent(this@MainActivity, LivenessActivity::class.java)
                    startActivityForResult(intent, REQUEST_VERIFY_EKYC)
                }
                REQUEST_LOGIN -> {
                    ONBOARDDATAMANAGER.businessType = BusinessType.VERIFY_EID_EKYC
                    val gson = Gson()
                    val jsonObject = JsonObject()
                    ONBOARDDATAMANAGER.eid?.let { it ->
                        
                        it.personOptionalDetails?.let { it2 ->
                            jsonObject.addProperty("eidNumber", it2.eidNumber ?: "")
                        }
                        jsonObject.addProperty("dg15", it.DG15FileEncode.toString())
                    }
    
                    methodChannel?.invokeMethod(ON_FINISH_EID_EVENT, gson.toJson(jsonObject))
                    return;
                }
                REQUEST_NFC -> {
                    ONBOARDDATAMANAGER.businessType = BusinessType.VERIFY_EID_EKYC
                    val intent = Intent(this@MainActivity, LivenessActivity::class.java)
                    startActivityForResult(intent, REQUEST_VERIFY_EKYC)
                }
                REQUEST_VERIFY_EKYC -> {

                    if (ONBOARDDATAMANAGER.isFaceMatch == "1") {
                        val gson = Gson()
                        val jsonString = gson.toJson(ONBOARDDATAMANAGER.verifyIdRequestModel)
                        val jsonObject = JsonObject()
                        jsonObject.addProperty("verifyIdRequestModel", jsonString)
                        ONBOARDDATAMANAGER.eid?.let { it ->
                            it.personOptionalDetails?.let { it2 ->
                                jsonObject.addProperty("id", it2.eidNumber ?: "")
                                jsonObject.addProperty("name", it2.fullName ?: "")
                                jsonObject.addProperty("dob", it2.dateOfBirth ?: "")
                                jsonObject.addProperty("expiry", it2.dateOfExpiry ?: "")
                                jsonObject.addProperty("gender", it2.gender ?: "")
                                jsonObject.addProperty("nationality", it2.nationality ?: "")
                            }
                        }
                        ONBOARDDATAMANAGER.onboardFaceImagePath?.let {
                            jsonObject.addProperty("onboardFaceImagePath", it)
                        }

                        methodChannel?.invokeMethod(ON_FINISH_EID_EVENT, gson.toJson(jsonObject))
                    }
                    else {
                        methodChannel?.invokeMethod(ON_FINISH_EID_EVENT, "FAIL_FACE_MATCHING")
                    }
                }
            }
        }
        if (ONBOARDDATAMANAGER.isVerified) {
            if (ONBOARDDATAMANAGER.isFaceMatch == "1") {
                val gson = Gson()
                val jsonString = gson.toJson(ONBOARDDATAMANAGER.verifyIdRequestModel)
                val jsonObject = JsonObject()
                jsonObject.addProperty("verifyIdRequestModel", jsonString)
                ONBOARDDATAMANAGER.eid?.let { it ->
                   
                    it.personOptionalDetails?.let { it2 ->
                        jsonObject.addProperty("eidNumber", it2.eidNumber ?: "")
                        jsonObject.addProperty("fullName", it2.fullName ?: "")
                        jsonObject.addProperty("gender", it2.gender ?: "")
                        jsonObject.addProperty("nationality", it2.nationality ?: "")
                        jsonObject.addProperty("placeOfResidence", it2.placeOfResidence ?: "")
                        jsonObject.addProperty("placeOfOrigin", it2.placeOfOrigin ?: "")
                        jsonObject.addProperty("dateOfBirth", it2.dateOfBirth ?: "")
                        jsonObject.addProperty("dateOfIssue", it2.dateOfIssue ?: "")
                        jsonObject.addProperty("dateOfExpiry", it2.dateOfExpiry ?: "")
                        jsonObject.addProperty("religion", it2.religion ?: "")
                        jsonObject.addProperty("oldEidNumber", it2.oldEidNumber ?: "")
                        jsonObject.addProperty("ethnicity", it2.ethnicity ?: "")
                        jsonObject.addProperty("unkIdNumber", it2.unkIdNumber ?: "")
                        jsonObject.addProperty("personalIdentification", it2.personalIdentification ?: "")
                        jsonObject.addProperty("fatherName", it2.fatherName ?: "")
                        jsonObject.addProperty("motherName", it2.motherName ?: "")

                    }
                    it.verificationStatus?.let { it3 ->
                        val verificationStatusObject = JsonObject().apply {
                            addProperty("pa", it3.pa.toString())
                            addProperty("aa", it3.aa.toString())
                            addProperty("ca", it3.ca.toString())
                            addProperty("mos", it3.mos.toString() )
                            addProperty("ss", it3.ss.toString())
                            addProperty("isMatch", "1")
                        }
                        jsonObject.add("verificationStatus", verificationStatusObject)
                    }
                    jsonObject.addProperty("dg15", it.DG15FileEncode.toString())
                }
                ONBOARDDATAMANAGER.onboardFaceImagePath?.let {
                    jsonObject.addProperty("onboardFaceImagePath", it)
                }
                if (ONBOARDDATAMANAGER.mBitmapFront==null){
                    methodChannel?.invokeMethod(ON_FINISH_EID_EVENT, "ID_FRONT_MISSING")
                    return;
                }
                if(ONBOARDDATAMANAGER.mBitmapBack==null){
                    methodChannel?.invokeMethod(ON_FINISH_EID_EVENT, "ID_BACK_MISSING")
                    return;
                }



                methodChannel?.invokeMethod(ON_FINISH_EID_EVENT, gson.toJson(jsonObject))
            }
            else {
                methodChannel?.invokeMethod(ON_FINISH_EID_EVENT, "FACE_NOT_MATCHED")
            }
        } else {
            methodChannel?.invokeMethod(ON_FINISH_EID_EVENT, "FAIL_FACE_MATCHING")
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun getRequestModel(intent: Intent?) : VerifyEidRequestModel {
        var requestModel: VerifyEidRequestModel? = null

        if (intent == null) {
            Log.d("TonyLog", "No intent")
        } else if (!intent.hasExtra(KEY_REQ_MODEL)) {
            Log.d("TonyLog", "No REQ_MODEL")
        } else {
            requestModel = intent.getParcelableExtra<VerifyEidRequestModel?>(KEY_REQ_MODEL)
        }
        if (requestModel == null) {
            Log.d("TonyLog", "No REQ_MODEL : NULL")
        }
        return requestModel!!;
    }
}

object Stream {

    var list = mutableListOf<Consumer<Intent>>()
    fun register(consumer: Consumer<Intent>){
        list.add(consumer)
    }

    fun enqueue(data: Intent){
        list.forEach {
            it.accept(data)
        }
    }
}

