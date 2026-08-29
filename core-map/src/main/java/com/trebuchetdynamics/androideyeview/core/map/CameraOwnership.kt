package com.trebuchetdynamics.androideyeview.core.map

enum class CameraOwner(internal val priority: Int) {
    AMBIENT(priority = 1),
    FOLLOW(priority = 2),
    DIRECT_NAVIGATION(priority = 3),
}

class CameraOwnership {
    private var owner: CameraOwner? = null

    val currentOwner: CameraOwner?
        @Synchronized get() = owner

    @Synchronized
    fun acquire(requestedOwner: CameraOwner): Boolean {
        val current = owner
        if (current != null && requestedOwner.priority < current.priority) return false

        owner = requestedOwner
        return true
    }

    @Synchronized
    fun release(releasingOwner: CameraOwner): Boolean {
        if (owner != releasingOwner) return false

        owner = null
        return true
    }

    @Synchronized
    fun onUserGesture(): CameraOwner? {
        val releasedOwner = owner
        owner = null
        return releasedOwner
    }
}
