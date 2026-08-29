package com.trebuchetdynamics.androideyeview.core.map

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class SyntheticContactAnimatorTest {
    @Test
    fun positiveTickIsDeterministicAndMovesAtLeastNinetyNinePercent() {
        val source = SyntheticContactFactory.create()

        val first = SyntheticContactAnimator.tick(source, elapsedSeconds = 1.0)
        val second = SyntheticContactAnimator.tick(source, elapsedSeconds = 1.0)
        val movedCount = source.zip(first).count { (before, after) -> before.position != after.position }

        assertThat(first).isEqualTo(second)
        assertThat(movedCount).isAtLeast(4_950)
    }

    @Test
    fun tickPreservesIdentityAndMetadataWithoutMutatingTheSource() {
        val source = SyntheticContactFactory.create(count = 20)
        val originalSnapshot = source.toList()

        val moved = SyntheticContactAnimator.tick(source, elapsedSeconds = 60.0)

        assertThat(moved.map { it.id }).containsExactlyElementsIn(source.map { it.id }).inOrder()
        assertThat(moved.map { it.label }).containsExactlyElementsIn(source.map { it.label }).inOrder()
        assertThat(moved.map { it.kind }).containsExactlyElementsIn(source.map { it.kind }).inOrder()
        assertThat(moved.map { it.headingDegrees }).containsExactlyElementsIn(source.map { it.headingDegrees }).inOrder()
        assertThat(moved.map { it.position.altitudeMeters })
            .containsExactlyElementsIn(source.map { it.position.altitudeMeters })
            .inOrder()
        assertThat(source).isEqualTo(originalSnapshot)
    }

    @Test
    fun longTickKeepsEveryCoordinateValid() {
        val moved = SyntheticContactAnimator.tick(
            SyntheticContactFactory.create(),
            elapsedSeconds = 7.0 * 24.0 * 60.0 * 60.0,
        )

        assertThat(moved.all { it.position.latitude in -90.0..90.0 }).isTrue()
        assertThat(moved.all { it.position.longitude in -180.0..180.0 }).isTrue()
    }

    @Test
    fun zeroTickReturnsEquivalentEntitiesAndInvalidDurationsAreRejected() {
        val source = SyntheticContactFactory.create(count = 3)

        assertThat(SyntheticContactAnimator.tick(source, 0.0)).isEqualTo(source)
        assertThrows(IllegalArgumentException::class.java) {
            SyntheticContactAnimator.tick(source, -0.1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            SyntheticContactAnimator.tick(source, Double.NaN)
        }
    }
}
