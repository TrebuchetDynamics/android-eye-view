package com.trebuchetdynamics.androideyeview.presentation

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SensorModeTest {
    @Test
    fun onlyNormalModeHasNoSimulationDisclaimer() {
        val modesWithoutDisclaimer = SensorMode.entries.filter { it.disclaimer == null }

        assertThat(modesWithoutDisclaimer).containsExactly(SensorMode.NORMAL)
    }

    @Test
    fun thermalInspiredCopyNeverClaimsMeasuredHeat() {
        val copy = listOf(
            SensorMode.THERMAL_INSPIRED.displayName,
            SensorMode.THERMAL_INSPIRED.disclaimer,
        ).joinToString(" ").lowercase()

        assertThat(copy).doesNotContain("flir")
        assertThat(copy).doesNotContain("temperature")
        assertThat(copy).doesNotContain("heat measurement")
        assertThat(copy).contains("visual simulation")
    }

    @Test
    fun everySimulatedModeUsesTheSharedHonestyLabel() {
        SensorMode.entries
            .filterNot { it == SensorMode.NORMAL }
            .forEach { mode ->
                assertThat(mode.disclaimer).isEqualTo("Visual simulation — not sensor imagery")
            }
    }
}
