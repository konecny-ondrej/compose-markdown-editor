package me.okonecny.markdowneditor

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import me.okonecny.interactivetext.TextMapping
import me.okonecny.interactivetext.ZeroTextMapping
import me.okonecny.interactivetext.plus

/**
 * Text, which carries information about what part of source text it came from.
 */
data class MappedText(
    val text: AnnotatedString,
    val textMapping: TextMapping,
    val inlineContent: Map<String, InlineTextContent> = emptyMap()
) {
    constructor(text: String, textMapping: TextMapping) : this(AnnotatedString(text), textMapping)

    companion object {
        val empty: MappedText = MappedText("", ZeroTextMapping)
    }

    fun annotatedWith(tag: String, annotation: String): MappedText = MappedText(
        text = buildAnnotatedString {
            pushStringAnnotation(tag, annotation)
            append(text)
            pop()
        },
        textMapping = textMapping,
        inlineContent = inlineContent
    )

    operator fun plus(other: MappedText): MappedText = when {
        this === empty -> other
        other === empty -> this
        else -> {
            val conflictingInlines = inlineContent.keys.intersect(other.inlineContent.keys)
            if (conflictingInlines.isNotEmpty()) {
                throw IllegalArgumentException("Definition for ${conflictingInlines.joinToString(", ")} is already present.")
            }
            MappedText(
                text = text + other.text,
                textMapping = textMapping + other.textMapping,
                inlineContent = inlineContent + other.inlineContent
            )
        }
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

        fun appendInlineContent(
            textMapping: TextMapping,
            inlineElementId: String,
            inlineContent: () -> InlineTextContent
        ) {
            val inlines = MappedText(
                text = buildAnnotatedString {
                    appendInlineContent(inlineElementId)
                },
                textMapping = textMapping,
                inlineContent = mapOf(inlineElementId to inlineContent())
            )
            mappedText += inlines
        }

        fun append(text: String) { // TODO: do we want to append text without mapping?
            mappedText += MappedText(text, ZeroTextMapping)
        }

        fun appendStyled(mappedText: MappedText, style: SpanStyle) {
            append(
                mappedText.copy(
                    text = buildAnnotatedString {
                        pushStyle(style)
                        append(mappedText.text)
                        pop()
                    }
                )
            )
        }
    }
}

internal inline fun buildMappedString(buildFn: MappedText.Builder.() -> Unit): MappedText =
    MappedText.Builder().apply(buildFn).mappedText
