package com.ssajudn.tarsika.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val TarsikaShapes =
    Shapes(
        extraSmall = RoundedCornerShape(4.dp),
        small = RoundedCornerShape(12.dp),
        medium = RoundedCornerShape(12.dp),
        large = RoundedCornerShape(16.dp),
        extraLarge = RoundedCornerShape(24.dp),
    )

val TarsikaSheetShape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp)
val TarsikaDockShape = RoundedCornerShape(0.dp)
