package com.gtel.ekyc.models.request

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName


class VerifyEidRequestModel(
    @SerializedName("ds_cert") var dsCert: String?,
    @SerializedName("code") var code: String?,
    @SerializedName("province") var province: String?,
    @SerializedName("id_card") var idCard: String?,
    @SerializedName("device_type") var deviceType: String?,
    @SerializedName("request_id") var requestId: String?
)  :Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )



    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(dsCert)
        parcel.writeString(code)
        parcel.writeString(province)
        parcel.writeString(idCard)
        parcel.writeString(deviceType)
        parcel.writeString(requestId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VerifyEidRequestModel> {
        override fun createFromParcel(parcel: Parcel): VerifyEidRequestModel {
            return VerifyEidRequestModel(parcel)
        }

        override fun newArray(size: Int): Array<VerifyEidRequestModel?> {
            return arrayOfNulls(size)
        }
    }

    fun toJson(): String {
        val gson = Gson()
        return gson.toJson(this)
    }



}