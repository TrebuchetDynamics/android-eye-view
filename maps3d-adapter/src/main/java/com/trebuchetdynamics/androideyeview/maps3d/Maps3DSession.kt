package com.trebuchetdynamics.androideyeview.maps3d

import com.trebuchetdynamics.androideyeview.core.map.MapController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface MapSessionState {
    data object Loading : MapSessionState
    data class Ready(val controller: MapController) : MapSessionState
    data class Failed(val message: String) : MapSessionState
    data object Closed : MapSessionState
}

class Maps3DSession : AutoCloseable {
    private val mutableState = MutableStateFlow<MapSessionState>(MapSessionState.Loading)
    val state: StateFlow<MapSessionState> = mutableState.asStateFlow()

    @Synchronized
    fun ready(controller: MapController) {
        if (mutableState.value != MapSessionState.Loading) {
            controller.close()
            return
        }
        mutableState.value = MapSessionState.Ready(controller)
    }

    @Synchronized
    fun fail(error: Throwable) {
        if (mutableState.value == MapSessionState.Loading) {
            mutableState.value = MapSessionState.Failed("Map initialization failed")
        }
    }

    @Synchronized
    override fun close() {
        val previous = mutableState.value
        if (previous == MapSessionState.Closed) return
        if (previous is MapSessionState.Ready) previous.controller.close()
        mutableState.value = MapSessionState.Closed
    }
}
