package me.okonecny.markdowneditor.compose

import androidx.compose.ui.text.TextRange

val IntRange.textRange: TextRange get() = if (this == IntRange.EMPTY) TextRange.Zero else TextRange(first, last + 1)