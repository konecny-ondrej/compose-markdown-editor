package me.okonecny.markdowneditor

import com.vladsch.flexmark.parser.Parser
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser

/**
 * Kotlin-inject component to construct all the services needed by MarkdownEditor.
 */
@Component
internal abstract class MarkdownEditorComponent {
    abstract val documentParser: DocumentParser

    @Provides
    protected fun documentParser(parser: FlexmarkDocumentParser): DocumentParser = parser

    // region Jetbrains-Markdown
    @Provides
    protected fun markdownFlavour(): MarkdownFlavourDescriptor = GFMFlavourDescriptor()

    @Provides
    protected fun markdownParser(flavour: MarkdownFlavourDescriptor): MarkdownParser = MarkdownParser(flavour)

    // endregion Jetbrains-Markdown
    // region Flexmark
    @Provides
    protected fun flexmarkParser(): Parser = Parser.builder().build()

    // endregion Flexmark
}