package com.gtel.ekyc.common

import android.annotation.SuppressLint
import android.content.Context
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.nfc.Tag
import android.os.Handler
import android.os.Looper
import android.text.format.DateUtils
import com.gtel.ekyc.models.BasicInformation
import io.reactivex.disposables.Disposable
import net.sf.scuba.data.Gender
import org.jmrtd.lds.icao.MRZInfo
import vn.jth.xverifysdk.card.EidFacade
import vn.jth.xverifysdk.card.InvalidateError
import vn.jth.xverifysdk.card.ParamInvalidateException
import vn.jth.xverifysdk.utils.StringUtils
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object EidFacadeLocal {

    private var isDecoding = false
    private var isProcessSuccess = false
    private var mHandler = Handler(Looper.getMainLooper())
    var basicInformation: BasicInformation? = null
        private set

    // ========================================
    // region Private
    // ========================================


    /**
     * This function processes MRZ when the information is known
     * Create MRZInfo with document type ID by default
     * We don't need to calculate expiry date
     * @param eidNumber: 12 or 9 digit of the ID
     * @param dateOfBirth:Date of birth in the format ddMMyyyy, dd/MM/yyyy
     * @param dateOfExpiry: Date of expiry in the format ddMMyyyy, dd/MM/yyyy
     * @return MRZInfo
     */
    fun createMrzInfo(eidNumber: String?, dateOfBirth: String?, dateOfExpiry: String?): MRZInfo {
        if (eidNumber == null) {
            throw ParamInvalidateException(InvalidateError.INVALIDATE_EID_NUMBER)
        }
        if (dateOfBirth == null) {
            throw ParamInvalidateException(InvalidateError.INVALIDATE_DOB)
        }
        if (dateOfExpiry == null) {
            throw ParamInvalidateException(InvalidateError.INVALIDATE_DOE)
        }
        var eid: String? = null
        var dob: String? = null
        var doe: String? = null

        eid = if (eidNumber.length == 12) {
            eidNumber.takeLast(9)
        } else if (eidNumber.length == 9) {
            eidNumber
        } else {
            throw ParamInvalidateException(InvalidateError.INVALIDATE_EID_NUMBER_FORMAT)
        }

        dob = if (dateOfBirth.length == 10) {
            formatDateString(dateOfBirth, "dd/MM/yyyy", "yyMMdd")
        } else if (dateOfBirth.length == 8) {
            formatDateString(dateOfBirth, "ddMMyyyy", "yyMMdd")
        } else if (dateOfBirth.length == 6) {
            dateOfBirth
        } else {
            throw ParamInvalidateException(InvalidateError.INVALIDATE_DOB_FORMAT)
        }

        doe = if(dateOfExpiry.length == 10) {
            formatDateString(dateOfExpiry, "dd/MM/yyyy", "yyMMdd")
        } else if (dateOfExpiry.length == 8) {
            formatDateString(dateOfExpiry, "ddMMyyyy", "yyMMdd")
        }else if (dateOfExpiry.length == 6) {
            dateOfExpiry
        }else{
            throw ParamInvalidateException(InvalidateError.INVALIDATE_DOE_FORMAT)
        }

        return MRZInfo.createTD1MRZInfo(
            "I",
            "",
            eid,
            "",
            dob,
            Gender.UNKNOWN,
            doe,
            "",
            "",
            "",
            "",
        )
    }

    /**
     * This func should be used for scan qrcode
     * @param eidNumber:  12 digit of the ID
     * @param dateOfBirth: your date of birth ddMMyyyy
     * @param dateOfIssue: date of issue the card ddMMyyyy
     */
    fun createMrz(eidNumber: String?, dateOfBirth: String?, dateOfIssue: String): MRZInfo {
        if (eidNumber?.length != 12) {
            throw ParamInvalidateException(InvalidateError.INVALIDATE_EID_NUMBER)
        }
        if (dateOfBirth?.length != 8) {
            throw ParamInvalidateException(InvalidateError.INVALIDATE_DOB)
        }
        if (dateOfIssue.length != 8) {
            throw ParamInvalidateException(InvalidateError.INVALIDATE_DOB)
        }

        val documentNumber = eidNumber.takeLast(9)
        val dobDate = stringToDate(dateOfBirth, "ddMMyyyy")
            ?: throw ParamInvalidateException(InvalidateError.INVALIDATE_DOB_FORMAT)
        val doiDate =
            stringToDate(dateOfIssue, "ddMMyyyy") ?: throw ParamInvalidateException(
                InvalidateError.INVALIDATE_DOE_FORMAT
            )
        try {
            val (mrzDob, mrzDoe) = calculateExpiry(dobDate, doiDate)
            return createMrzInfo(documentNumber, mrzDob, mrzDoe)
        } catch (e: Exception) {
            throw e
        }
    }

    /**
     * This func calculate time and return date of birth and date of expiry form yyMMdd
     * @param dob  You need to transmit your date of birth - ddMMyyyy
     * @param doi: Date of issued - ddMMyyyy
     */
    /*
     * we need to calculate age making to the card, yearIssued - year of birth = card making age
     * Based on documentation, user make to identify citizen card from age 14 to 23 will expires at age 25
     * From 23 to 38 will expires at age 40
     * From 38 to 58 will expires at age 60
     * else Citizen identification cards from the age of 58 onwards will be used until the person dies.
     * current will expiry since 31/12/2099
     */
    @Throws(Exception::class)
    private fun calculateExpiry(dob: Date, doi: Date): Pair<String, String> {
        val calendar = Calendar.getInstance()
        calendar.time = doi
        val yearIssued = calendar.get(Calendar.YEAR)
        calendar.time = dob
        val yearBirth = calendar.get(Calendar.YEAR)

        val ageMakingCard = yearIssued - yearBirth

        val dateExpire: Date? = when {
            ageMakingCard >= 58 -> stringToDate("31122099", "ddMMyyyy")
            checkAgeRang60(doi, dob) -> {
                calendar.time = dob
                calendar.add(Calendar.YEAR, 60)
                calendar.time
            }

            checkAgeRang40(doi, dob) -> {
                calendar.time = dob
                calendar.add(Calendar.YEAR, 40)
                calendar.time
            }

            else -> {
                calendar.time = dob
                calendar.add(Calendar.YEAR, 25)
                calendar.time
            }
        }

        val dateExpireStr = formatDate(dateExpire, "yyMMdd") ?: ""
        val mrzDob = formatDate(dob, "yyMMdd") ?: ""
        return Pair(mrzDob, dateExpireStr)
    }

    /*
     * Check age marking card in [ 23, 38]
     * Take the year of birth plus the maximum year to make the card
     * Take the year of birth plus the minimum year to make the card
     * => We have arrangement of year to make the card [beginDate, maxDate]
     * Then, add 1 day after your birthday to calculate the time start making a new card
     */
    private fun checkAgeRang40(dateOfIssued: Date, birthDate: Date): Boolean {
        val calendar = Calendar.getInstance()
        // Calculate maxDate: birthDate + 38 years
        calendar.time = birthDate
        calendar.add(Calendar.YEAR, 38)
        val maxDate = calendar.time

        // Calculate beginDate: birthDate + 23 years
        calendar.time = birthDate
        calendar.add(Calendar.YEAR, 23)
        val beginDate = calendar.time

        // Calculate minDate: beginDate + 1 day
        calendar.time = beginDate
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val minDate = calendar.time

        // Check if passportDate is within the range [minDate, maxDate]
        return dateOfIssued in minDate..maxDate
    }

    private fun checkAgeRang60(dateOfIssued: Date, birthDate: Date): Boolean {
        val calendar = Calendar.getInstance()
        // Calculate maxDate: birthDate + 58 years
        calendar.time = birthDate
        calendar.add(Calendar.YEAR, 58)
        val maxDate = calendar.time
        // Calculate beginDate: birthDate + 38 years
        calendar.time = birthDate
        calendar.add(Calendar.YEAR, 38)
        val beginDate = calendar.time
        // Calculate minDate: beginDate + 1 day
        calendar.time = beginDate
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val minDate = calendar.time
        // Check if passportDate is within the range [minDate, maxDate]
        return dateOfIssued in minDate..maxDate
    }


    fun formatDate(date: Date?, dateFormat: String?): String? {
        @SuppressLint("SimpleDateFormat")
        val format = SimpleDateFormat(dateFormat)
        return format.format(date)
    }

    fun stringToDate(date: String, dateFormat: String?): Date? {
        return try {
            @SuppressLint("SimpleDateFormat") val format =
                SimpleDateFormat(dateFormat, Locale.getDefault())
            format.parse(date)
        } catch (ex: Exception) {
            null
        }
    }

    fun formatDateString(dateString: String?,input:String,output:String): String {
        if(dateString.isNullOrEmpty()) return ""
        val inputFormat = SimpleDateFormat(input, Locale.getDefault())
        val outputFormat = SimpleDateFormat(output, Locale.getDefault())
        val date = inputFormat.parse(dateString)
        return outputFormat.format(date)
    }

    // endregion
    // ========================================
}

