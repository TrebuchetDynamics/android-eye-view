package com.trebuchetdynamics.androideyeview.core.map

object FollowCamera {
    fun forEntity(
        entity: MapEntity,
        reducedMotion: Boolean,
    ): MapCamera = MapCamera(
        center = entity.position,
        headingDegrees = normalizeHeading(entity.headingDegrees),
        tiltDegrees = if (reducedMotion) 58.0 else 72.0,
        rangeMeters = if (reducedMotion) 2_200.0 else 1_500.0,
        rollDegrees = 0.0,
    )

    private fun normalizeHeading(headingDegrees: Double): Double =
        ((headingDegrees % 360.0) + 360.0) % 360.0
}
