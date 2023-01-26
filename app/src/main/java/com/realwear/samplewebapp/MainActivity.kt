package com.realwear.samplewebapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ResultReceiver
import android.util.Log
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewClientCompat
import com.realwear.samplewebapp.BarcodeReaderHelper.Companion.BARCODE_INTENT
import com.realwear.samplewebapp.BarcodeReaderHelper.Companion.BARCODE_USER_CONTEXT
import com.realwear.samplewebapp.ui.theme.SampleWebAppTheme
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.ByteArrayInputStream
import java.io.InputStream
import kotlin.coroutines.suspendCoroutine

class MainActivity : ComponentActivity() {
    val barcodeResultReceiver =
        BarcodeReaderHelper.Companion.CoroutineResultReceiver(null, Handler(Looper.getMainLooper()))
    val barcodeReaderLauncher = BarcodeReaderHelper.prepareBarcodeReaderLauncher(
        this,
        "Scan a QR code",
        "",
        barcodeResultReceiver
    )

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SampleWebAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    // NOTE: This is equivalent to
                    // override fun onCreate() {
                    //   val webView = getElementById(r.id.webview)
                    //   ...
                    // }
                    // for xml-based layouts.
                    AndroidView(
                        factory = {
                            WebView(it).apply {
                                // Configure
                                webViewClient = LocalContentWebViewClient(
                                    this@MainActivity,
                                    WebViewAssetLoader.Builder()
                                        // Search for html, css, js files in src/main/assets/assets
                                        .addPathHandler(
                                            "/assets/",
                                            WebViewAssetLoader.AssetsPathHandler(it)
                                        )
                                        // Search for images and binaries in src/main/assets/assets
                                        .addPathHandler(
                                            "/assets/",
                                            WebViewAssetLoader.ResourcesPathHandler(it)
                                        )
                                        // NOTE: Add specific paths here that locate your static website's
                                        // resources to speed up resource location in bigger projects
                                        .build()
                                )
                                webChromeClient = WebChromeClient()
                                settings.javaScriptEnabled = true

                                // Load your website
                                // NOTE: this line does not need to be changed! Your compiled website's
                                // index.html should be in src/main/assets.
                                loadUrl("https://appassets.androidplatform.net/assets/index.html")
                            }
                        }
                    )
                }
            }
        }
    }
}

private class LocalContentWebViewClient(
    private val mainActivity: MainActivity,
    private val assetLoader: WebViewAssetLoader
) :
    WebViewClientCompat() {
    @RequiresApi(21)
    override fun shouldInterceptRequest(
        view: WebView,
        request: WebResourceRequest,
    ): WebResourceResponse? {
        return when (request.url.toString()) {
            "https://qr/" -> {
                // Blocks the current thread to get result.
                val scanResult: String? = runBlocking {
                    // Create a code block that can be ran in a different thread ("coroutine context")
                    suspendCoroutine {
                        mainActivity.barcodeResultReceiver.continuation = it

                        val intent = Intent(BARCODE_INTENT)
                        intent.putExtra(BARCODE_USER_CONTEXT, "Scan a QR code")
                        mainActivity.barcodeReaderLauncher.launch(intent)
                    }
                }

                if (scanResult is String) {
                    Log.d("Scan", "String result found, returning: ${scanResult}")
                    WebResourceResponse(
                        "text/plain",
                        "UTF-8",
                        200,
                        "Success",
                        // Allow clients to read the body of this response
                        mapOf(
                            Pair("Access-Control-Allow-Origin", "*")
                        ),
                        scanResult.byteInputStream()
                    )
                } else {
                    Log.e("Scan", "Scan failed, returning failure.")
                    WebResourceResponse(
                        "text/plain",
                        "UTF-8",
                        400,
                        "Failed scan",
                        // Allow clients to read the body of this response
                        mapOf(
                            Pair("Access-Control-Allow-Origin", "*")
                        ),
                        ByteArrayInputStream(ByteArray(0))
                    )
                }
            }
            else -> assetLoader.shouldInterceptRequest(request.url)
        }
    }

    // to support API < 21
    @Deprecated("Deprecated in Java")
    override fun shouldInterceptRequest(
        view: WebView,
        url: String,
    ): WebResourceResponse? {
        return assetLoader.shouldInterceptRequest(Uri.parse(url))
    }

    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        return when (request.url.scheme) {
            "camera" -> {
                true
            }
            "thermal_camera" -> {
                true
            }
            else -> super.shouldOverrideUrlLoading(view, request)
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SampleWebAppTheme {
        Greeting("Android")
    }
}