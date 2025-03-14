package com.gtel.ekyc.service
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import java.io.ByteArrayOutputStream
import com.gtel.ekyc.BuildConfig
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import org.json.JSONObject
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

class TakeCCCDService {
    companion object {
        private const val API_URL = BuildConfig.API_BASE_URL + "/ekyc/api/base64/verify-ocrid"
        private const val API_KEY = BuildConfig.API_KEY
        private const val CUSTOMER_CODE = BuildConfig.CUSTOMER_CODE

        fun bitmapToBase64JPEG(bitmap: Bitmap,quality:Int): String {
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()
            return Base64.encodeToString(byteArray, Base64.DEFAULT).replace("\n", "").replace("\r", "")
        }

        private fun createJsonRequestBody(json: JSONObject): RequestBody {
            val jsonString = json.toString()

            // Manually encode to avoid corruption
            val requestBody = jsonString.toByteArray(Charsets.UTF_8).toRequestBody("application/json".toMediaTypeOrNull())


            return requestBody
        }
        fun verifyOcr(imgFrontBase64: String, imgBackBase64: String, callback: (Boolean, Map<String, String>?, String?) -> Unit) {
            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS) // Time to establish connection
                .readTimeout(20, TimeUnit.SECONDS) // Time to read data
                .writeTimeout(20, TimeUnit.SECONDS) // Time to send data
                .build()

            val json = JSONObject().apply {
                put("code", CUSTOMER_CODE)
                put("img_front", imgFrontBase64)
                put("img_back", imgBackBase64)
            }

            val request = Request.Builder()
                .url(API_URL)
                .addHeader("x-api-key", API_KEY)
                .addHeader("os-type", "mobile")
                .post(createJsonRequestBody(json))
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    when (e) {
                        is SocketTimeoutException -> {
                            Log.e("TakeCCCDService", "Timeout Error: ${e.message}")
                            callback(false, null, "TimeOutError")
                        }

                        is UnknownHostException -> {
                            Log.e("TakeCCCDService", "No Internet Connection: ${e.message}")
                        }

                        else -> {
                            Log.e("TakeCCCDService", "Request Failed: ${e.message}")
                        }
                    }
                    callback(false, null, e.message)
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        Log.d("TakeCCCDService", " : $responseBody")
                        val jsonResponse = JSONObject(responseBody ?: "{}")
                        val errorCodeBack = jsonResponse.optJSONObject("data")?.optString("back_invalid_code", "") ?: ""
                        val errorCodeFront = jsonResponse.optJSONObject("data")?.optString("front_invalid_code", "") ?: ""
                        if(errorCodeBack == "0" && errorCodeFront == "0") {
                            callback(true, null, null)
                        }
                        else{
                            val errorMap = mapOf("back_invalid_code" to errorCodeBack, "front_invalid_code" to errorCodeFront)
                            callback(true, errorMap, null)
                        }
                    } else {
                        Log.d("TakeCCCDService error code", "Response code : ${response.code}")

                        callback(false, null, response.message)
                    }
                }
            })
        }
        fun getErrorMessage(errorCode: String): String {
            return when (errorCode) {
                "0" -> "Hình ảnh hợp lệ!"
                "1" -> "Hình ảnh giấy tờ có dấu hiệu của màn hình điện tử!"
                "2" -> "Hình ảnh giấy tờ là bản photocopy!"
                "3" -> "Trường id trên giấy tờ không đúng định dạng!"
                "5" -> "Hình ảnh giấy tờ bị mất góc!"
                "6" -> "Hình ảnh giấy tờ chụp quá sát góc!"
                "7" -> "Hình ảnh giấy tờ có 1 số trường bị che!"
                "8" -> "Hình ảnh giấy tờ thiếu dấu vân tay!"
                "9" -> "Hình ảnh giấy tờ bị mờ!"
                "10" -> "Hình ảnh giấy tờ bị chói sáng!"
                "11" -> "Thông tin trên giấy tờ có dấu hiệu bị chỉnh sửa!"
                "12" -> "Ảnh chân dung trên giấy tờ có dấu hiệu bị thay thế!"
                "13" -> "Giấy tờ tuỳ thân hết hạn!"
                "14" -> "Trường ngày sinh trên giấy tờ không đúng định dạng!"
                else -> "Hình ảnh không hợp lệ\nVui lòng chụp lại CCCD!"
            }
        }
    }

}