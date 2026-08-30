package com.trebuchetdynamics.androideyeview.webmap

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun WebMapHost(
    session: WebMapSession,
    modifier: Modifier = Modifier,
    onUserGesture: () -> Unit,
    onEntityClick: (String) -> Unit,
    onMetric: (String, Double) -> Unit = { _, _ -> },
    onError: (String) -> Unit = {},
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val currentGesture = rememberUpdatedState(onUserGesture)
    val currentClick = rememberUpdatedState(onEntityClick)
    val currentMetric = rememberUpdatedState(onMetric)
    val currentError = rememberUpdatedState(onError)

    AndroidView(
        factory = { context ->
            SecureWebMapView(
                context = context,
                session = session,
                lifecycle = lifecycle,
                callbacks = WebMapCallbacks(
                    onReady = {},
                    onError = { currentError.value(it) },
                    onUserGesture = { currentGesture.value() },
                    onEntityClick = { currentClick.value(it) },
                    onMetric = { name, value -> currentMetric.value(name, value) },
                ),
            )
        },
        modifier = modifier,
        onRelease = { view -> view.shutdown() },
    )
}
