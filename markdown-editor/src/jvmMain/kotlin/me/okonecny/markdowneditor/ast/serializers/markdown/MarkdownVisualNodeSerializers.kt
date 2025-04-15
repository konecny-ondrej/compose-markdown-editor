package me.okonecny.markdowneditor.ast.serializers.markdown

import androidx.compose.ui.text.AnnotatedString
import me.okonecny.wysiwyg.ast.serializers.NodeToEmptyAnnotatedString
import me.okonecny.wysiwyg.ast.serializers.TextNodeAnnotatedStringSerializer
import me.okonecny.wysiwyg.ast.serializers.VisualNodeSerializers

inline fun <reified D : Any> VisualNodeSerializers.Companion.markdown(): VisualNodeSerializers<D, AnnotatedString> =
    VisualNodeSerializers<D, AnnotatedString>()
        .withUnknownNodeSerializer(NodeToEmptyAnnotatedString())
        .withSerializer<D>(BlockChildrenToMarkdown())
        .withSerializer(TextNodeAnnotatedStringSerializer())
        .withSerializer(HeadingToMarkdown())
        .withSerializer(ParagraphToMarkdown())
        .withSerializer(BlockQuoteToMarkdown())
        .withSerializer(CodeBlockToMarkdown())
        .withSerializer(HorizontalRuleToMarkdown())
        .withSerializer(HtmlBlockToMarkdown())
        .withSerializer(OrderedListToMarkdown())
        .withSerializer(OrderedListItemToMarkdown())
        .withSerializer(BulletListToMarkdown())
        .withSerializer(BulletListItemToMarkdown())
        .withSerializer(TaskListItemToMarkdown())
// TODO
//    .withSerializer(UiTableBlock())
        .withSerializer(CodeSpanToMarkdown())
        .withSerializer(EmphasisToMarkdown())
        .withSerializer(StrongEmphasisToMarkdown())
        .withSerializer(SoftLineBreakToMarkdown())
        .withSerializer(UserMentionToMarkdown())
        .withSerializer(StrikethroughToMarkdown())
        .withSerializer(HardLineBreakToMarkdown())
        .withSerializer(LinkToMarkdown())
        .withSerializer(AutoLinkToMarkdown())
        .withSerializer(AnchorToMarkdown())
        .withSerializer(HtmlEntityToMarkdown())
        .withSerializer(ImageToMarkdown())
        .withSerializer(EmojiToMarkdown())