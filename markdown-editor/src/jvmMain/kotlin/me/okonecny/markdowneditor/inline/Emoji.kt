package me.okonecny.markdowneditor.inline

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.em
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

    val emojiImage = resolvedEmoji.unicodeChars
        .replace("U+", "")
        .replace(" ", "-")
    appendInlineContent(
        textMapping = BoundedBlockTextMapping(
            coveredSourceRange = TextRange(emojiNode.startOffset, emojiNode.endOffset),
            visualTextRange = TextRange(visualLength, visualLength + 1)
        ),
        inlineElementId = "emoji$emojiImage"
    ) {
        InlineTextContent(
            Placeholder(1.3.em, 1.3.em, PlaceholderVerticalAlign.Center)
        ) {
            Image(
                painter = painterResource("/openmoji/${emojiImage}.svg"),
                contentDescription = emojiShortcut.alt,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}