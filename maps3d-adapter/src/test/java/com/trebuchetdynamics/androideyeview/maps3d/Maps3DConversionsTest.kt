package com.trebuchetdynamics.androideyeview.maps3d

import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.common.truth.Truth.assertThat
import com.trebuchetdynamics.androideyeview.core.map.EntityKind
import com.trebuchetdynamics.androideyeview.core.map.GeoPoint
import com.trebuchetdynamics.androideyeview.core.map.MapCamera
import com.trebuchetdynamics.androideyeview.core.map.MapEntity
import com.trebuchetdynamics.androideyeview.core.map.MapPolyline
import org.junit.Test

class Maps3DConversionsTest {
    @Test
    fun convertsCoordinatesWithoutChangingUnits() {
        val point = GeoPoint(37.6213, -122.3790, 1_250.5)

        val sdk = point.toSdkPosition()

        assertThat(sdk.latitude).isEqualTo(37.6213)
        assertThat(sdk.longitude).isEqualTo(-122.3790)
        assertThat(sdk.altitude).isEqualTo(1_250.5)
    }

    @Test
    fun convertsCameraWithoutChangingAnglesOrRange() {
        val camera = MapCamera(
            center = GeoPoint(37.6213, -122.3790, 1_000.0),
            headingDegrees = 245.0,
            tiltDegrees = 70.0,
            rangeMeters = 4_000.0,
            rollDegrees = -4.0,
        )

        val sdk = camera.toSdkCamera()

        assertThat(sdk.center.latitude).isEqualTo(37.6213)
        assertThat(sdk.heading).isEqualTo(245.0)
        assertThat(sdk.tilt).isEqualTo(70.0)
        assertThat(sdk.range).isEqualTo(4_000.0)
        assertThat(sdk.roll).isEqualTo(-4.0)
    }

    @Test
    fun createsStableAbsoluteMarkerOptions() {
        val entity = MapEntity(
            id = "contact-42",
            position = GeoPoint(37.0, -122.0, 9_000.0),
            headingDegrees = 90.0,
            label = "AEV 42",
            kind = EntityKind.AIRCRAFT,
        )

        val options = entity.toMarkerOptions(showLabel = true)

        assertThat(options.id).isEqualTo("contact-42")
        assertThat(options.label).isEqualTo("AEV 42")
        assertThat(options.altitudeMode).isEqualTo(AltitudeMode.ABSOLUTE)
        assertThat(options.position.altitude).isEqualTo(9_000.0)
    }

    @Test
    fun createsPolylineWithOriginalPath() {
        val line = MapPolyline(
            id = "trail",
            points = listOf(
                GeoPoint(37.0, -122.0, 1_000.0),
                GeoPoint(37.1, -121.9, 1_100.0),
            ),
        )

        val options = line.toPolylineOptions()

        assertThat(options.id).isEqualTo("trail")
        assertThat(options.path.map { it.altitude }).containsExactly(1_000.0, 1_100.0).inOrder()
        assertThat(options.altitudeMode).isEqualTo(AltitudeMode.ABSOLUTE)
    }
}
