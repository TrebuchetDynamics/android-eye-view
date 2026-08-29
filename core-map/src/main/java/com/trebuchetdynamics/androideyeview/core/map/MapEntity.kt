package com.trebuchetdynamics.androideyeview.core.map

enum class EntityKind {
    AIRCRAFT,
}

data class MapEntity(
    val id: String,
    val position: GeoPoint,
    val headingDegrees: Double,
    val label: String?,
    val kind: EntityKind,
) {
    init {
        require(id.isNotBlank()) { "Entity ID must not be blank." }
        require(headingDegrees.isFinite() && headingDegrees >= 0.0 && headingDegrees < 360.0) {
            "Heading must be finite and within [0, 360)."
        }
    }
}
