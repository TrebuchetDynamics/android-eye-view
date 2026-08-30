package com.trebuchetdynamics.androideyeview.webmap

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.webkit.CookieManager
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.webkit.WebViewAssetLoader

@SuppressLint("SetJavaScriptEnabled")
class SecureWebMapView(
    context: Context,
    private val session: WebMapSession,
    private val lifecycle: Lifecycle,
    callbacks: WebMapCallbacks,
) : WebView(context), LifecycleEventObserver {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val assetLoader = WebViewAssetLoader.Builder()
        .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(context))
        .build()
    private var destroyed = false

    init {
        check(session.attach()) { "A WebMapSession supports only one active WebView." }
        setBackgroundColor(Color.rgb(5, 10, 13))
        configureSettings()
        CookieManager.getInstance().setAcceptThirdPartyCookies(this, false)

        val controller = WebMapController(
            JavascriptSink { script ->
                post {
                    if (!destroyed) evaluateJavascript(script, null)
                }
            },
        )
        addJavascriptInterface(
            WebMapBridge(
                postToMain = { callback -> mainHandler.post(callback) },
                callbacks = WebMapCallbacks(
                    onReady = { session.ready(controller); callbacks.onReady() },
                    onError = { message -> session.fail(message); callbacks.onError(message) },
                    onUserGesture = callbacks.onUserGesture,
                    onEntityClick = callbacks.onEntityClick,
                    onMetric = callbacks.onMetric,
                ),
                isClosed = { destroyed || session.state.value == WebMapSessionState.Closed },
            ),
            BRIDGE_NAME,
        )
        webViewClient = LocalContentClient()
        lifecycle.addObserver(this)
        loadUrl(START_URL)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> onResume()
            Lifecycle.Event.ON_PAUSE -> onPause()
            Lifecycle.Event.ON_DESTROY -> shutdown()
            else -> Unit
        }
    }

    fun shutdown() {
        dispose(closeSession = true)
    }

    private fun dispose(closeSession: Boolean) {
        if (destroyed) {
            if (closeSession) session.close()
            return
        }
        destroyed = true
        lifecycle.removeObserver(this)
        if (closeSession) session.close()
        removeJavascriptInterface(BRIDGE_NAME)
        stopLoading()
        clearHistory()
        removeAllViews()
        destroy()
    }

    @Suppress("DEPRECATION")
    private fun configureSettings() {
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.allowFileAccess = false
        settings.allowContentAccess = false
        settings.allowFileAccessFromFileURLs = false
        settings.allowUniversalAccessFromFileURLs = false
        settings.javaScriptCanOpenWindowsAutomatically = false
        settings.setSupportMultipleWindows(false)
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        settings.setGeolocationEnabled(false)
        settings.saveFormData = false
        settings.mediaPlaybackRequiresUserGesture = true
    }

    private inner class LocalContentClient : WebViewClient() {
        override fun shouldInterceptRequest(
            view: WebView,
            request: WebResourceRequest,
        ): WebResourceResponse? = assetLoader.shouldInterceptRequest(request.url)

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean =
            request.isForMainFrame && !request.url.isTrustedAppAsset()

        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest,
            error: WebResourceError,
        ) {
            if (request.isForMainFrame && !destroyed) {
                session.fail("Local globe content failed to load")
            }
        }

        override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
            if (!destroyed) {
                session.fail(
                    if (detail.didCrash()) {
                        "Globe renderer crashed"
                    } else {
                        "Globe renderer was reclaimed by Android"
                    },
                )
                dispose(closeSession = false)
            }
            return true
        }
    }

    private fun Uri.isTrustedAppAsset(): Boolean =
        scheme == "https" && host == WebViewAssetLoader.DEFAULT_DOMAIN

    companion object {
        private const val BRIDGE_NAME = "AndroidEyeView"
        private val START_URL = Uri.Builder()
            .scheme("https")
            .authority(WebViewAssetLoader.DEFAULT_DOMAIN)
            .appendPath("assets")
            .appendPath("map")
            .appendPath("index.html")
            .build()
            .toString()
    }
}
