package com.realwear.samplewebapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume

/**
 * Helper class to use RealWear's Barcode Reader.
 */
class BarcodeReaderHelper {
    companion object {
        const val EXTRA_TEXT_VALUE = "VALUE"
        const val EXTRA_CURRENT_TEXT = "CURRENT_TEXT"

        const val BARCODE_INTENT = "com.realwear.barcodereader.intent.action.SCAN_BARCODE"
        const val EXTRA_BARCODE_RESULT = "com.realwear.barcodereader.intent.extra.RESULT"
        const val BARCODE_USER_CONTEXT = "com.realwear.barcodereader.intent.extra.CONTEXT"

        /**
         * Create a barcode reader launcher.
         *
         * Should be placed in the init code of your main activity. To launch the barcode reader when
         * needed:
         * ```
         * val intent = Intent(BARCODE_INTENT)
         * intent.putExtra(BARCODE_USER_CONTEXT, userContext)
         * launcher.launch(intent)
         * ```
         * @param activity The activity to launch from.
         * @param userContext The context of why someone is scanning. e.g. 'Scan a delivery QR code'
         * @param currentText Text to append any result to, if applicable.
         * @param resultReceiver The callback after a result is found.
         */
        fun prepareBarcodeReaderLauncher(
            activity: ComponentActivity,
            userContext: String,
            currentText: String,
            resultReceiver: ResultReceiver
        ): ActivityResultLauncher<Intent> {
            return activity.registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            )
            { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // Process successful scan
                    result.data?.getStringExtra(EXTRA_BARCODE_RESULT)?.let {
                        val bundle = Bundle()
                        bundle.putString(EXTRA_TEXT_VALUE, it)
                        bundle.putString(EXTRA_CURRENT_TEXT, currentText)
                        resultReceiver.send(result.resultCode, bundle)
                    }
                }
            }
        }

        /**
         * Returns the barcode scan result to a coroutine.
         */
        class CoroutineResultReceiver(
            var continuation: Continuation<String?>?,
            handler: Handler
        ) : ResultReceiver(handler) {
            override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                super.onReceiveResult(resultCode, resultData)

                continuation?.let {
                    val barcodeValue = resultData?.getString(EXTRA_TEXT_VALUE)

                    barcodeValue ?: run {
                        it.resume(null)
                        return
                    }

                    val value = resultData.getString(EXTRA_CURRENT_TEXT, "") + barcodeValue

                    it.resume(value)
                }
                    ?: run {
                        Log.e(
                            "BarcodeReaderHelper",
                            "No continuation provided, result cannot be delivered."
                        )
                        return
                    }
            }
        }
    }
}