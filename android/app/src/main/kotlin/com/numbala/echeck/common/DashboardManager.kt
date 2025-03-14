package com.gtel.ekyc.common

import android.content.res.Resources
import com.gtel.ekyc.APPDELEGATE
import com.gtel.ekyc.R


val DASHBOARDMANAGER = DashboardManager.shared

class DashboardManager private constructor() {

    private val items: List<DashboardItem>

    init {
        val res: Resources = APPDELEGATE.context.resources
        items = listOf(
            DashboardItem(
                0,
                BusinessType.VERIFY_EID,
                res.getString(R.string.title_verify_eid),
                R.drawable.ic_nfc,
                R.color.white,
                true
            ),
        )
    }

    fun getListDashboard(): List<DashboardItem> {
        return items
    }

    companion object {
        @get:Synchronized
        var shared: DashboardManager = DashboardManager()
    }
}