package com.gtel.ekyc.common

enum class BusinessType(val type: Int) {
    VERIFY_EID(0),
    VERIFY_EID_EKYC(1),
    VERIFY_EID_BITMAP(3)
}