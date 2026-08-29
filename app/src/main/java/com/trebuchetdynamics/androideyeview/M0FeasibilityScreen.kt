package com.trebuchetdynamics.androideyeview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trebuchetdynamics.androideyeview.presentation.SensorMode
import com.trebuchetdynamics.androideyeview.presentation.SensorOverlay

@Composable
fun M0FeasibilityScreen(
    state: M0UiState,
    actions: M0Actions,
    mapContent: @Composable () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        mapContent()
        SensorOverlay(mode = state.sensorMode)

        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .safeDrawingPadding()
                .padding(12.dp)
                .widthIn(max = 380.dp)
                .heightIn(max = 720.dp),
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "ANDROID EYE VIEW / M0",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "Native Maps 3D feasibility console",
                    style = MaterialTheme.typography.bodySmall,
                )
                StatusLine("MAP", state.mapStatus)
                StatusLine("CONTACTS", "${state.renderedContactCount} / ${state.sourceContactCount}")
                StatusLine("LABELS", state.labelCount.toString())
                StatusLine("TICKS", state.tickCount.toString())
                StatusLine("TRAIL", state.trailPointCount.toString())
                state.loadDurationMillis?.let { StatusLine("LOAD", "%.2f ms".format(it)) }
                state.lastTickDurationMillis?.let { StatusLine("LAST TICK", "%.2f ms".format(it)) }
                state.mapError?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
                Text(text = state.message, style = MaterialTheme.typography.bodySmall)

                Button(
                    onClick = actions.loadContacts,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Load 5,000 contacts")
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = actions.tickOnce) { Text("Tick once") }
                    OutlinedButton(
                        onClick = if (state.motionRunning) actions.stopMotion else actions.startMotion,
                    ) {
                        Text(if (state.motionRunning) "Stop motion" else "Start motion")
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = actions.selectFirstAircraft) { Text("Select aircraft") }
                    OutlinedButton(
                        onClick = if (state.followActive) actions.stopFollow else actions.startFollow,
                    ) {
                        Text(if (state.followActive) "Stop follow" else "Follow")
                    }
                }

                Text("Presentation", style = MaterialTheme.typography.labelLarge)
                SensorMode.entries.forEach { mode ->
                    FilterChip(
                        selected = state.sensorMode == mode,
                        onClick = { actions.setSensorMode(mode) },
                        label = { Text(mode.displayName) },
                    )
                }
                Spacer(Modifier.height(56.dp))
            }
        }

        state.sensorMode.disclaimer?.let { disclaimer ->
            Text(
                text = disclaimer,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 72.dp)
                    .background(Color(0xDD050A0D), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun StatusLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.secondary,
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.labelSmall,
        )
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

data class M0Actions(
    val loadContacts: () -> Unit,
    val tickOnce: () -> Unit,
    val startMotion: () -> Unit,
    val stopMotion: () -> Unit,
    val selectFirstAircraft: () -> Unit,
    val startFollow: () -> Unit,
    val stopFollow: () -> Unit,
    val setSensorMode: (SensorMode) -> Unit,
)
