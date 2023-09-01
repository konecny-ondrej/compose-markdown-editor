package me.okonecny.markdowneditor.flexmark

import androidx.compose.ui.text.TextRange
import com.vladsch.flexmark.util.ast.Node

inline fun <reified T : Node> Node.getAncestorOfType(): T? = getAncestorOfType(T::class.java) as T?

val Node.range: TextRange get() = TextRange(startOffset, endOffset)

fun Node.contains(other: Node): Boolean {
    return startOffset <= other.startOffset && endOffset >= other.endOffset
}