package me.okonecny.markdowneditor.flexmark

import androidx.compose.ui.text.TextRange
import com.vladsch.flexmark.util.ast.Node

val Node.range: TextRange get() = TextRange(startOffset, endOffset)

fun Node.contains(other: Node): Boolean {
    return startOffset <= other.startOffset && endOffset >= other.endOffset
}

val Node.source: String get() = chars.toString()
