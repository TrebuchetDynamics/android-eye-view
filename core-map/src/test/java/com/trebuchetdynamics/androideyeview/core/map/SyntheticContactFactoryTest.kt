package com.trebuchetdynamics.androideyeview.core.map

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

class SyntheticContactFactoryTest {
    @Test
    fun createsExactlyFiveThousandStableContacts() {
        val first = SyntheticContactFactory.create(5_000, 0xA11CE)
        val second = SyntheticContactFactory.create(5_000, 0xA11CE)

        assertThat(first).hasSize(5_000)
        assertThat(first.map { it.id }.toSet()).hasSize(5_000)
        assertThat(second).isEqualTo(first)
    }

    @Test
    fun distributesAircraftInsideTheNorthAmericanTestRegion() {
        val contacts = SyntheticContactFactory.create()

        assertThat(contacts).isNotEmpty()
        assertThat(contacts.all { it.kind == EntityKind.AIRCRAFT }).isTrue()
        assertThat(contacts.all { it.position.latitude in 25.0..49.0 }).isTrue()
        assertThat(contacts.all { it.position.longitude in -124.0..-67.0 }).isTrue()
        assertThat(contacts.all { it.position.altitudeMeters in 500.0..12_500.0 }).isTrue()
        assertThat(contacts.all { it.headingDegrees >= 0.0 && it.headingDegrees < 360.0 }).isTrue()
    }

    @Test
    fun validatesRequestedCount() {
        assertThrows(IllegalArgumentException::class.java) {
            SyntheticContactFactory.create(count = -1)
        }
    }
}
