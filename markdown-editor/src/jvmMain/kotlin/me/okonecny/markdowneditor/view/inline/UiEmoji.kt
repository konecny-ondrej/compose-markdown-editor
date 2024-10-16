package me.okonecny.markdowneditor.view.inline

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import com.vladsch.flexmark.ext.emoji.EmojiImageType
import com.vladsch.flexmark.ext.emoji.EmojiShortcutType
import com.vladsch.flexmark.ext.emoji.internal.EmojiReference
import com.vladsch.flexmark.ext.emoji.internal.EmojiResolvedShortcut
import me.okonecny.interactivetext.BoundedBlockTextMapping
import me.okonecny.markdowneditor.MappedText
import me.okonecny.markdowneditor.ast.data.Emoji
import me.okonecny.markdowneditor.buildMappedString
import me.okonecny.markdowneditor.flexmark.FlexmarkDocument
import me.okonecny.markdowneditor.internal.Emoji
import me.okonecny.markdowneditor.view.InlineRenderer
import me.okonecny.markdowneditor.view.RenderContext
import me.okonecny.wysiwyg.ast.VisualNode

internal class UiEmoji : InlineRenderer<Emoji, FlexmarkDocument> {
    @Composable
    override fun RenderContext<FlexmarkDocument>.render(inlineNode: VisualNode<Emoji>): MappedText = buildMappedString {
        appendEmoji(
            inlineNode,
            MappedText(
                text = inlineNode.data.shortcut,
                textMapping = BoundedBlockTextMapping(
                    coveredSourceRange = inlineNode.sourceRange,
                    visualTextRange = TextRange(0, inlineNode.data.shortcut.length)
                )
            )
        )
    }
}

private fun MappedText.Builder.appendEmoji(emojiNode: VisualNode<Emoji>, fallback: MappedText) {
    val emojiShortcut = EmojiResolvedShortcut.getEmojiText(
        emojiNode.data.shortcut,
        EmojiShortcutType.GITHUB,
        EmojiImageType.IMAGE_ONLY,
        "/openmoji"
    )
    val resolvedEmoji: EmojiReference.Emoji? = emojiShortcut.emoji
    if (resolvedEmoji == null || resolvedEmoji.unicodeString.isEmpty()) {
        append(fallback)
        return
    }
    val emojiString = resolvedEmoji.unicodeString
    append(
        MappedText(
            text = resolvedEmoji.annotatedString,
            textMapping = BoundedBlockTextMapping(
                coveredSourceRange = emojiNode.sourceRange,
                visualTextRange = TextRange(visualLength, visualLength + emojiString.length)
            )
        )
    )
}

internal val EmojiReference.Emoji.unicodeString: String
    get() = buildString {
        unicodeChars
            ?.replace("U+", "")
            ?.split(" ")
            ?.map { hexValue -> Integer.parseInt(hexValue, 16) }
            ?.forEach { appendCodePoint(it) }
    }

internal val EmojiReference.Emoji.annotatedString: AnnotatedString
    get() = buildAnnotatedString {
        pushStyle(
            SpanStyle(
                fontFamily = FontFamily.Emoji
            )
        )
        append(unicodeString)
        pop()
    }
