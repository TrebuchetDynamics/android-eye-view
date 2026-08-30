package com.trebuchetdynamics.androideyeview

import com.google.common.truth.Truth.assertThat
import com.trebuchetdynamics.androideyeview.core.map.MapCamera
import com.trebuchetdynamics.androideyeview.core.map.MapController
import com.trebuchetdynamics.androideyeview.core.map.MapEntity
import com.trebuchetdynamics.androideyeview.core.map.MapPolyline
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class M0FeasibilityViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadsExactlyFiveThousandContactsWithBalancedLabels() = runTest(dispatcher.scheduler) {
        val controller = RecordingController()
        val viewModel = M0FeasibilityViewModel(dispatcher = dispatcher, nanoTime = tickingClock())
        viewModel.bindController(controller)

        viewModel.loadContacts()
        advanceUntilIdle()

        assertThat(controller.lastEntities).hasSize(5_000)
        assertThat(controller.lastEntities.count { it.label != null }).isEqualTo(600)
        assertThat(viewModel.state.value.sourceContactCount).isEqualTo(5_000)
        assertThat(viewModel.state.value.renderedContactCount).isEqualTo(5_000)
    }

    @Test
    fun movementTicksAreCancellable() = runTest(dispatcher.scheduler) {
        val controller = RecordingController()
        val viewModel = M0FeasibilityViewModel(dispatcher = dispatcher, nanoTime = tickingClock())
        viewModel.bindController(controller)
        viewModel.loadContacts()
        advanceUntilIdle()
        val baselineRenders = controller.renderEntitiesCount

        viewModel.startMotion()
        advanceTimeBy(2_100)
        viewModel.stopMotion()
        advanceUntilIdle()
        val rendersAfterStop = controller.renderEntitiesCount
        advanceTimeBy(2_100)

        assertThat(rendersAfterStop).isAtLeast(baselineRenders + 2)
        assertThat(controller.renderEntitiesCount).isEqualTo(rendersAfterStop)
        assertThat(viewModel.state.value.motionRunning).isFalse()
    }

    @Test
    fun selectionRendersModelTrailAndFollowWithoutRebuildingOnGesture() = runTest(dispatcher.scheduler) {
        val controller = RecordingController()
        val viewModel = M0FeasibilityViewModel(dispatcher = dispatcher, nanoTime = tickingClock())
        viewModel.bindController(controller)
        viewModel.loadContacts()
        advanceUntilIdle()
        val selectedId = controller.lastEntities.first().id

        viewModel.selectAircraft(selectedId)
        viewModel.startFollow()
        viewModel.tickOnce()
        advanceUntilIdle()
        viewModel.onUserGesture()

        assertThat(controller.models).isNotEmpty()
        assertThat(controller.polylines).isNotEmpty()
        assertThat(controller.cameras).isNotEmpty()
        assertThat(controller.stopCameraCount).isEqualTo(1)
        assertThat(viewModel.state.value.followActive).isFalse()
    }

    @Test
    fun replacingControllerReplaysContactsSelectionTrailAndFollow() = runTest(dispatcher.scheduler) {
        val first = RecordingController()
        val replacement = RecordingController()
        val viewModel = M0FeasibilityViewModel(dispatcher = dispatcher, nanoTime = tickingClock())
        viewModel.bindController(first)
        viewModel.loadContacts()
        advanceUntilIdle()
        val initialPosition = first.lastEntities.first().position
        viewModel.selectAircraft(first.lastEntities.first().id)
        viewModel.startFollow()
        viewModel.tickOnce()
        advanceUntilIdle()
        val tickedEntity = first.lastEntities.first()

        viewModel.bindController(replacement)

        assertThat(first.closeCount).isEqualTo(1)
        assertThat(tickedEntity.position).isNotEqualTo(initialPosition)
        assertThat(replacement.lastEntities).hasSize(5_000)
        assertThat(replacement.lastEntities.first()).isEqualTo(tickedEntity)
        assertThat(replacement.models).hasSize(1)
        assertThat(replacement.polylines).hasSize(1)
        assertThat(replacement.cameras).hasSize(1)
    }

    @Test
    fun recordsOnlyKnownFiniteRendererMetrics() = runTest(dispatcher.scheduler) {
        val viewModel = M0FeasibilityViewModel(dispatcher = dispatcher, nanoTime = tickingClock())

        viewModel.recordRendererMetric("contacts-render-ms", 42.5)
        viewModel.recordRendererMetric("raf-p95-ms", 18.2)
        viewModel.recordRendererMetric("unknown", 99.0)
        viewModel.recordRendererMetric("contacts-render-ms", Double.NaN)

        assertThat(viewModel.state.value.rendererContactsMillis).isEqualTo(42.5)
        assertThat(viewModel.state.value.rendererRafP95Millis).isEqualTo(18.2)
    }

    @Test
    fun closingViewModelStopsMotionAndClosesControllerOnce() = runTest(dispatcher.scheduler) {
        val controller = RecordingController()
        val viewModel = M0FeasibilityViewModel(dispatcher = dispatcher, nanoTime = tickingClock())
        viewModel.bindController(controller)
        viewModel.startMotion()

        viewModel.close()
        viewModel.close()
        advanceUntilIdle()

        assertThat(controller.closeCount).isEqualTo(1)
        assertThat(viewModel.state.value.motionRunning).isFalse()
    }

    private fun tickingClock(): () -> Long {
        var value = 0L
        return { value.also { value += 1_000_000L } }
    }

    private class RecordingController : MapController {
        var lastEntities = emptyList<MapEntity>()
        var renderEntitiesCount = 0
        val models = mutableListOf<MapEntity>()
        val polylines = mutableListOf<MapPolyline>()
        val cameras = mutableListOf<MapCamera>()
        var stopCameraCount = 0
        var closeCount = 0

        override fun renderEntities(entities: List<MapEntity>) {
            lastEntities = entities
            renderEntitiesCount += 1
        }

        override fun removeEntities(entityIds: Set<String>) = Unit

        override fun renderModel(entity: MapEntity, modelUri: String) {
            models += entity
        }

        override fun renderPolyline(polyline: MapPolyline) {
            polylines += polyline
        }

        override fun setCamera(camera: MapCamera) {
            cameras += camera
        }

        override fun stopCameraMotion() {
            stopCameraCount += 1
        }

        override fun close() {
            closeCount += 1
        }
    }
}
