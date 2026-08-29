package com.trebuchetdynamics.androideyeview.core.map

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CameraOwnershipTest {
    @Test
    fun directNavigationOutranksFollowAndFollowOutranksAmbient() {
        val ownership = CameraOwnership()

        assertThat(ownership.acquire(CameraOwner.AMBIENT)).isTrue()
        assertThat(ownership.currentOwner).isEqualTo(CameraOwner.AMBIENT)
        assertThat(ownership.acquire(CameraOwner.FOLLOW)).isTrue()
        assertThat(ownership.currentOwner).isEqualTo(CameraOwner.FOLLOW)
        assertThat(ownership.acquire(CameraOwner.AMBIENT)).isFalse()
        assertThat(ownership.currentOwner).isEqualTo(CameraOwner.FOLLOW)
        assertThat(ownership.acquire(CameraOwner.DIRECT_NAVIGATION)).isTrue()
        assertThat(ownership.currentOwner).isEqualTo(CameraOwner.DIRECT_NAVIGATION)
        assertThat(ownership.acquire(CameraOwner.FOLLOW)).isFalse()
        assertThat(ownership.currentOwner).isEqualTo(CameraOwner.DIRECT_NAVIGATION)
    }

    @Test
    fun currentOwnerCanReacquireAndOnlyCurrentOwnerCanRelease() {
        val ownership = CameraOwnership()

        assertThat(ownership.acquire(CameraOwner.FOLLOW)).isTrue()
        assertThat(ownership.acquire(CameraOwner.FOLLOW)).isTrue()
        assertThat(ownership.release(CameraOwner.AMBIENT)).isFalse()
        assertThat(ownership.currentOwner).isEqualTo(CameraOwner.FOLLOW)
        assertThat(ownership.release(CameraOwner.FOLLOW)).isTrue()
        assertThat(ownership.currentOwner).isNull()
    }

    @Test
    fun userGestureSynchronouslyReleasesAnyProgrammaticOwner() {
        CameraOwner.entries.forEach { owner ->
            val ownership = CameraOwnership()
            ownership.acquire(owner)

            assertThat(ownership.onUserGesture()).isEqualTo(owner)
            assertThat(ownership.currentOwner).isNull()
        }
    }

    @Test
    fun userGestureWithoutAnOwnerIsAnIdempotentNoOp() {
        val ownership = CameraOwnership()

        assertThat(ownership.onUserGesture()).isNull()
        assertThat(ownership.onUserGesture()).isNull()
    }
}
