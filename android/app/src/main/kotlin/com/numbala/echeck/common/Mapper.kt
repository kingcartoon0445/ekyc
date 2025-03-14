package com.gtel.ekyc.common

import android.graphics.Bitmap
import org.json.JSONObject
import vn.jth.xverifysdk.data.Eid
import vn.jth.xverifysdk.jmrtd.VerificationStatus
import java.io.ByteArrayOutputStream

class Mapper {


    fun eidToMap(eid: Eid) : Map<String, Any?>? {
        if(eid.personOptionalDetails ==null)
            return null

        var map = mutableMapOf<String, Any?> (
            "dateOfBirth" to eid.personOptionalDetails!!.dateOfBirth,
            "dateOfExpiry" to eid.personOptionalDetails!!.dateOfExpiry,
            "dateOfIssue" to eid.personOptionalDetails!!.dateOfIssue,
            "eidNumber" to eid.personOptionalDetails!!.eidNumber,
            "ethnicity" to eid.personOptionalDetails!!.ethnicity,
            "fatherName" to eid.personOptionalDetails!!.fatherName,
            "fullName" to eid.personOptionalDetails!!.fullName,
            "gender" to eid.personOptionalDetails!!.gender,
            "motherName" to eid.personOptionalDetails!!.motherName,
            "nationality" to eid.personOptionalDetails!!.nationality,
            "oldEidNumber" to eid.personOptionalDetails!!.oldEidNumber,
            "personalIdentification" to eid.personOptionalDetails!!.personalIdentification,
            "placeOfOrigin" to eid.personOptionalDetails!!.placeOfOrigin,
            "placeOfResidence" to eid.personOptionalDetails!!.placeOfResidence,
            "religion" to eid.personOptionalDetails!!.religion,
            "unkIdNumber" to eid.personOptionalDetails!!.unkIdNumber,
            "spouseName" to eid.personOptionalDetails!!.spouseName,
            "unkInfo" to eid.personOptionalDetails!!.unkInfo,
            "verificationStatus" to verificatinStatus(eid.verificationStatus!!)
        )

        if (eid.portrait !=null) {
            val byteArrayOutputStream = ByteArrayOutputStream()
            eid.portrait!!.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val bytes = byteArrayOutputStream.toByteArray()
            map.put( "portrait" , bytes)
        }
        if (eid.faceImage !=null) {

            val byteArrayOutputStream = ByteArrayOutputStream()
            eid.faceImage!!.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val bytes = byteArrayOutputStream.toByteArray()
            map.put( "face" , bytes)
        }
        return map
    }


    fun verificatinStatus(verificationStatus: VerificationStatus) : String{
        val jsonObject = JSONObject()
        jsonObject.put("aa",verificationStatus.aa?.name)
        jsonObject.put("aaReason",verificationStatus.aaReason)
        jsonObject.put("bac",verificationStatus.bac?.name)
        jsonObject.put("bacReason",verificationStatus.bacReason)
        jsonObject.put("ca",verificationStatus.ca?.name)
        jsonObject.put("caReason",verificationStatus.caReason)
        jsonObject.put("cs",verificationStatus.cs?.name)
        jsonObject.put("csReason",verificationStatus.caReason)
        jsonObject.put("ds",verificationStatus.ds?.name)
        jsonObject.put("dsReason",verificationStatus.dsReason)
        jsonObject.put("eac",verificationStatus.eac?.name)
        jsonObject.put("mos",verificationStatus.mos?.name)
        jsonObject.put("mosReason",verificationStatus.mosReason)
        jsonObject.put("pa",verificationStatus.pa?.name)
        jsonObject.put("paReason",verificationStatus.paReason)
        jsonObject.put("sac",verificationStatus.sac?.name)
        jsonObject.put("sacReason",verificationStatus.sacReason)
        jsonObject.put("ss",verificationStatus.ss?.name)
        jsonObject.put("ssReason",verificationStatus.ssReason)
        return jsonObject.toString()
    }
}