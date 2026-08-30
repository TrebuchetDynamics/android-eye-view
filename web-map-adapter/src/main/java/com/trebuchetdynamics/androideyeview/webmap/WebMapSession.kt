package com.trebuchetdynamics.androideyeview.webmap

import com.trebuchetdynamics.androideyeview.core.map.MapController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface WebMapSessionState {
    data object Loading : WebMapSessionState
    data class Ready(val controller: MapController) : WebMapSessionState
    data class Failed(val message: String) : WebMapSessionState
    data object Closed : WebMapSessionState
}

class WebMapSession : AutoCloseable {
    private val mutableState = MutableStateFlow<WebMapSessionState>(WebMapSessionState.Loading)
    val state: StateFlow<WebMapSessionState> = mutableState.asStateFlow()
    private var attached = false

    @Synchronized
    fun attach(): Boolean {
        if (attached || mutableState.value != WebMapSessionState.Loading) return false
        attached = true
        return true
    }

    @Synchronized
    fun ready(controller: MapController) {
        val current = mutableState.value
        if (current is WebMapSessionState.Ready && current.controller === controller) return
        if (current != WebMapSessionState.Loading) {
            controller.close()
            return
        }
        mutableState.value = WebMapSessionState.Ready(controller)
    }

    @Synchronized
    fun fail(message: String) {
        val current = mutableState.value
        if (current is WebMapSessionState.Ready) current.controller.close()
        if (current == WebMapSessionState.Loading || current is WebMapSessionState.Ready) {
            mutableState.value = WebMapSessionState.Failed(
                message.ifBlank { "Free globe initialization failed" }.take(MAX_MESSAGE_LENGTH),
            )
        }
    }

    @Synchronized
    override fun close() {
        val previous = mutableState.value
        if (previous == WebMapSessionState.Closed) return
        if (previous is WebMapSessionState.Ready) previous.controller.close()
        mutableState.value = WebMapSessionState.Closed
    }

    companion object {
        private const val MAX_MESSAGE_LENGTH = 512
    }
}
