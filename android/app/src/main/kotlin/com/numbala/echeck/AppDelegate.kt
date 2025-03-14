package com.gtel.ekyc

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

@SuppressLint("StaticFieldLeak")
val APPDELEGATE = AppDelegate.shared

class AppDelegate : Application() {

    var activity: Activity? = null
    val context: Context get() = shared.applicationContext

    init { shared = this }

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    companion object {
        val TAG = AppDelegate::class.java.simpleName
        @SuppressLint("StaticFieldLeak")
        @get:Synchronized
        var shared: AppDelegate = AppDelegate()
    }
}