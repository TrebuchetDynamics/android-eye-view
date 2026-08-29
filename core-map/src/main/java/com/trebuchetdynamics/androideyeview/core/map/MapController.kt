package com.trebuchetdynamics.androideyeview.core.map

interface MapController : AutoCloseable {
    fun renderEntities(entities: List<MapEntity>)

    fun removeEntities(entityIds: Set<String>)

    fun renderModel(entity: MapEntity, modelUri: String)

    fun renderPolyline(polyline: MapPolyline)

    fun setCamera(camera: MapCamera)

    fun stopCameraMotion()

    override fun close()
}
