package com.gtel.ekyc.models

import java.io.Serializable

/**
 * Contains data from QRCODE Eid
 */
open class BasicInformation :Serializable {
    /** 12 digit of the ID */
    var eidNumber:String?=null
    /** Old id number */
    var oldEidNumber:String?=null
    var fullName:String?=null
    var dateOfBirth:String?=null
    var gender:String?=null
    var placeOfResidence:String?=null
    var dateOfIssue:String?=null
    /** This field is not available in the QR code */
    var dateOfExpiry:String?=null

    var fatherName:String?=null
    var motherName:String?=null

    override fun toString(): String {
        return "BasicInformation(eidNumber=$eidNumber, oldEidNumber=$oldEidNumber, fullName=$fullName, dateOfBirth=$dateOfBirth, gender=$gender, placeOfResidence=$placeOfResidence, dateOfIssue=$dateOfIssue, dateOfExpiry=$dateOfExpiry, fatherName=$fatherName, motherName=$motherName)"
    }
}