package com.trebuchetdynamics.androideyeview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trebuchetdynamics.androideyeview.core.map.CameraOwner
import com.trebuchetdynamics.androideyeview.core.map.CameraOwnership
import com.trebuchetdynamics.androideyeview.core.map.FollowCamera
import com.trebuchetdynamics.androideyeview.core.map.MapController
import com.trebuchetdynamics.androideyeview.core.map.MapEntity
import com.trebuchetdynamics.androideyeview.core.map.MapPolyline
import com.trebuchetdynamics.androideyeview.core.map.RenderBudget
import com.trebuchetdynamics.androideyeview.core.map.SyntheticContactAnimator
import com.trebuchetdynamics.androideyeview.core.map.SyntheticContactFactory
import com.trebuchetdynamics.androideyeview.core.map.TrailBuffer
import com.trebuchetdynamics.androideyeview.presentation.SensorMode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class M0FeasibilityViewModel(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val nanoTime: () -> Long = System::nanoTime,
) : ViewModel(), AutoCloseable {
    private val mutableState = MutableStateFlow(M0UiState())
    val state: StateFlow<M0UiState> = mutableState.asStateFlow()

    private val cameraOwnership = CameraOwnership()
    private val trail = TrailBuffer(maxPoints = 120)
    private var controller: MapController? = null
    private var contacts = emptyList<MapEntity>()
    private var renderedContacts = emptyList<MapEntity>()
    private var motionJob: Job? = null
    private var closed = false

    fun bindController(newController: MapController) {
        if (closed) {
            newController.close()
            return
        }
        if (controller === newController) return
        controller?.close()
        controller = newController
        replayScene(newController)
        mutableState.value = mutableState.value.copy(
            mapStatus = "READY",
            mapError = null,
            message = "Free globe ready",
        )
    }

    fun markMapLoading() {
        mutableState.value = mutableState.value.copy(mapStatus = "LOADING", mapError = null)
    }

    fun markMapFailed(message: String) {
        mutableState.value = mutableState.value.copy(mapStatus = "UNAVAILABLE", mapError = message)
    }

    fun loadContacts() {
        if (closed) return
        viewModelScope.launch {
            val start = nanoTime()
            val generated = withContext(dispatcher) {
                SyntheticContactFactory.create()
            }
            contacts = generated
            val allocation = RenderBudget.BALANCED.allocate(generated)
            val labeled = allocation.entities.map { entity ->
                if (entity.id in allocation.labelEntityIds) entity else entity.copy(label = null)
            }
            renderedContacts = labeled
            controller?.renderEntities(labeled)
            mutableState.value = mutableState.value.copy(
                sourceContactCount = generated.size,
                renderedContactCount = labeled.size,
                labelCandidateCount = allocation.labelEntityIds.size,
                loadDurationMillis = elapsedMillis(start),
                message = "Synthetic contacts loaded",
            )
        }
    }

    fun startMotion() {
        if (closed || motionJob?.isActive == true || contacts.isEmpty()) return
        mutableState.value = mutableState.value.copy(motionRunning = true)
        motionJob = viewModelScope.launch {
            while (isActive) {
                delay(MOTION_INTERVAL_MILLIS)
                tickAndRender()
            }
        }
    }

    fun stopMotion() {
        motionJob?.cancel()
        motionJob = null
        mutableState.value = mutableState.value.copy(motionRunning = false)
    }

    fun tickOnce() {
        if (closed || contacts.isEmpty()) return
        viewModelScope.launch { tickAndRender() }
    }

    fun selectAircraft(entityId: String) {
        val selected = contacts.firstOrNull { it.id == entityId } ?: return
        trail.clear()
        trail.append(selected.position)
        controller?.renderModel(selected, AIRCRAFT_MODEL_URI)
        mutableState.value = mutableState.value.copy(
            selectedId = selected.id,
            trailPointCount = 1,
            message = "Selected ${selected.label ?: selected.id}",
        )
    }

    fun startFollow(reducedMotion: Boolean = false) {
        val selected = selectedEntity() ?: return
        if (!cameraOwnership.acquire(CameraOwner.FOLLOW)) return
        controller?.setCamera(FollowCamera.forEntity(selected, reducedMotion))
        mutableState.value = mutableState.value.copy(followActive = true)
    }

    fun stopFollow() {
        cameraOwnership.release(CameraOwner.FOLLOW)
        controller?.stopCameraMotion()
        mutableState.value = mutableState.value.copy(followActive = false)
    }

    fun onUserGesture() {
        if (cameraOwnership.onUserGesture() != null) {
            controller?.stopCameraMotion()
        }
        mutableState.value = mutableState.value.copy(followActive = false)
    }

    fun setSensorMode(mode: SensorMode) {
        mutableState.value = mutableState.value.copy(sensorMode = mode)
    }

    fun recordRendererMetric(name: String, value: Double) {
        if (!value.isFinite() || value < 0.0) return
        mutableState.value = when (name) {
            "contacts-render-ms" -> mutableState.value.copy(rendererContactsMillis = value)
            "raf-p95-ms" -> mutableState.value.copy(rendererRafP95Millis = value)
            else -> mutableState.value
        }
    }

    private suspend fun tickAndRender() {
        val start = nanoTime()
        val updated = withContext(dispatcher) {
            SyntheticContactAnimator.tick(contacts, elapsedSeconds = 1.0)
        }
        contacts = updated
        val allocation = RenderBudget.BALANCED.allocate(updated)
        val labeled = allocation.entities.map { entity ->
            if (entity.id in allocation.labelEntityIds) entity else entity.copy(label = null)
        }
        renderedContacts = labeled
        controller?.renderEntities(labeled)

        selectedEntity()?.let { selected ->
            trail.append(selected.position)
            controller?.renderModel(selected, AIRCRAFT_MODEL_URI)
            val points = trail.snapshot()
            if (points.size >= 2) {
                controller?.renderPolyline(MapPolyline(TRAIL_ID, points))
            }
            if (cameraOwnership.currentOwner == CameraOwner.FOLLOW) {
                controller?.setCamera(FollowCamera.forEntity(selected, reducedMotion = false))
            }
            mutableState.value = mutableState.value.copy(trailPointCount = points.size)
        }

        mutableState.value = mutableState.value.copy(
            renderedContactCount = labeled.size,
            labelCandidateCount = allocation.labelEntityIds.size,
            tickCount = mutableState.value.tickCount + 1,
            lastTickDurationMillis = elapsedMillis(start),
        )
    }

    private fun replayScene(target: MapController) {
        if (renderedContacts.isNotEmpty()) target.renderEntities(renderedContacts)
        val selected = selectedEntity() ?: return
        target.renderModel(selected, AIRCRAFT_MODEL_URI)
        val trailPoints = trail.snapshot()
        if (trailPoints.size >= 2) {
            target.renderPolyline(MapPolyline(TRAIL_ID, trailPoints))
        }
        if (cameraOwnership.currentOwner == CameraOwner.FOLLOW) {
            target.setCamera(FollowCamera.forEntity(selected, reducedMotion = false))
        }
    }

    private fun selectedEntity(): MapEntity? {
        val selectedId = mutableState.value.selectedId ?: return null
        return contacts.firstOrNull { it.id == selectedId }
    }

    private fun elapsedMillis(startNanos: Long): Double =
        (nanoTime() - startNanos).coerceAtLeast(0L) / 1_000_000.0

    override fun close() {
        if (closed) return
        closed = true
        stopMotion()
        controller?.close()
        controller = null
    }

    override fun onCleared() {
        close()
        super.onCleared()
    }

    companion object {
        const val AIRCRAFT_MODEL_URI = "marker://aircraft-fallback"
        private const val TRAIL_ID = "selected-aircraft-trail"
        private const val MOTION_INTERVAL_MILLIS = 1_000L
    }
}

data class M0UiState(
    val mapStatus: String = "LOADING",
    val mapError: String? = null,
    val sourceContactCount: Int = 0,
    val renderedContactCount: Int = 0,
    val labelCandidateCount: Int = 0,
    val selectedId: String? = null,
    val trailPointCount: Int = 0,
    val motionRunning: Boolean = false,
    val followActive: Boolean = false,
    val sensorMode: SensorMode = SensorMode.NORMAL,
    val loadDurationMillis: Double? = null,
    val lastTickDurationMillis: Double? = null,
    val rendererContactsMillis: Double? = null,
    val rendererRafP95Millis: Double? = null,
    val tickCount: Int = 0,
    val message: String = "Waiting for free globe",
)
