package com.trebuchetdynamics.androideyeview.core.map

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FollowCameraTest {
    @Test
    fun followsEntityWithNormalizedFiniteValues() {
        val entity = aircraft(heading = 359.5)

        val camera = FollowCamera.forEntity(entity, reducedMotion = false)

        assertThat(camera.center).isEqualTo(entity.position)
        assertThat(camera.headingDegrees).isWithin(0.0001).of(359.5)
        assertThat(camera.rangeMeters).isAtLeast(500.0)
        assertThat(camera.rangeMeters).isAtMost(5_000.0)
        assertThat(camera.tiltDegrees).isAtLeast(0.0)
        assertThat(camera.tiltDegrees).isAtMost(90.0)
    }

    @Test
    fun reducedMotionNeverAddsRoll() {
        val camera = FollowCamera.forEntity(aircraft(heading = 45.0), reducedMotion = true)

        assertThat(camera.rollDegrees).isEqualTo(0.0)
    }

    private fun aircraft(heading: Double) = MapEntity(
        id = "selected-aircraft",
        position = GeoPoint(37.6213, -122.3790, 2_000.0),
        headingDegrees = heading,
        label = "SIM-00001",
        kind = EntityKind.AIRCRAFT,
    )
}
