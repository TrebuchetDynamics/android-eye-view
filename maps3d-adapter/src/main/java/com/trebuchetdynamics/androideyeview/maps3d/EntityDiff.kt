package com.trebuchetdynamics.androideyeview.maps3d

import com.trebuchetdynamics.androideyeview.core.map.MapEntity

data class EntityDiff(
    val added: List<MapEntity>,
    val updated: List<MapEntity>,
    val removedIds: Set<String>,
    val unchangedIds: Set<String>,
) {
    companion object {
        fun calculate(previous: List<MapEntity>, next: List<MapEntity>): EntityDiff {
            val previousById = previous.requireUniqueIds("previous")
            val nextById = next.requireUniqueIds("next")
            val added = next.filter { it.id !in previousById }
            val updated = next.filter { entity ->
                val old = previousById[entity.id]
                old != null && old != entity
            }
            val unchanged = next.asSequence()
                .filter { previousById[it.id] == it }
                .mapTo(linkedSetOf()) { it.id }
            val removed = previousById.keys - nextById.keys
            return EntityDiff(added, updated, removed, unchanged)
        }
    }
}

private fun List<MapEntity>.requireUniqueIds(source: String): Map<String, MapEntity> {
    val result = associateBy(MapEntity::id)
    require(result.size == size) { "$source entities contain duplicate IDs" }
    return result
}
