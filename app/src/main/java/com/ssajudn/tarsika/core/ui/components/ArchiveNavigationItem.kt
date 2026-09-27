package com.ssajudn.tarsika.core.ui.components

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

data class ArchiveNavigationItem(
    val route: String,
    @param:StringRes val label: Int,
    val icon: ImageVector,
)
