package com.trebuchetdynamics.androideyeview.maps3d

import android.os.Handler
import android.os.Looper
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.model.Marker
import com.google.android.gms.maps3d.model.Model
import com.google.android.gms.maps3d.model.Polyline
import com.trebuchetdynamics.androideyeview.core.map.MapCamera
import com.trebuchetdynamics.androideyeview.core.map.MapController
import com.trebuchetdynamics.androideyeview.core.map.MapEntity
import com.trebuchetdynamics.androideyeview.core.map.MapPolyline

class Maps3DController(
    private val map: GoogleMap3D,
    private val onEntityClick: (String) -> Unit = {},
) : MapController {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val markers = linkedMapOf<String, Marker>()
    private val models = linkedMapOf<String, Model>()
    private val polylines = linkedMapOf<String, Polyline>()
    private var entitySnapshot = emptyList<MapEntity>()
    private var closed = false

    override fun renderEntities(entities: List<MapEntity>) {
        checkOpen()
        val diff = EntityDiff.calculate(entitySnapshot, entities)
        removeEntities(diff.removedIds)
        diff.updated.forEach(::updateMarker)
        diff.added.forEach(::addMarker)
        entitySnapshot = entities.toList()
    }

    override fun removeEntities(entityIds: Set<String>) {
        if (closed) return
        entityIds.forEach { id -> markers.remove(id)?.remove() }
        if (entityIds.isNotEmpty()) {
            entitySnapshot = entitySnapshot.filterNot { it.id in entityIds }
        }
    }

    override fun renderModel(entity: MapEntity, modelUri: String) {
        checkOpen()
        val modelId = "model-${entity.id}"
        val existing = models[modelId]
        if (existing == null) {
            map.addModel(entity.toModelOptions(modelUri)).also { model ->
                model.setClickListener { dispatchEntityClick(entity.id) }
                models[modelId] = model
            }
        } else {
            existing.setPosition(entity.position.toSdkPosition())
            existing.setOrientation(entity.toModelOptions(modelUri).orientation)
            if (existing.url != modelUri) existing.setUrl(modelUri)
        }
    }

    override fun renderPolyline(polyline: MapPolyline) {
        checkOpen()
        val existing = polylines[polyline.id]
        if (existing == null) {
            polylines[polyline.id] = map.addPolyline(polyline.toPolylineOptions())
        } else {
            existing.setPath(polyline.points.map { it.toSdkPosition() })
        }
    }

    override fun setCamera(camera: MapCamera) {
        checkOpen()
        map.setCamera(camera.toSdkCamera())
    }

    override fun stopCameraMotion() {
        if (!closed) map.stopCameraAnimation()
    }

    override fun close() {
        if (closed) return
        closed = true
        markers.values.forEach(Marker::remove)
        models.values.forEach(Model::remove)
        polylines.values.forEach(Polyline::remove)
        markers.clear()
        models.clear()
        polylines.clear()
        entitySnapshot = emptyList()
        map.stopCameraAnimation()
        map.setCameraChangedListener(null)
        map.setCameraAnimationEndListener(null)
        map.setOnMapSteadyListener(null)
        map.setMap3DClickListener(null)
    }

    private fun addMarker(entity: MapEntity) {
        map.addMarker(entity.toMarkerOptions())?.also { marker ->
            marker.setClickListener { dispatchEntityClick(entity.id) }
            markers[entity.id] = marker
        }
    }

    private fun updateMarker(entity: MapEntity) {
        val marker = markers[entity.id]
        if (marker == null) {
            addMarker(entity)
            return
        }
        marker.setPosition(entity.position.toSdkPosition())
        marker.setLabel(entity.label)
    }

    private fun dispatchEntityClick(id: String) {
        mainHandler.post { onEntityClick(id) }
    }

    private fun checkOpen() {
        check(!closed) { "Maps3DController is closed" }
    }
}
