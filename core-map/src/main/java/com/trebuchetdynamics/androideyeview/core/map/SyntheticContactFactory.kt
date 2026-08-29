package com.trebuchetdynamics.androideyeview.core.map

import java.util.Random

object SyntheticContactFactory {
    const val DEFAULT_COUNT: Int = 5_000
    const val DEFAULT_SEED: Int = 0xA11CE

    private const val MIN_LATITUDE = 25.0
    private const val MAX_LATITUDE = 49.0
    private const val MIN_LONGITUDE = -124.0
    private const val MAX_LONGITUDE = -67.0
    private const val MIN_ALTITUDE_METERS = 500.0
    private const val MAX_ALTITUDE_METERS = 12_500.0

    fun create(
        count: Int = DEFAULT_COUNT,
        seed: Int = DEFAULT_SEED,
    ): List<MapEntity> {
        require(count >= 0) { "Contact count must not be negative." }

        val random = Random(seed.toLong())
        return List(count) { index ->
            val sequence = (index + 1).toString().padStart(length = 5, padChar = '0')
            MapEntity(
                id = "synthetic-$sequence",
                position = GeoPoint(
                    latitude = random.between(MIN_LATITUDE, MAX_LATITUDE),
                    longitude = random.between(MIN_LONGITUDE, MAX_LONGITUDE),
                    altitudeMeters = random.between(MIN_ALTITUDE_METERS, MAX_ALTITUDE_METERS),
                ),
                headingDegrees = random.between(0.0, 360.0),
                label = "SIM-$sequence",
                kind = EntityKind.AIRCRAFT,
            )
        }
    }

    private fun Random.between(minimum: Double, maximum: Double): Double =
        minimum + nextDouble() * (maximum - minimum)
}
