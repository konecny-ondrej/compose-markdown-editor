package me.okonecny.markdowneditor.view.inline

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import com.vladsch.flexmark.ext.emoji.Emoji
import com.vladsch.flexmark.ext.emoji.EmojiImageType
import com.vladsch.flexmark.ext.emoji.EmojiShortcutType
import com.vladsch.flexmark.ext.emoji.internal.EmojiReference
import com.vladsch.flexmark.ext.emoji.internal.EmojiResolvedShortcut
import com.vladsch.flexmark.util.ast.Node
import me.okonecny.interactivetext.BoundedBlockTextMapping
import me.okonecny.markdowneditor.MappedText
import me.okonecny.markdowneditor.buildMappedString
import me.okonecny.markdowneditor.flexmark.rawCode
import me.okonecny.markdowneditor.internal.Emoji
import me.okonecny.markdowneditor.view.InlineRenderer
import me.okonecny.markdowneditor.view.RenderContext

internal class UiEmoji : InlineRenderer<Emoji, Node> {
    @Composable
    override fun RenderContext<Node>.render(inlineNode: Emoji): MappedText = buildMappedString {
        appendEmoji(
            inlineNode,
            inlineNode.rawCode()
        )
    }
}

private fun MappedText.Builder.appendEmoji(emojiNode: Emoji, fallback: MappedText) {
    val emojiShortcut = EmojiResolvedShortcut.getEmojiText(
        emojiNode,
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
                coveredSourceRange = TextRange(emojiNode.startOffset, emojiNode.endOffset),
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
