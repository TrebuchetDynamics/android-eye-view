package com.trebuchetdynamics.androideyeview.webmap

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WebMapBridgeTest {
    @Test
    fun marshalsCallbacksThroughMainPoster() {
        val queued = mutableListOf<() -> Unit>()
        val events = mutableListOf<String>()
        val bridge = WebMapBridge(
            postToMain = queued::add,
            callbacks = WebMapCallbacks(
                onReady = { events += "ready" },
                onError = { events += "error:$it" },
                onUserGesture = { events += "gesture" },
                onEntityClick = { events += "click:$it" },
                onMetric = { name, value -> events += "$name:$value" },
            ),
        )

        bridge.onReady()
        bridge.onError("bad")
        bridge.onUserGesture()
        bridge.onEntityClick("synthetic-00001")
        bridge.onMetric("contacts-render-ms", 42.5)

        assertThat(events).isEmpty()
        queued.forEach { it() }
        assertThat(events).containsExactly(
            "ready",
            "error:bad",
            "gesture",
            "click:synthetic-00001",
            "contacts-render-ms:42.5",
        ).inOrder()
    }

    @Test
    fun rejectsInvalidCallbackPayloadsAndIgnoresCallbacksAfterClose() {
        val events = mutableListOf<String>()
        var closed = false
        val bridge = WebMapBridge(
            postToMain = { it() },
            callbacks = WebMapCallbacks(
                onReady = { events += "ready" },
                onError = { events += "error" },
                onUserGesture = { events += "gesture" },
                onEntityClick = { events += "click" },
                onMetric = { _, _ -> events += "metric" },
            ),
            isClosed = { closed },
        )

        bridge.onEntityClick("")
        bridge.onEntityClick("x".repeat(129))
        bridge.onMetric("unknown", Double.NaN)
        closed = true
        bridge.onReady()

        assertThat(events).isEmpty()
    }
}
