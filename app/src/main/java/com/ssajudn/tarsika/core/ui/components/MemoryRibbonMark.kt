package com.ssajudn.tarsika.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun MemoryRibbonMark(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val leadWidth = if (compact) 26.dp else 40.dp
    val dotSize = if (compact) 6.dp else 8.dp
    val tailWidth = if (compact) 12.dp else 18.dp
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .width(leadWidth)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
        )
        Box(
            modifier =
                Modifier
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary),
        )
        Box(
            modifier =
                Modifier
                    .width(tailWidth)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
        )
    }
}
