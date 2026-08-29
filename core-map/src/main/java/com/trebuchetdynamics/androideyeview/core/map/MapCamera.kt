package com.trebuchetdynamics.androideyeview.core.map

data class MapCamera(
    val center: GeoPoint,
    val headingDegrees: Double,
    val tiltDegrees: Double,
    val rangeMeters: Double,
    val rollDegrees: Double = 0.0,
) {
    init {
        require(headingDegrees.isFinite() && headingDegrees >= 0.0 && headingDegrees < 360.0) {
            "Heading must be finite and within [0, 360)."
        }
        require(tiltDegrees.isFinite() && tiltDegrees in 0.0..90.0) {
            "Tilt must be finite and within [0, 90]."
        }
        require(rangeMeters.isFinite() && rangeMeters > 0.0) {
            "Range must be finite and positive."
        }
        require(rollDegrees.isFinite() && rollDegrees in -180.0..180.0) {
            "Roll must be finite and within [-180, 180]."
        }
    }
}
