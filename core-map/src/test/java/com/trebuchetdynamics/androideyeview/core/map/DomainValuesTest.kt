package com.trebuchetdynamics.androideyeview.core.map

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class DomainValuesTest {
    @Test
    fun geoPointAcceptsBoundaryCoordinatesAndUsesValueSemantics() {
        val first = GeoPoint(latitude = -90.0, longitude = 180.0, altitudeMeters = 1_234.5)
        val second = GeoPoint(latitude = -90.0, longitude = 180.0, altitudeMeters = 1_234.5)

        assertThat(first).isEqualTo(second)
        assertThat(first.copy(latitude = 90.0).latitude).isEqualTo(90.0)
    }

    @Test
    fun geoPointRejectsInvalidOrNonFiniteCoordinates() {
        assertThrows(IllegalArgumentException::class.java) { GeoPoint(-90.1, 0.0, 0.0) }
        assertThrows(IllegalArgumentException::class.java) { GeoPoint(90.1, 0.0, 0.0) }
        assertThrows(IllegalArgumentException::class.java) { GeoPoint(0.0, -180.1, 0.0) }
        assertThrows(IllegalArgumentException::class.java) { GeoPoint(0.0, 180.1, 0.0) }
        assertThrows(IllegalArgumentException::class.java) { GeoPoint(Double.NaN, 0.0, 0.0) }
        assertThrows(IllegalArgumentException::class.java) { GeoPoint(0.0, 0.0, Double.POSITIVE_INFINITY) }
    }

    @Test
    fun mapValuesRejectBlankIdsAndInvalidHeadings() {
        val point = GeoPoint(30.0, -97.0, 3_000.0)

        assertThrows(IllegalArgumentException::class.java) {
            MapEntity("", point, 90.0, "SIM-1", EntityKind.AIRCRAFT)
        }
        assertThrows(IllegalArgumentException::class.java) {
            MapEntity("contact-1", point, Double.NaN, "SIM-1", EntityKind.AIRCRAFT)
        }
        assertThrows(IllegalArgumentException::class.java) {
            MapEntity("contact-1", point, 360.0, "SIM-1", EntityKind.AIRCRAFT)
        }
        assertThrows(IllegalArgumentException::class.java) {
            MapPolyline("", listOf(point, point.copy(longitude = -96.9)))
        }
    }

    @Test
    fun polylineDefensivelyCopiesPointsAndUsesValueSemantics() {
        val firstPoint = GeoPoint(30.0, -97.0, 1_000.0)
        val secondPoint = firstPoint.copy(longitude = -96.9)
        val mutablePoints = mutableListOf(firstPoint, secondPoint)
        val polyline = MapPolyline("trail-1", mutablePoints)

        mutablePoints.clear()

        assertThat(polyline.points).containsExactly(firstPoint, secondPoint).inOrder()
        assertThat(polyline).isEqualTo(MapPolyline("trail-1", listOf(firstPoint, secondPoint)))
    }

    @Test
    fun mapCameraValidatesFiniteOrientationAndPositiveRange() {
        val center = GeoPoint(30.0, -97.0, 1_000.0)
        val camera = MapCamera(center, headingDegrees = 359.9, tiltDegrees = 45.0, rangeMeters = 2_000.0)

        assertThat(camera.center).isEqualTo(center)
        assertThrows(IllegalArgumentException::class.java) {
            MapCamera(center, Double.NaN, 45.0, 2_000.0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            MapCamera(center, 0.0, Double.POSITIVE_INFINITY, 2_000.0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            MapCamera(center, 0.0, 45.0, 0.0)
        }
    }
}
