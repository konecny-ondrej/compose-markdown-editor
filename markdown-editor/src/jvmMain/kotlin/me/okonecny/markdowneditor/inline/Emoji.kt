package me.okonecny.markdowneditor.inline

import androidx.compose.ui.text.AnnotatedString
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
                fontFamily = FontFamily(
                    Font("/NotoColorEmoji-Regular.ttf")
                )
            )
        )
        append(unicodeString)
        pop()
    }

internal fun String.isMaybeEmojiStart() = matches("^:[^:]+$".toRegex())
