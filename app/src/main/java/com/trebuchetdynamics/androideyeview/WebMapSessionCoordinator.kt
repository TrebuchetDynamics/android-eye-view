package com.trebuchetdynamics.androideyeview

import com.trebuchetdynamics.androideyeview.webmap.WebMapSession

internal class WebMapSessionCoordinator(
    initial: WebMapSession = WebMapSession(),
) {
    var current: WebMapSession = initial
        private set

    fun retry(): WebMapSession {
        current.close()
        return WebMapSession().also { current = it }
    }

    fun close() {
        current.close()
    }
}
