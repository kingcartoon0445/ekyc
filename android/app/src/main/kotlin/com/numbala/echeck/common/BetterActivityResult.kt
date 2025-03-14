package com.gtel.ekyc.common

import android.content.Intent
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts

class BetterActivityResult<I, O> private constructor(
    private val launcher: ActivityResultLauncher<I>
) {
    private var onActivityResult: ((O) -> Unit)? = null

    fun launch(input: I, onActivityResult: ((O) -> Unit)?) {
        this.onActivityResult = onActivityResult
        launcher.launch(input)
    }

    companion object {
        fun <I, O> registerForActivityResult(
            caller: ActivityResultCaller,
            contract: ActivityResultContract<I, O>
        ): BetterActivityResult<I, O> {
            val launcher = caller.registerForActivityResult(contract) { result ->
                // Invoke the callback with the result
                // Cast `result` to `O` if necessary
                (result as? O)?.let { outcome ->
                    (this as? BetterActivityResult<I, O>)?.onActivityResult?.invoke(outcome)
                }
            }
            return BetterActivityResult(launcher)
        }

        fun registerActivityForResult(caller: ActivityResultCaller): BetterActivityResult<Intent, androidx.activity.result.ActivityResult> {
            return registerForActivityResult(caller, ActivityResultContracts.StartActivityForResult())
        }
    }
}


