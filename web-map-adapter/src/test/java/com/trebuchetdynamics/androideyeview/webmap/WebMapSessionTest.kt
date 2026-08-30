package com.trebuchetdynamics.androideyeview.webmap

import com.google.common.truth.Truth.assertThat
import com.trebuchetdynamics.androideyeview.core.map.MapCamera
import com.trebuchetdynamics.androideyeview.core.map.MapController
import com.trebuchetdynamics.androideyeview.core.map.MapEntity
import com.trebuchetdynamics.androideyeview.core.map.MapPolyline
import org.junit.Test

class WebMapSessionTest {
    @Test
    fun transitionsLoadingToReadyAndClosesControllerOnce() {
        val session = WebMapSession()
        val controller = RecordingController()

        assertThat(session.attach()).isTrue()
        session.ready(controller)
        session.close()
        session.close()

        assertThat(session.state.value).isEqualTo(WebMapSessionState.Closed)
        assertThat(controller.closeCount).isEqualTo(1)
    }

    @Test
    fun rejectsSecondViewAndLateController() {
        val session = WebMapSession()
        val accepted = RecordingController()
        val late = RecordingController()

        assertThat(session.attach()).isTrue()
        assertThat(session.attach()).isFalse()
        session.ready(accepted)
        session.ready(accepted)
        session.ready(late)

        assertThat(session.state.value).isInstanceOf(WebMapSessionState.Ready::class.java)
        assertThat(accepted.closeCount).isEqualTo(0)
        assertThat(late.closeCount).isEqualTo(1)
    }

    @Test
    fun runtimeFailureReplacesReadyStateAndClosesController() {
        val session = WebMapSession()
        val controller = RecordingController()
        session.ready(controller)

        session.fail("Renderer process lost")

        val failed = session.state.value as WebMapSessionState.Failed
        assertThat(failed.message).isEqualTo("Renderer process lost")
        assertThat(controller.closeCount).isEqualTo(1)
    }

    @Test
    fun failureMessageIsBoundedAndDoesNotReplaceClosedState() {
        val session = WebMapSession()
        session.fail("x".repeat(1_000))

        val failed = session.state.value as WebMapSessionState.Failed
        assertThat(failed.message.length).isAtMost(512)

        session.close()
        session.fail("late")
        assertThat(session.state.value).isEqualTo(WebMapSessionState.Closed)
    }

    private class RecordingController : MapController {
        var closeCount = 0
        override fun renderEntities(entities: List<MapEntity>) = Unit
        override fun removeEntities(entityIds: Set<String>) = Unit
        override fun renderModel(entity: MapEntity, modelUri: String) = Unit
        override fun renderPolyline(polyline: MapPolyline) = Unit
        override fun setCamera(camera: MapCamera) = Unit
        override fun stopCameraMotion() = Unit
        override fun close() { closeCount += 1 }
    }
}
