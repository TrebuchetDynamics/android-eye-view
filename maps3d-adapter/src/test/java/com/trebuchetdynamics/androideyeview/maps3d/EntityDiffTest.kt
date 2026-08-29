package com.trebuchetdynamics.androideyeview.maps3d

import com.google.common.truth.Truth.assertThat
import com.trebuchetdynamics.androideyeview.core.map.EntityKind
import com.trebuchetdynamics.androideyeview.core.map.GeoPoint
import com.trebuchetdynamics.androideyeview.core.map.MapEntity
import org.junit.Test

class EntityDiffTest {
    @Test
    fun separatesAddedUpdatedRemovedAndUnchangedEntities() {
        val unchanged = entity("unchanged", 1.0)
        val beforeUpdate = entity("updated", 2.0)
        val afterUpdate = entity("updated", 2.5)
        val removed = entity("removed", 3.0)
        val added = entity("added", 4.0)

        val diff = EntityDiff.calculate(
            previous = listOf(unchanged, beforeUpdate, removed),
            next = listOf(unchanged, afterUpdate, added),
        )

        assertThat(diff.added).containsExactly(added)
        assertThat(diff.updated).containsExactly(afterUpdate)
        assertThat(diff.removedIds).containsExactly("removed")
        assertThat(diff.unchangedIds).containsExactly("unchanged")
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsDuplicateIds() {
        EntityDiff.calculate(emptyList(), listOf(entity("same", 1.0), entity("same", 2.0)))
    }

    private fun entity(id: String, longitude: Double) = MapEntity(
        id = id,
        position = GeoPoint(37.0, longitude, 1_000.0),
        headingDegrees = 90.0,
        label = id,
        kind = EntityKind.AIRCRAFT,
    )
}
