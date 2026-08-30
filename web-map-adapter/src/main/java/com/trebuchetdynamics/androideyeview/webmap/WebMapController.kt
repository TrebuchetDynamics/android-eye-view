package com.trebuchetdynamics.androideyeview.webmap

import com.trebuchetdynamics.androideyeview.core.map.MapCamera
import com.trebuchetdynamics.androideyeview.core.map.MapController
import com.trebuchetdynamics.androideyeview.core.map.MapEntity
import com.trebuchetdynamics.androideyeview.core.map.MapPolyline

fun interface JavascriptSink {
    fun evaluate(script: String)
}

class WebMapController(
    private val sink: JavascriptSink,
    private val encoder: MapCommandEncoder = MapCommandEncoder(),
) : MapController {
    private var closed = false

    @Synchronized
    override fun renderEntities(entities: List<MapEntity>) = send(encoder.renderEntities(entities))

    @Synchronized
    override fun removeEntities(entityIds: Set<String>) = send(encoder.removeEntities(entityIds))

    @Synchronized
    override fun renderModel(entity: MapEntity, modelUri: String) =
        send(encoder.renderSelectedAircraft(entity, modelUri))

    @Synchronized
    override fun renderPolyline(polyline: MapPolyline) = send(encoder.renderPolyline(polyline))

    @Synchronized
    override fun setCamera(camera: MapCamera) = send(encoder.setCamera(camera))

    @Synchronized
    override fun stopCameraMotion() = send(encoder.stopCameraMotion())

    @Synchronized
    override fun close() {
        if (closed) return
        sink.evaluate(script(encoder.close()))
        closed = true
    }

    private fun send(command: String) {
        if (closed) return
        sink.evaluate(script(command))
    }

    private fun script(command: String): String =
        "window.androidEyeView.applyCommand($command);"
}
