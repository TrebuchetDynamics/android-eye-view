package com.trebuchetdynamics.androideyeview.core.map

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RenderBudgetTest {
    @Test
    fun everyBudgetAcceptsTheFiveThousandContactDensityGate() {
        val source = SyntheticContactFactory.create()

        RenderBudget.entries.forEach { budget ->
            val allocation = budget.allocate(source)

            assertThat(allocation.entities).hasSize(5_000)
            assertThat(allocation.labelEntityIds).hasSize(budget.maxLabels)
            assertThat(allocation.modelEntityIds).hasSize(budget.maxModels)
            assertThat(allocation.labelEntityIds).containsNoneIn(
                allocation.entities.drop(budget.maxLabels).map { it.id },
            )
            assertThat(allocation.modelEntityIds).containsNoneIn(
                allocation.entities.drop(budget.maxModels).map { it.id },
            )
        }
    }

    @Test
    fun qualityLevelsReduceLabelsAndModelsIndependently() {
        assertThat(RenderBudget.QUALITY.maxLabels).isGreaterThan(RenderBudget.BALANCED.maxLabels)
        assertThat(RenderBudget.BALANCED.maxLabels)
            .isGreaterThan(RenderBudget.BATTERY_SAVER.maxLabels)
        assertThat(RenderBudget.QUALITY.maxModels).isGreaterThan(RenderBudget.BALANCED.maxModels)
        assertThat(RenderBudget.BALANCED.maxModels)
            .isGreaterThan(RenderBudget.BATTERY_SAVER.maxModels)
        assertThat(RenderBudget.entries.map { it.maxEntities }.toSet()).containsExactly(5_000)
    }

    @Test
    fun allocationDoesNotMutateSourceTruthAndSkipsMissingLabels() {
        val source = SyntheticContactFactory.create(count = 100)
            .mapIndexed { index, entity -> if (index % 2 == 0) entity.copy(label = null) else entity }
        val original = source.toList()

        val allocation = RenderBudget.QUALITY.allocate(source)

        assertThat(source).isEqualTo(original)
        assertThat(allocation.entities).isEqualTo(source)
        assertThat(allocation.labelEntityIds)
            .containsExactlyElementsIn(source.filter { it.label != null }.map { it.id })
        assertThat(allocation.modelEntityIds).hasSize(RenderBudget.QUALITY.maxModels)
    }
}
