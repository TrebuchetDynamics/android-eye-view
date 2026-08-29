package com.trebuchetdynamics.androideyeview.maps3d

import com.google.common.truth.Truth.assertThat
import com.trebuchetdynamics.androideyeview.core.map.MapCamera
import com.trebuchetdynamics.androideyeview.core.map.MapController
import com.trebuchetdynamics.androideyeview.core.map.MapEntity
import com.trebuchetdynamics.androideyeview.core.map.MapPolyline
import org.junit.Test

class Maps3DSessionTest {
    @Test
    fun transitionsFromLoadingToReadyToClosed() {
        val controller = RecordingController()
        val session = Maps3DSession()

        session.ready(controller)
        assertThat(session.state.value).isInstanceOf(MapSessionState.Ready::class.java)

        session.close()
        assertThat(session.state.value).isEqualTo(MapSessionState.Closed)
        assertThat(controller.closeCount).isEqualTo(1)
    }

    @Test
    fun transitionsFromLoadingToFailedToClosed() {
        val session = Maps3DSession()

        session.fail(IllegalStateException("secret detail"))
        assertThat(session.state.value).isEqualTo(MapSessionState.Failed("Map initialization failed"))

        session.close()
        assertThat(session.state.value).isEqualTo(MapSessionState.Closed)
    }

    @Test
    fun ignoresCallbacksAndDoubleCloseAfterClosing() {
        val controller = RecordingController()
        val lateController = RecordingController()
        val session = Maps3DSession()

        session.ready(controller)
        session.close()
        session.close()
        session.ready(lateController)
        session.fail(RuntimeException("late"))

        assertThat(controller.closeCount).isEqualTo(1)
        assertThat(lateController.closeCount).isEqualTo(1)
        assertThat(session.state.value).isEqualTo(MapSessionState.Closed)
    }

    private class RecordingController : MapController {
        var closeCount = 0

        override fun renderEntities(entities: List<MapEntity>) = Unit
        override fun removeEntities(entityIds: Set<String>) = Unit
        override fun renderModel(entity: MapEntity, modelUri: String) = Unit
        override fun renderPolyline(polyline: MapPolyline) = Unit
        override fun setCamera(camera: MapCamera) = Unit
        override fun stopCameraMotion() = Unit
        override fun close() {
            closeCount += 1
        }
    }
}
