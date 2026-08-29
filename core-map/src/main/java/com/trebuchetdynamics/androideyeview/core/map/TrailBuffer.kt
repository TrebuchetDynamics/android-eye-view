package com.trebuchetdynamics.androideyeview.core.map

class TrailBuffer(private val maxPoints: Int = 120) {
    private val points = ArrayDeque<GeoPoint>(maxPoints)

    init {
        require(maxPoints >= 2) { "A trail must retain at least two points." }
    }

    @Synchronized
    fun append(point: GeoPoint) {
        if (points.lastOrNull() == point) return
        if (points.size == maxPoints) points.removeFirst()
        points.addLast(point)
    }

    @Synchronized
    fun snapshot(): List<GeoPoint> = points.toList()

    @Synchronized
    fun clear() = points.clear()
}
