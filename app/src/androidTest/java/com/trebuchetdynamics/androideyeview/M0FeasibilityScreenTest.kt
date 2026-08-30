package com.trebuchetdynamics.androideyeview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.trebuchetdynamics.androideyeview.presentation.SensorMode
import com.trebuchetdynamics.androideyeview.ui.theme.AndroidEyeViewTheme
import java.util.concurrent.atomic.AtomicBoolean
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class M0FeasibilityScreenTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun readyStateShowsCoreFeasibilityControls() {
        composeRule.setContent {
            AndroidEyeViewTheme {
                M0FeasibilityScreen(
                    state = M0UiState(
                        mapStatus = "READY",
                        sourceContactCount = 5_000,
                        renderedContactCount = 5_000,
                    ),
                    actions = noOpActions(),
                    mapContent = {
                        Box(Modifier.fillMaxSize().testTag("fake_map"))
                    },
                )
            }
        }

        composeRule.onNodeWithTag("fake_map").assertIsDisplayed()
        composeRule.onNodeWithText("FREE / KEYLESS").assertIsDisplayed()
        composeRule.onNodeWithText("Load 5,000 contacts").assertIsDisplayed()
        composeRule.onNodeWithText("5000 / 5000").assertIsDisplayed()
        composeRule.onNodeWithText("Aircraft marker fallback — not a glTF model").assertIsDisplayed()
    }

    @Test
    fun simulatedPresentationShowsHonestyLabel() {
        composeRule.setContent {
            AndroidEyeViewTheme {
                M0FeasibilityScreen(
                    state = M0UiState(sensorMode = SensorMode.THERMAL_INSPIRED),
                    actions = noOpActions(),
                    mapContent = {},
                )
            }
        }

        composeRule.onNodeWithText("Visual simulation — not sensor imagery").assertIsDisplayed()
    }

    @Test
    fun mapFailureRemainsReadable() {
        val retried = AtomicBoolean(false)
        composeRule.setContent {
            AndroidEyeViewTheme {
                M0FeasibilityScreen(
                    state = M0UiState(
                        mapStatus = "UNAVAILABLE",
                        mapError = "Map initialization failed",
                    ),
                    actions = noOpActions().copy(retryMap = { retried.set(true) }),
                    mapContent = {},
                )
            }
        }

        composeRule.onNodeWithText("UNAVAILABLE").assertIsDisplayed()
        composeRule.onNodeWithText("Map initialization failed").assertIsDisplayed()
        composeRule.onNodeWithText("Retry globe").assertIsDisplayed().performClick()
        composeRule.runOnIdle { assertTrue(retried.get()) }
    }

    private fun noOpActions() = M0Actions(
        retryMap = {},
        loadContacts = {},
        tickOnce = {},
        startMotion = {},
        stopMotion = {},
        selectFirstAircraft = {},
        startFollow = {},
        stopFollow = {},
        setSensorMode = {},
    )
}
