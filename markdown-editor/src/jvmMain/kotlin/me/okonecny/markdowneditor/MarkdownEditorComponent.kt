package me.okonecny.markdowneditor

import com.vladsch.flexmark.parser.Parser
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

/**
 * Kotlin-inject component to construct all the services needed by MarkdownEditor.
 */
@Component
internal abstract class MarkdownEditorComponent {
    abstract val documentParser: DocumentParser

    @Provides
    protected fun flexmarkParser(): Parser = Parser.builder().build()
}