package com.trebuchetdynamics.androideyeview.core.map

enum class RenderBudget(
    val maxEntities: Int,
    val maxLabels: Int,
    val maxModels: Int,
) {
    QUALITY(maxEntities = 5_000, maxLabels = 1_200, maxModels = 64),
    BALANCED(maxEntities = 5_000, maxLabels = 600, maxModels = 24),
    BATTERY_SAVER(maxEntities = 5_000, maxLabels = 250, maxModels = 8),
    ;

    fun allocate(sourceEntities: List<MapEntity>): RenderAllocation {
        val renderedEntities = sourceEntities.take(maxEntities).toList()
        return RenderAllocation(
            entities = renderedEntities,
            labelEntityIds = renderedEntities.asSequence()
                .filter { it.label != null }
                .take(maxLabels)
                .mapTo(linkedSetOf()) { it.id },
            modelEntityIds = renderedEntities.asSequence()
                .filter { it.kind == EntityKind.AIRCRAFT }
                .take(maxModels)
                .mapTo(linkedSetOf()) { it.id },
        )
    }
}

data class RenderAllocation(
    val entities: List<MapEntity>,
    val labelEntityIds: Set<String>,
    val modelEntityIds: Set<String>,
)
