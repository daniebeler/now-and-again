package com.daniebeler.nowandagain.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),    // cards
    large = RoundedCornerShape(8.dp),     // FAB
    extraLarge = RoundedCornerShape(12.dp), // bottom sheet
)