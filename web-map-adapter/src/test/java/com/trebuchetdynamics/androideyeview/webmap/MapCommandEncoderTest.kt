package com.trebuchetdynamics.androideyeview.webmap

import com.google.common.truth.Truth.assertThat
import com.trebuchetdynamics.androideyeview.core.map.EntityKind
import com.trebuchetdynamics.androideyeview.core.map.GeoPoint
import com.trebuchetdynamics.androideyeview.core.map.MapCamera
import com.trebuchetdynamics.androideyeview.core.map.MapEntity
import com.trebuchetdynamics.androideyeview.core.map.MapPolyline
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Test

class MapCommandEncoderTest {
    private val encoder = MapCommandEncoder()

    @Test
    fun encodesAllFiveThousandEntitiesWithLongitudeLatitudeAndStableIds() {
        val entities = (1..5_000).map { entity(it) }

        val command = parse(encoder.renderEntities(entities))
        val encoded = command.getValue("entities").jsonArray

        assertThat(command.getValue("type").jsonPrimitive.content).isEqualTo("renderEntities")
        assertThat(encoded).hasSize(5_000)
        assertThat(encoded.first().jsonObject.getValue("id").jsonPrimitive.content)
            .isEqualTo("synthetic-00001")
        assertThat(encoded.first().jsonObject.getValue("longitude").jsonPrimitive.double)
            .isWithin(0.000_001).of(-122.379)
        assertThat(encoded.first().jsonObject.getValue("latitude").jsonPrimitive.double)
            .isWithin(0.000_001).of(37.6213)
    }

    @Test
    fun safelyEscapesHostileLabelsAndOmitsNullLabels() {
        val labeled = entity(1).copy(label = "</script>\n\"quoted\"")
        val unlabeled = entity(2).copy(label = null)

        val entities = parse(encoder.renderEntities(listOf(labeled, unlabeled)))
            .getValue("entities").jsonArray

        assertThat(entities[0].jsonObject.getValue("label").jsonPrimitive.content)
            .isEqualTo("</script>\n\"quoted\"")
        assertThat(entities[1].jsonObject.containsKey("label")).isFalse()
    }

    @Test
    fun selectedAircraftUsesMarkerFallbackWithoutForwardingModelUri() {
        val encoded = encoder.renderSelectedAircraft(entity(1), "https://paid.invalid/model.glb")
        val command = parse(encoded)

        assertThat(command.getValue("type").jsonPrimitive.content)
            .isEqualTo("renderSelectedAircraft")
        assertThat(encoded).doesNotContain("model.glb")
        assertThat(encoded).doesNotContain("paid.invalid")
    }

    @Test
    fun encodesPolylineCameraRemovalStopAndClose() {
        val polyline = parse(
            encoder.renderPolyline(
                MapPolyline(
                    "trail",
                    listOf(GeoPoint(1.0, 2.0, 3.0), GeoPoint(4.0, 5.0, 6.0)),
                ),
            ),
        )
        val camera = parse(
            encoder.setCamera(
                MapCamera(GeoPoint(10.0, 20.0, 30.0), 45.0, 60.0, 1_500.0),
            ),
        )

        assertThat(polyline.getValue("points").jsonArray).hasSize(2)
        assertThat(camera.getValue("camera").jsonObject.getValue("rangeMeters").jsonPrimitive.double)
            .isEqualTo(1_500.0)
        assertThat(parse(encoder.removeEntities(setOf("a", "b"))).getValue("entityIds").jsonArray)
            .hasSize(2)
        assertThat(parse(encoder.stopCameraMotion()).getValue("type").jsonPrimitive.content)
            .isEqualTo("stopCameraMotion")
        assertThat(parse(encoder.close()).getValue("type").jsonPrimitive.content).isEqualTo("close")
    }

    private fun parse(value: String) = Json.parseToJsonElement(value).jsonObject

    private fun entity(index: Int) = MapEntity(
        id = "synthetic-${index.toString().padStart(5, '0')}",
        position = GeoPoint(37.6213, -122.379, 2_000.0),
        headingDegrees = 90.0,
        label = "SIM-${index.toString().padStart(5, '0')}",
        kind = EntityKind.AIRCRAFT,
    )
}
