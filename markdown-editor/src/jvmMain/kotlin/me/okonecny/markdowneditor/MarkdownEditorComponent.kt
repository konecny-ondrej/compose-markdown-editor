package me.okonecny.markdowneditor

import com.vladsch.flexmark.html.renderer.HeaderIdGenerator
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.parser.ParserEmulationProfile
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Scope

@Scope
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class MarkdownEditorScope

/**
 * Kotlin-inject component to construct all the services needed by MarkdownEditor.
 */
@MarkdownEditorScope
@Component
internal abstract class MarkdownEditorComponent {
    abstract val documentParser: DocumentParser

    @Provides
    @MarkdownEditorScope
    protected fun flexmarkParser(): Parser {
        val builder = Parser.builder()
        builder.setAll(ParserEmulationProfile.GITHUB.profileOptions)
        return builder.build()
    }

    @Provides
    @MarkdownEditorScope
    protected fun headerIdGenerator(): HeaderIdGenerator = HeaderIdGenerator()
}