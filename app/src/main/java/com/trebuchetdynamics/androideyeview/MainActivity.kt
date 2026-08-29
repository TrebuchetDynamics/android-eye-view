package com.trebuchetdynamics.androideyeview

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trebuchetdynamics.androideyeview.maps3d.MapSessionState
import com.trebuchetdynamics.androideyeview.maps3d.Maps3DHost
import com.trebuchetdynamics.androideyeview.maps3d.Maps3DSession
import com.trebuchetdynamics.androideyeview.ui.theme.AndroidEyeViewTheme

class MainActivity : ComponentActivity() {
    private val viewModel: M0FeasibilityViewModel by viewModels()
    private val mapSession = Maps3DSession()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val maps3DKeyConfigured = hasConfiguredMaps3DKey()
        setContent {
            AndroidEyeViewTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()
                val mapState by mapSession.state.collectAsStateWithLifecycle()

                LaunchedEffect(mapState, maps3DKeyConfigured) {
                    if (!maps3DKeyConfigured) {
                        viewModel.markMapFailed(
                            "Maps 3D key required. Add an Android-restricted key to secrets.properties.",
                        )
                    } else {
                        when (val current = mapState) {
                            MapSessionState.Loading -> viewModel.markMapLoading()
                            is MapSessionState.Ready -> viewModel.bindController(current.controller)
                            is MapSessionState.Failed -> viewModel.markMapFailed(current.message)
                            MapSessionState.Closed -> Unit
                        }
                    }
                }

                M0FeasibilityScreen(
                    state = state,
                    actions = M0Actions(
                        loadContacts = viewModel::loadContacts,
                        tickOnce = viewModel::tickOnce,
                        startMotion = viewModel::startMotion,
                        stopMotion = viewModel::stopMotion,
                        selectFirstAircraft = { viewModel.selectAircraft("synthetic-00001") },
                        startFollow = { viewModel.startFollow(reducedMotion = false) },
                        stopFollow = viewModel::stopFollow,
                        setSensorMode = viewModel::setSensorMode,
                    ),
                    mapContent = {
                        if (maps3DKeyConfigured) {
                            Maps3DHost(
                                session = mapSession,
                                modifier = Modifier.fillMaxSize(),
                                onUserGesture = viewModel::onUserGesture,
                                onEntityClick = viewModel::selectAircraft,
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Text("MAP KEY REQUIRED")
                            }
                        }
                    },
                )
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun hasConfiguredMaps3DKey(): Boolean {
        val applicationInfo = packageManager.getApplicationInfo(
            packageName,
            PackageManager.GET_META_DATA,
        )
        val key = applicationInfo.metaData
            ?.getString("com.google.android.geo.maps3d.API_KEY")
            ?.trim()
        return !key.isNullOrEmpty() && key != "DEFAULT_API_KEY"
    }

    override fun onDestroy() {
        if (isFinishing) {
            mapSession.close()
            viewModel.close()
        }
        super.onDestroy()
    }
}
