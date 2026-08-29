package com.trebuchetdynamics.androideyeview.core.map

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TrailBufferTest {
    @Test
    fun keepsChronologicalPointsAtTheExactBound() {
        val buffer = TrailBuffer(maxPoints = 3)
        val points = (0..4).map { GeoPoint(37.0, -122.0 + it / 100.0, 1_000.0) }

        points.forEach(buffer::append)

        assertThat(buffer.snapshot()).containsExactly(points[2], points[3], points[4]).inOrder()
    }

    @Test
    fun suppressesConsecutiveDuplicatePositions() {
        val buffer = TrailBuffer(maxPoints = 3)
        val point = GeoPoint(37.0, -122.0, 1_000.0)

        buffer.append(point)
        buffer.append(point)

        assertThat(buffer.snapshot()).containsExactly(point)
    }

    @Test
    fun snapshotsAreImmutableCopiesAndClearRemovesEverything() {
        val buffer = TrailBuffer(maxPoints = 3)
        buffer.append(GeoPoint(37.0, -122.0, 1_000.0))
        val snapshot = buffer.snapshot()

        buffer.append(GeoPoint(38.0, -121.0, 1_100.0))
        buffer.clear()

        assertThat(snapshot).hasSize(1)
        assertThat(buffer.snapshot()).isEmpty()
    }
}
