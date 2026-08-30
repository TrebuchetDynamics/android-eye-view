package com.trebuchetdynamics.androideyeview

import com.google.common.truth.Truth.assertThat
import com.trebuchetdynamics.androideyeview.webmap.WebMapSessionState
import org.junit.Test

class WebMapSessionCoordinatorTest {
    @Test
    fun offlineStartupFailureCanRetryWithFreshLoadingSession() {
        val coordinator = WebMapSessionCoordinator()
        val failedSession = coordinator.current
        failedSession.fail("Free map data failed to load")

        val retrySession = coordinator.retry()

        assertThat(failedSession.state.value).isEqualTo(WebMapSessionState.Closed)
        assertThat(retrySession).isNotSameInstanceAs(failedSession)
        assertThat(retrySession.state.value).isEqualTo(WebMapSessionState.Loading)
        assertThat(coordinator.current).isSameInstanceAs(retrySession)
    }
}
