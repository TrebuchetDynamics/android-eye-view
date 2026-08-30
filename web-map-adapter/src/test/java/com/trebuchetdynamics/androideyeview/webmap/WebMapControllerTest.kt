package com.trebuchetdynamics.androideyeview.webmap

import com.google.common.truth.Truth.assertThat
import com.trebuchetdynamics.androideyeview.core.map.EntityKind
import com.trebuchetdynamics.androideyeview.core.map.GeoPoint
import com.trebuchetdynamics.androideyeview.core.map.MapEntity
import org.junit.Test

class WebMapControllerTest {
    @Test
    fun sendsOneJsonCommandPerControllerOperation() {
        val scripts = mutableListOf<String>()
        val controller = WebMapController(JavascriptSink(scripts::add))

        controller.renderEntities(listOf(entity()))
        controller.renderModel(entity(), "ignored.glb")
        controller.stopCameraMotion()

        assertThat(scripts).hasSize(3)
        assertThat(scripts[0]).startsWith("window.androidEyeView.applyCommand(")
        assertThat(scripts[0]).contains("renderEntities")
        assertThat(scripts[1]).contains("renderSelectedAircraft")
        assertThat(scripts[2]).contains("stopCameraMotion")
    }

    @Test
    fun closeIsIdempotentAndSuppressesFutureCommands() {
        val scripts = mutableListOf<String>()
        val controller = WebMapController(JavascriptSink(scripts::add))

        controller.close()
        controller.close()
        controller.renderEntities(listOf(entity()))

        assertThat(scripts).hasSize(1)
        assertThat(scripts.single()).contains("\"close\"")
    }

    private fun entity() = MapEntity(
        id = "synthetic-00001",
        position = GeoPoint(37.6213, -122.379, 2_000.0),
        headingDegrees = 90.0,
        label = "SIM-00001",
        kind = EntityKind.AIRCRAFT,
    )
}
