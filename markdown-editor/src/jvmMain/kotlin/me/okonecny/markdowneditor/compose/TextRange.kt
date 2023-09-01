package me.okonecny.markdowneditor.compose

import androidx.compose.ui.text.TextRange

val TextRange.onlyIncludedIndexes: IntRange get() = if (collapsed) IntRange.EMPTY else IntRange(min, max - 1)