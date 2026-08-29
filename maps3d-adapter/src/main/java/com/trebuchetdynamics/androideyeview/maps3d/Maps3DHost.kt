package com.trebuchetdynamics.androideyeview.maps3d

import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.maps3d.GoogleMap3D
import com.google.android.gms.maps3d.Map3DInitConfig
import com.google.android.gms.maps3d.Map3DView
import com.google.android.gms.maps3d.OnMap3DViewReadyCallback
import com.google.android.gms.maps3d.model.Map3DMode
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun Maps3DHost(
    session: Maps3DSession,
    modifier: Modifier = Modifier,
    onUserGesture: () -> Unit = {},
    onEntityClick: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember {
        Map3DView(context, initialConfig()).apply { onCreate(Bundle()) }
    }
    val lifecycleForwarder = remember(mapView) { MapLifecycleForwarder(mapView) }

    LaunchedEffect(mapView, session) {
        mapView.getMap3DViewAsync(object : OnMap3DViewReadyCallback {
            override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
                googleMap3D.setMapMode(Map3DMode.SATELLITE)
                val attach = {
                    session.ready(Maps3DController(googleMap3D, onEntityClick))
                    googleMap3D.setOnMapReadyListener(null)
                }
                googleMap3D.setOnMapReadyListener { _ -> attach() }
            }

            override fun onError(error: Exception) {
                session.fail(error)
            }
        })

        // The experimental SDK's readiness callback can fire only once per app lifetime.
        // A reused native map still has a valid controller after this bounded grace period.
        delay(SCENE_READY_GRACE_MILLIS)
        if (session.state.value == MapSessionState.Loading) {
            mapView.getMap3DViewAsync(object : OnMap3DViewReadyCallback {
                override fun onMap3DViewReady(googleMap3D: GoogleMap3D) {
                    session.ready(Maps3DController(googleMap3D, onEntityClick))
                }

                override fun onError(error: Exception) {
                    session.fail(error)
                }
            })
        }
    }

    DisposableEffect(lifecycleOwner, mapView, session) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> lifecycleForwarder.onStart()
                Lifecycle.Event.ON_RESUME -> lifecycleForwarder.onResume()
                Lifecycle.Event.ON_PAUSE -> lifecycleForwarder.onPause()
                Lifecycle.Event.ON_STOP -> lifecycleForwarder.onStop()
                Lifecycle.Event.ON_DESTROY -> {
                    session.close()
                    lifecycleForwarder.onDestroy()
                }
                else -> Unit
            }
        }
        val callbacks = object : ComponentCallbacks2 {
            override fun onConfigurationChanged(newConfig: Configuration) = Unit
            override fun onLowMemory() = mapView.onLowMemory()
            override fun onTrimMemory(level: Int) {
                if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW) mapView.onLowMemory()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        context.applicationContext.registerComponentCallbacks(callbacks)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            context.applicationContext.unregisterComponentCallbacks(callbacks)
            session.close()
            lifecycleForwarder.onDestroy()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier.pointerInput(onUserGesture) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    if (event.type == PointerEventType.Press) onUserGesture()
                }
            }
        },
    )
}

private fun initialConfig(): Map3DInitConfig = Map3DInitConfig.create(
    centerLat = 37.6213,
    centerLng = -122.3790,
    centerAlt = 2_000.0,
    heading = 315.0,
    tilt = 65.0,
    roll = 0.0,
    range = 45_000.0,
    minAltitude = 0.0,
    maxAltitude = 20_000_000.0,
    minHeading = 0.0,
    maxHeading = 360.0,
    minTilt = 0.0,
    maxTilt = 90.0,
    bounds = null,
    mapMode = Map3DMode.SATELLITE,
    mapId = null,
    language = Locale.getDefault().language,
    region = Locale.getDefault().country,
)

private class MapLifecycleForwarder(private val mapView: Map3DView) {
    private var destroyed = false

    fun onStart() {
        if (!destroyed) mapView.onStart()
    }

    fun onResume() {
        if (!destroyed) mapView.onResume()
    }

    fun onPause() {
        if (!destroyed) mapView.onPause()
    }

    fun onStop() {
        if (!destroyed) mapView.onStop()
    }

    fun onDestroy() {
        if (destroyed) return
        destroyed = true
        mapView.onSaveInstanceState(Bundle())
        mapView.onDestroy()
    }
}

private const val SCENE_READY_GRACE_MILLIS = 2_000L
