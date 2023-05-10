package me.okonecny.whodoes.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.windowShadow(size: Dp, color: Color = Color.DarkGray) = padding(size + 1.dp)
    .shadow(color = color, blurRadius = size)
    .border(Dp.Hairline, color.copy(0.5f), RoundedCornerShape(size / 2.0f))