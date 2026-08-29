package com.trebuchetdynamics.androideyeview.core.map

import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object SyntheticContactAnimator {
    private const val EARTH_RADIUS_METERS = 6_371_008.8
    private const val MIN_SPEED_METERS_PER_SECOND = 80
    private const val SPEED_VARIANTS = 171

    fun tick(
        entities: List<MapEntity>,
        elapsedSeconds: Double,
    ): List<MapEntity> {
        require(elapsedSeconds.isFinite() && elapsedSeconds >= 0.0) {
            "Elapsed seconds must be finite and non-negative."
        }
        if (elapsedSeconds == 0.0) return entities.toList()

        return entities.map { entity ->
            val speedMetersPerSecond = MIN_SPEED_METERS_PER_SECOND +
                Math.floorMod(entity.id.hashCode(), SPEED_VARIANTS)
            val distanceMeters = speedMetersPerSecond * elapsedSeconds
            entity.copy(position = destination(entity.position, entity.headingDegrees, distanceMeters))
        }
    }

    private fun destination(
        origin: GeoPoint,
        headingDegrees: Double,
        distanceMeters: Double,
    ): GeoPoint {
        val latitude = Math.toRadians(origin.latitude)
        val longitude = Math.toRadians(origin.longitude)
        val bearing = Math.toRadians(headingDegrees)
        val angularDistance = distanceMeters / EARTH_RADIUS_METERS

        val destinationLatitude = asin(
            sin(latitude) * cos(angularDistance) +
                cos(latitude) * sin(angularDistance) * cos(bearing),
        )
        val destinationLongitude = longitude + atan2(
            sin(bearing) * sin(angularDistance) * cos(latitude),
            cos(angularDistance) - sin(latitude) * sin(destinationLatitude),
        )

        return origin.copy(
            latitude = Math.toDegrees(destinationLatitude),
            longitude = normalizeLongitude(Math.toDegrees(destinationLongitude)),
        )
    }

    private fun normalizeLongitude(longitude: Double): Double =
        ((longitude + 540.0) % 360.0) - 180.0
}
