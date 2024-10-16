package me.okonecny.markdowneditor.view

import androidx.compose.runtime.Composable
import me.okonecny.interactivetext.InteractiveText
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.ast.data.Paragraph
import me.okonecny.markdowneditor.flexmark.FlexmarkDocument
import me.okonecny.wysiwyg.ast.VisualNode

internal class UiParagraph : BlockRenderer<Paragraph, FlexmarkDocument> {
    @Composable
    override fun RenderContext<FlexmarkDocument>.render(block: VisualNode<Paragraph>) {
        val inlines = renderInlines(block.children)
        val styles = DocumentTheme.current.styles
        InteractiveText(
            interactiveId = block.interactiveId,
            text = inlines.text,
            textMapping = inlines.textMapping,
            style = styles.paragraph,
            inlineContent = inlines.inlineContent,
            activeAnnotationTags = activeAnnotationTags,
            onAnnotationCLick = handleLinks()
        )
    }
}