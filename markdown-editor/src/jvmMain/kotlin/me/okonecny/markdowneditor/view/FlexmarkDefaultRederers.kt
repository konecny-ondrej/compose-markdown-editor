package me.okonecny.markdowneditor.view

import me.okonecny.markdowneditor.CodeFenceRenderer
import me.okonecny.markdowneditor.flexmark.FlexmarkDocument
import me.okonecny.markdowneditor.view.inline.*

fun Renderers.Companion.flexmarkDefault(
    codeFenceRenderers: List<CodeFenceRenderer> = emptyList()
) = Renderers<FlexmarkDocument>()
    .withUnknownBlockTypeRenderer(UiUnparsedBlock())
    .withUnknownInlineTypeRenderer(UiUnparsedInline())
    .withRenderer(UiHeading())
    .withRenderer(UiParagraph())
    .withRenderer(UiHorizontalRule())
    .withRenderer(UiBlockQuote())
    .withRenderer(UiCodeFence(codeFenceRenderers))
    .withRenderer(UiHtmlBlock())
    .withRenderer(UiOrderedList())
    .withRenderer(UiOrderedListItem())
    .withRenderer(UiBulletList())
    .withRenderer(UiBulletListItem())
    .withRenderer(UiTaskListItem())
    .withRenderer(UiTableBlock())
    .withRenderer(UiText())
    .withRenderer(UiTextBase())
    .withRenderer(UiCode())
    .withRenderer(UiEmphasis())
    .withRenderer(UiStrongEmphasis())
    .withRenderer(UiSoftLineBreak())
    .withRenderer(UiGfmUser())
    .withRenderer(UiStrikethrough())
    .withRenderer(UiHardLineBreak())
    .withRenderer(UiLink())
    .withRenderer(UiAnchor())
    .withRenderer(UiAutoLink())
    .withRenderer(UiHtmlEntity())
    .withRenderer(UiImage())
    .withRenderer(UiEmoji())
//.withRenderer<MailLink>() // TODO
//.withRenderer<HtmlInlineBase>() // TODO