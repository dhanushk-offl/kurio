package com.dhanu.kurio.core.design.shape

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object KurioRadii {
    val Button = 14.dp
    val Card = 18.dp
    val Sheet = 24.dp
}

val KurioShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(KurioRadii.Button),
    large = RoundedCornerShape(KurioRadii.Card),
    extraLarge = RoundedCornerShape(KurioRadii.Sheet)
)
