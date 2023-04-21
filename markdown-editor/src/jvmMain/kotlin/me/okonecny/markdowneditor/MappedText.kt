package me.okonecny.markdowneditor

import androidx.compose.ui.text.AnnotatedString
import me.okonecny.interactivetext.ChunkedSourceTextMapping
import me.okonecny.interactivetext.TextMapping
import me.okonecny.interactivetext.ZeroTextMapping

/**
 * Text, which carries information about what part of source text it came from.
 */
internal data class MappedText(
    val text: AnnotatedString,
    val textMapping: TextMapping
) {
    companion object {
        val empty: MappedText = MappedText(AnnotatedString(""), ZeroTextMapping)
    }

    operator fun plus(other: MappedText): MappedText = if (this == empty) {
        other
    } else {
        MappedText(
            text = text + other.text,
            textMapping = ChunkedSourceTextMapping.concat(textMapping, other.textMapping)
        )
    }

    internal class Builder(
        text: MappedText = empty
    ) {
        var mappedText: MappedText = text
            private set

        fun append(text: MappedText) {
            mappedText += text
        }
    }
}

internal fun buildMappedString(buildFn: MappedText.Builder.() -> Unit): MappedText {
    val builder = MappedText.Builder()
    builder.buildFn()
    return builder.mappedText
}
