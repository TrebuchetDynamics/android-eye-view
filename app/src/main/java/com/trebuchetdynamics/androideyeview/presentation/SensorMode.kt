package com.trebuchetdynamics.androideyeview.presentation

private const val SIMULATION_DISCLAIMER = "Visual simulation — not sensor imagery"

enum class SensorMode(
    val displayName: String,
    val disclaimer: String?,
) {
    NORMAL("Normal", null),
    CRT("CRT", SIMULATION_DISCLAIMER),
    NVG("NVG", SIMULATION_DISCLAIMER),
    MONOCHROME("Monochrome", SIMULATION_DISCLAIMER),
    SNOW("Snow", SIMULATION_DISCLAIMER),
    THERMAL_INSPIRED("Thermal-inspired", SIMULATION_DISCLAIMER),
}
