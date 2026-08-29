package com.trebuchetdynamics.androideyeview.core.map

class MapPolyline(
    val id: String,
    points: List<GeoPoint>,
) {
    val points: List<GeoPoint> = points.toList()

    init {
        require(id.isNotBlank()) { "Polyline ID must not be blank." }
        require(this.points.size >= 2) { "A polyline requires at least two points." }
    }

    override fun equals(other: Any?): Boolean =
        this === other || other is MapPolyline && id == other.id && points == other.points

    override fun hashCode(): Int = 31 * id.hashCode() + points.hashCode()

    override fun toString(): String = "MapPolyline(id=$id, points=$points)"
}
