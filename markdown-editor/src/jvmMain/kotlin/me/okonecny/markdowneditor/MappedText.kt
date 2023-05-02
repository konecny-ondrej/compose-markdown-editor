package me.okonecny.markdowneditor

import androidx.compose.ui.text.AnnotatedString
import me.okonecny.interactivetext.TextMapping
import me.okonecny.interactivetext.ZeroTextMapping
import me.okonecny.interactivetext.plus

/**
 * Text, which carries information about what part of source text it came from.
 */
data class MappedText(
    val text: AnnotatedString,
    val textMapping: TextMapping
) {
    constructor(text: String, textMapping: TextMapping) : this(AnnotatedString(text), textMapping)

    companion object {
        val empty: MappedText = MappedText("", ZeroTextMapping)
    }

    operator fun plus(other: MappedText): MappedText = if (this === empty) {
        other
    } else {
        MappedText(
            text = text + other.text,
            textMapping = textMapping + other.textMapping
        )
    }

    internal class Builder(
        text: MappedText = empty
    ) {
        var mappedText: MappedText = text
            private set

        val visualLength get() = mappedText.text.length

        fun append(text: MappedText) {
            mappedText += text
        }

        fun append(text: String) { // TODO: do we want to append text without mapping?
            mappedText += MappedText(text, ZeroTextMapping)
        }
    }
}

internal inline fun buildMappedString(buildFn: MappedText.Builder.() -> Unit): MappedText =
    MappedText.Builder().apply(buildFn).mappedText
