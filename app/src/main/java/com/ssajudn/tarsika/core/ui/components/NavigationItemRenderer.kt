package com.ssajudn.tarsika.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
internal fun RowScope.RenderArchiveNavigationItem(
    item: ArchiveNavigationItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier =
            Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable(
                    role = Role.Tab,
                    onClickLabel = stringResource(item.label),
                    onClick = onClick,
                )
                .semantics { this.selected = selected },
        color = MaterialTheme.colorScheme.surface,
        contentColor =
            if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Icon(item.icon, contentDescription = null, modifier = Modifier.size(21.dp))
            Text(stringResource(item.label), style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
internal fun RenderArchiveRailItem(
    item: ArchiveNavigationItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val container =
        if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        }
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .clickable(
                    role = Role.Tab,
                    onClickLabel = stringResource(item.label),
                    onClick = onClick,
                )
                .semantics { this.selected = selected },
        color = container,
        contentColor =
            if (selected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(item.icon, contentDescription = null, modifier = Modifier.size(22.dp))
            Text(stringResource(item.label), style = MaterialTheme.typography.labelMedium)
        }
    }
}
