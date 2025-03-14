package com.gtel.ekyc.common

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.gtel.ekyc.common.BusinessType

class DashboardItem(
    var id: Int,
    var businessType: BusinessType,
    var title: String,
    @field:DrawableRes var icon: Int,
    @field:ColorRes var background: Int,
    var isEnable : Boolean) {
}
