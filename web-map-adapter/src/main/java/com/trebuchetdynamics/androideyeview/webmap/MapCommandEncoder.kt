package com.trebuchetdynamics.androideyeview.webmap

import com.trebuchetdynamics.androideyeview.core.map.MapCamera
import com.trebuchetdynamics.androideyeview.core.map.MapEntity
import com.trebuchetdynamics.androideyeview.core.map.MapPolyline
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class MapCommandEncoder {
    fun renderEntities(entities: List<MapEntity>): String = encode(
        "renderEntities",
        "entities" to JsonArray(entities.map(::entityJson)),
    )

    fun removeEntities(entityIds: Set<String>): String = encode(
        "removeEntities",
        "entityIds" to buildJsonArray {
            entityIds.sorted().forEach { add(JsonPrimitive(it)) }
        },
    )

    fun renderSelectedAircraft(entity: MapEntity, @Suppress("UNUSED_PARAMETER") modelUri: String): String =
        encode("renderSelectedAircraft", "entity" to entityJson(entity))

    fun renderPolyline(polyline: MapPolyline): String = encode(
        "renderPolyline",
        "id" to JsonPrimitive(polyline.id),
        "points" to buildJsonArray {
            polyline.points.forEach { point ->
                add(buildJsonObject {
                    put("latitude", point.latitude)
                    put("longitude", point.longitude)
                })
            }
        },
    )

    fun setCamera(camera: MapCamera): String = encode(
        "setCamera",
        "camera" to buildJsonObject {
            put("latitude", camera.center.latitude)
            put("longitude", camera.center.longitude)
            put("headingDegrees", camera.headingDegrees)
            put("tiltDegrees", camera.tiltDegrees)
            put("rangeMeters", camera.rangeMeters)
        },
    )

    fun stopCameraMotion(): String = encode("stopCameraMotion")

    fun close(): String = encode("close")

    private fun entityJson(entity: MapEntity): JsonObject = buildJsonObject {
        put("id", entity.id)
        put("latitude", entity.position.latitude)
        put("longitude", entity.position.longitude)
        put("altitudeMeters", entity.position.altitudeMeters)
        put("headingDegrees", entity.headingDegrees)
        entity.label?.let { put("label", it) }
    }

    private fun encode(type: String, vararg fields: Pair<String, kotlinx.serialization.json.JsonElement>): String =
        buildJsonObject {
            put("type", type)
            fields.forEach { (name, value) -> put(name, value) }
        }.toString()
}
