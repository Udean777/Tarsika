package com.ssajudn.tarsika.feature.gallery.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test

class GalleryMessageTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyMessageDisplaysTitleAndDescription() {
        composeRule.setContent {
            MaterialTheme {
                GalleryMessage(
                    title = "No photos",
                    description = "Choose a different folder.",
                )
            }
        }

        composeRule.onNodeWithText("No photos").assertIsDisplayed()
        composeRule.onNodeWithText("Choose a different folder.").assertIsDisplayed()
    }
}
