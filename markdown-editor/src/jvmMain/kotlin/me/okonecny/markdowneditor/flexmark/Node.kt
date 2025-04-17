package me.okonecny.markdowneditor.flexmark

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.ast.TextCollectingVisitor
import me.okonecny.markdowneditor.TextWithInlines

val Node.range: TextRange get() = TextRange(startOffset, endOffset)

fun Node.contains(other: Node): Boolean {
    return startOffset <= other.startOffset && endOffset >= other.endOffset
}

val Node.source: String get() = chars.toString()

/**
 * Returns the unprocessed Markdown source code corresponding to the node.
 */
internal fun Node.rawCode(): TextWithInlines {
    return TextWithInlines(
        text = chars.toString()
    )
}

/**
 * Collects the node text, resolving all escapes.
 */
internal fun Node.text(): String {
    val builder = TextCollectingVisitor()
    builder.collect(this)
    return builder.text
}