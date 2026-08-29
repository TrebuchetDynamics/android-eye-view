package com.trebuchetdynamics.androideyeview.maps3d

import android.graphics.Color
import com.google.android.gms.maps3d.model.AltitudeMode
import com.google.android.gms.maps3d.model.Camera
import com.google.android.gms.maps3d.model.CollisionBehavior
import com.google.android.gms.maps3d.model.LatLngAltitude
import com.google.android.gms.maps3d.model.MarkerOptions
import com.google.android.gms.maps3d.model.ModelOptions
import com.google.android.gms.maps3d.model.PolylineOptions
import com.google.android.gms.maps3d.model.camera
import com.google.android.gms.maps3d.model.latLngAltitude
import com.google.android.gms.maps3d.model.markerOptions
import com.google.android.gms.maps3d.model.modelOptions
import com.google.android.gms.maps3d.model.orientation
import com.google.android.gms.maps3d.model.polylineOptions
import com.google.android.gms.maps3d.model.vector3D
import com.trebuchetdynamics.androideyeview.core.map.GeoPoint
import com.trebuchetdynamics.androideyeview.core.map.MapCamera
import com.trebuchetdynamics.androideyeview.core.map.MapEntity
import com.trebuchetdynamics.androideyeview.core.map.MapPolyline

internal fun GeoPoint.toSdkPosition(): LatLngAltitude = latLngAltitude {
    latitude = this@toSdkPosition.latitude
    longitude = this@toSdkPosition.longitude
    altitude = altitudeMeters
}

internal fun MapCamera.toSdkCamera(): Camera = camera {
    center = this@toSdkCamera.center.toSdkPosition()
    heading = headingDegrees
    tilt = tiltDegrees
    range = rangeMeters
    roll = rollDegrees
}

internal fun MapEntity.toMarkerOptions(showLabel: Boolean = true): MarkerOptions = markerOptions {
    id = this@toMarkerOptions.id
    position = this@toMarkerOptions.position.toSdkPosition()
    label = if (showLabel) this@toMarkerOptions.label else null
    altitudeMode = AltitudeMode.ABSOLUTE
    collisionBehavior = CollisionBehavior.OPTIONAL_AND_HIDES_LOWER_PRIORITY
    collisionPriority = 1
    isExtruded = false
    isDrawnWhenOccluded = false
}

internal fun MapEntity.toModelOptions(modelUri: String): ModelOptions = modelOptions {
    id = "model-${this@toModelOptions.id}"
    position = this@toModelOptions.position.toSdkPosition()
    url = modelUri
    altitudeMode = AltitudeMode.ABSOLUTE
    orientation = orientation {
        heading = headingDegrees
        tilt = -90.0
        roll = 0.0
    }
    scale = vector3D {
        x = MODEL_SCALE
        y = MODEL_SCALE
        z = MODEL_SCALE
    }
}

internal fun MapPolyline.toPolylineOptions(): PolylineOptions = polylineOptions {
    id = this@toPolylineOptions.id
    path = points.map { it.toSdkPosition() }
    altitudeMode = AltitudeMode.ABSOLUTE
    strokeColor = Color.CYAN
    strokeWidth = 4.0
    outerColor = 0xA0000000.toInt()
    outerWidth = 7.0
    geodesic = true
    drawsOccludedSegments = true
    zIndex = 10
}

private const val MODEL_SCALE = 0.05
