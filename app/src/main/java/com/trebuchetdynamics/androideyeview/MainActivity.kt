package com.trebuchetdynamics.androideyeview

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.trebuchetdynamics.androideyeview.ui.theme.AndroidEyeViewTheme
import com.trebuchetdynamics.androideyeview.webmap.WebMapHost
import com.trebuchetdynamics.androideyeview.webmap.WebMapSessionState

class MainActivity : ComponentActivity() {
    private val viewModel: M0FeasibilityViewModel by viewModels()
    private val mapSessions = WebMapSessionCoordinator()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidEyeViewTheme {
                var activeMapSession by remember { mutableStateOf(mapSessions.current) }
                val state by viewModel.state.collectAsStateWithLifecycle()
                val mapState by activeMapSession.state.collectAsStateWithLifecycle()

                LaunchedEffect(mapState) {
                    when (val current = mapState) {
                        WebMapSessionState.Loading -> viewModel.markMapLoading()
                        is WebMapSessionState.Ready -> viewModel.bindController(current.controller)
                        is WebMapSessionState.Failed -> viewModel.markMapFailed(current.message)
                        WebMapSessionState.Closed -> Unit
                    }
                }

                M0FeasibilityScreen(
                    state = state,
                    actions = M0Actions(
                        retryMap = {
                            activeMapSession = mapSessions.retry()
                        },
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
                        key(activeMapSession) {
                            WebMapHost(
                                session = activeMapSession,
                                modifier = Modifier.fillMaxSize(),
                                onUserGesture = viewModel::onUserGesture,
                                onEntityClick = viewModel::selectAircraft,
                                onMetric = viewModel::recordRendererMetric,
                            )
                        }
                    },
                )
            }
        }
    }

    override fun onDestroy() {
        mapSessions.close()
        if (isFinishing) viewModel.close()
        super.onDestroy()
    }
}
