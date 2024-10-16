package me.okonecny.markdowneditor.view

import androidx.compose.runtime.Composable
import me.okonecny.interactivetext.InteractiveText
import me.okonecny.markdowneditor.DocumentTheme
import me.okonecny.markdowneditor.ast.data.Heading
import me.okonecny.markdowneditor.flexmark.FlexmarkDocument
import me.okonecny.wysiwyg.ast.VisualNode

internal class UiHeading : BlockRenderer<Heading, FlexmarkDocument> {
    @Composable
    override fun RenderContext<FlexmarkDocument>.render(block: VisualNode<Heading>) {
        val inlines = renderInlines(block.children)
        val styles = DocumentTheme.current.styles
        InteractiveText(
            interactiveId = block.interactiveId,
            text = inlines.text,
            textMapping = inlines.textMapping,
            inlineContent = inlines.inlineContent,
            style = when (block.data.level) {
                Heading.Level.H1 -> styles.h1
                Heading.Level.H2 -> styles.h2
                Heading.Level.H3 -> styles.h3
                Heading.Level.H4 -> styles.h4
                Heading.Level.H5 -> styles.h5
                Heading.Level.H6 -> styles.h6
            },
            activeAnnotationTags = activeAnnotationTags,
            onAnnotationCLick = handleLinks()
        )
    }
}