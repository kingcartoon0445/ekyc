package com.gtel.ekyc.validators

import android.content.Context
import androidx.appcompat.widget.AppCompatEditText
import com.gtel.ekyc.R
import com.mobsandgeeks.saripaar.QuickRule

import java.util.regex.Pattern

class DocumentNumberRule : QuickRule<AppCompatEditText>() {

    override fun isValid(editText: AppCompatEditText): Boolean {
        val text = editText.text!!.toString().trim { it <= ' ' }
        val patternDate = Pattern.compile(REGEX)
        val matcherDate = patternDate.matcher(text)
        return matcherDate.find()
    }

    override fun getMessage(context: Context): String {
        return context.getString(R.string.error_validation_document_number)
    }

    companion object {

        private val REGEX = "[A-Z0-9<]{9}$"
    }
}
