package com.trebuchetdynamics.androideyeview.core.map

data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double,
) {
    init {
        require(latitude.isFinite() && latitude in -90.0..90.0) {
            "Latitude must be finite and within [-90, 90]."
        }
        require(longitude.isFinite() && longitude in -180.0..180.0) {
            "Longitude must be finite and within [-180, 180]."
        }
        require(altitudeMeters.isFinite()) { "Altitude must be finite." }
    }
}
