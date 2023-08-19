package me.okonecny.markdowneditor.flexmark

import androidx.compose.ui.text.TextRange
import com.vladsch.flexmark.util.sequence.BasedSequence

val BasedSequence.range: TextRange get() = TextRange(startOffset, endOffset)