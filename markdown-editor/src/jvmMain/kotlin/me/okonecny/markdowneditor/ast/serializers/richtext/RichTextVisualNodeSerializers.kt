package me.okonecny.markdowneditor.ast.serializers.richtext

import androidx.compose.ui.text.AnnotatedString
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.ast.data.BlockQuote
import me.okonecny.wysiwyg.ast.serializers.NodeToEmptyAnnotatedString
import me.okonecny.wysiwyg.ast.serializers.TextNodeAnnotatedStringSerializer
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializers

inline fun <reified D : Any> VisualNodeSerializers.Companion.richText(
    theme: DocumentTheme
): VisualNodeSerializers<D, AnnotatedString> = VisualNodeSerializers<D, AnnotatedString>()
    .withUnknownNodeSerializer(NodeToEmptyAnnotatedString())
    .withSerializer<D>(BlockChildrenToRichText())
    .withSerializer(TextNodeAnnotatedStringSerializer())
    .withSerializer(HeadingToRichText(theme.styles))
    .withSerializer(ParagraphToRichText())
    .withSerializer<BlockQuote>(BlockChildrenToRichText(theme.styles.blockQuote.textStyle))
//    .withSerializer(UiCodeFence(codeFenceRenderers))
//    .withSerializer(UiHtmlBlock())
//    .withSerializer(UiOrderedList())
//    .withSerializer(UiOrderedListItem())
//    .withSerializer(UiBulletList())
//    .withSerializer(UiBulletListItem())
//    .withSerializer(UiTaskListItem())
//    .withSerializer(UiTableBlock())
//    .withSerializer(UiText())
//    .withSerializer(UiTextBase())
//    .withSerializer(UiCode())
//    .withSerializer(UiEmphasis())
//    .withSerializer(UiStrongEmphasis())
//    .withSerializer(UiSoftLineBreak())
//    .withSerializer(UiGfmUser())
//    .withSerializer(UiStrikethrough())
//    .withSerializer(UiHardLineBreak())
//    .withSerializer(UiLink())
//    .withSerializer(UiAnchor())
//    .withSerializer(UiAutoLink())
//    .withSerializer(UiHtmlEntity())
//    .withSerializer(UiImage())
//    .withSerializer(UiEmoji())