package me.okonecny.markdowneditor.internal

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font

val FontFamily.Companion.Emoji: FontFamily by lazy {
    FontFamily(
        Font("/NotoColorEmoji-Regular.ttf")
    )
}

val FontFamily.Companion.Symbol: FontFamily by lazy {
    FontFamily(
        Font("/NotoSansMNerdFont-Regular.ttf")
    )
}