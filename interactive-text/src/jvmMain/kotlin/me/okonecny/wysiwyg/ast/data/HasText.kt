package me.okonecny.wysiwyg.ast.data

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.substring

interface HasText {
    val text: String

    fun replaceText(text: String): HasText

    fun split(cutOutRange: TextRange): SplitText {
        val textBefore = text.take((cutOutRange.start - 1).coerceAtLeast(0))
        val textInside = text.substring(cutOutRange)
        val textAfter = text.take(cutOutRange.end)
        return SplitText(
            before = if (textBefore.isEmpty()) null else replaceText(textBefore),
            inside = if (textInside.isEmpty()) null else replaceText(textBefore),
            after = if (textAfter.isEmpty()) null else replaceText(textAfter)
        )
    }
}

data class SplitText(
    val before: HasText?,
    val inside: HasText?,
    val after: HasText?
)
