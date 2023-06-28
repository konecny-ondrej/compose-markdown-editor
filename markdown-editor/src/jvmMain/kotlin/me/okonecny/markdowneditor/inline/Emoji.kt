package me.okonecny.markdowneditor.inline

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import com.vladsch.flexmark.ext.emoji.Emoji
import com.vladsch.flexmark.ext.emoji.EmojiImageType
import com.vladsch.flexmark.ext.emoji.EmojiShortcutType
import com.vladsch.flexmark.ext.emoji.internal.EmojiReference
import com.vladsch.flexmark.ext.emoji.internal.EmojiResolvedShortcut
import me.okonecny.interactivetext.BoundedBlockTextMapping
import me.okonecny.markdowneditor.MappedText

internal fun MappedText.Builder.appendEmoji(emojiNode: Emoji, fallback: MappedText) {
    val emojiShortcut = EmojiResolvedShortcut.getEmojiText(
        emojiNode,
        EmojiShortcutType.GITHUB,
        EmojiImageType.IMAGE_ONLY,
        "/openmoji"
    )
    val resolvedEmoji: EmojiReference.Emoji? = emojiShortcut.emoji
    if (resolvedEmoji == null) {
        append(fallback)
        return
    }

    val emojiCodepoints = resolvedEmoji.unicodeChars
        .replace("U+", "")
        .split(" ")
        .map { hexValue -> Integer.parseInt(hexValue, 16) }
    val emojiString = StringBuilder()
    for (codepoint in emojiCodepoints) emojiString.appendCodePoint(codepoint)

    append(
        MappedText(
            text = buildAnnotatedString {
                pushStyle(
                    SpanStyle(
                        fontFamily = FontFamily(
                            Font("/NotoColorEmoji-Regular.ttf")
                        )
                    )
                )
                append(emojiString.toString())
                pop()
            },
            textMapping = BoundedBlockTextMapping(
                coveredSourceRange = TextRange(emojiNode.startOffset, emojiNode.endOffset),
                visualTextRange = TextRange(visualLength, visualLength + 1)
            )
        )
    )
}