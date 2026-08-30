package com.trebuchetdynamics.androideyeview.webmap

import android.webkit.JavascriptInterface

class WebMapCallbacks(
    val onReady: () -> Unit,
    val onError: (String) -> Unit,
    val onUserGesture: () -> Unit,
    val onEntityClick: (String) -> Unit,
    val onMetric: (String, Double) -> Unit,
)

class WebMapBridge(
    private val postToMain: (() -> Unit) -> Unit,
    private val callbacks: WebMapCallbacks,
    private val isClosed: () -> Boolean = { false },
) {
    @JavascriptInterface
    fun onReady() = dispatch(callbacks.onReady)

    @JavascriptInterface
    fun onError(message: String) {
        if (message.isBlank()) return
        dispatch { callbacks.onError(message.take(MAX_TEXT_LENGTH)) }
    }

    @JavascriptInterface
    fun onUserGesture() = dispatch(callbacks.onUserGesture)

    @JavascriptInterface
    fun onEntityClick(id: String) {
        if (id.isBlank() || id.length > MAX_ID_LENGTH) return
        dispatch { callbacks.onEntityClick(id) }
    }

    @JavascriptInterface
    fun onMetric(name: String, value: Double) {
        if (name !in ALLOWED_METRICS || !value.isFinite() || value < 0.0) return
        dispatch { callbacks.onMetric(name, value) }
    }

    private fun dispatch(callback: () -> Unit) {
        if (isClosed()) return
        postToMain {
            if (!isClosed()) callback()
        }
    }

    companion object {
        private const val MAX_TEXT_LENGTH = 512
        private const val MAX_ID_LENGTH = 128
        private val ALLOWED_METRICS = setOf("contacts-render-ms", "raf-p95-ms")
    }
}
