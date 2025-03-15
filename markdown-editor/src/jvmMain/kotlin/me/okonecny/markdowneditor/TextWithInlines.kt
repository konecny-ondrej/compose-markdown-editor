package me.okonecny.markdowneditor

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString

/**
 * Text, which carries information about what part of source text it came from.
 */
data class TextWithInlines(
    val text: AnnotatedString,
    val inlineContent: Map<String, InlineTextContent> = emptyMap()
) {
    constructor(text: String) : this(AnnotatedString(text))

    companion object {
        val empty: TextWithInlines = TextWithInlines("")
    }

    fun annotatedWith(tag: String, annotation: String): TextWithInlines = TextWithInlines(
        text = buildAnnotatedString {
            pushStringAnnotation(tag, annotation)
            append(text)
            pop()
        },
        inlineContent = inlineContent
    )

    operator fun plus(other: TextWithInlines): TextWithInlines = when {
        this === empty -> other
        other === empty -> this
        else -> {
            val conflictingInlines = inlineContent.keys.intersect(other.inlineContent.keys)
            if (conflictingInlines.isNotEmpty()) {
                throw IllegalArgumentException("Definition for ${conflictingInlines.joinToString(", ")} is already present.")
            }
            TextWithInlines(
                text = text + other.text,
                inlineContent = inlineContent + other.inlineContent
            )
        }
    }

    internal class Builder(
        text: TextWithInlines = empty
    ) {
        var textWithInlines: TextWithInlines = text
            private set

        val visualLength get() = textWithInlines.text.length

        fun append(text: TextWithInlines) {
            textWithInlines += text
        }

        fun appendInlineContent(
            inlineElementId: String,
            inlineContent: () -> InlineTextContent
        ) {
            val inlines = TextWithInlines(
                text = buildAnnotatedString {
                    appendInlineContent(inlineElementId)
                },
                inlineContent = mapOf(inlineElementId to inlineContent())
            )
            textWithInlines += inlines
        }

        fun appendStyled(textWithInlines: TextWithInlines, style: SpanStyle) {
            append(
                textWithInlines.copy(
                    text = buildAnnotatedString {
                        pushStyle(style)
                        append(textWithInlines.text)
                        pop()
                    }
                )
            )
        }
    }
}

internal inline fun buildMappedString(buildFn: TextWithInlines.Builder.() -> Unit): TextWithInlines =
    TextWithInlines.Builder().apply(buildFn).textWithInlines
