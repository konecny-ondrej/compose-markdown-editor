package me.okonecny.markdowneditor.internal

import com.vladsch.flexmark.ext.autolink.AutolinkExtension
import com.vladsch.flexmark.ext.emoji.EmojiExtension
import com.vladsch.flexmark.ext.emoji.EmojiImageType
import com.vladsch.flexmark.ext.emoji.EmojiShortcutType
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension
import com.vladsch.flexmark.ext.gfm.users.GfmUsersExtension
import com.vladsch.flexmark.ext.tables.TablesExtension
import com.vladsch.flexmark.html.renderer.HeaderIdGenerator
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.parser.ParserEmulationProfile
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.cache.*
import io.ktor.client.plugins.logging.*
import me.okonecny.markdowneditor.flexmark.FlexmarkParser
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
    abstract val httpClient: HttpClient
    abstract val imageLoader: ImageLoader
    abstract val markdownParser: FlexmarkParser

    @Provides
    @MarkdownEditorScope
    protected fun httpClient(): HttpClient = HttpClient(CIO) {
        install(HttpCache)
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    co.touchlab.kermit.Logger.d(message, tag = "HttpClient")
                }
            }
        }
    }

    @Provides
    @MarkdownEditorScope
    protected fun flexmarkParser(): Parser {
        val builder = Parser.builder()
        builder.setAll(ParserEmulationProfile.GITHUB.profileOptions)
        builder.set(EmojiExtension.USE_IMAGE_TYPE, EmojiImageType.IMAGE_ONLY)
        builder.set(EmojiExtension.USE_SHORTCUT_TYPE, EmojiShortcutType.GITHUB)
        builder.extensions(
            listOf(
                TablesExtension.create(),
                TaskListExtension.create(),
                EmojiExtension.create(),
                AutolinkExtension.create(),
                GfmUsersExtension.create(),
                StrikethroughExtension.create()
            )
        )
        return builder.build()
    }

    @Provides
    @MarkdownEditorScope
    protected fun headerIdGenerator(): HeaderIdGenerator = HeaderIdGenerator()
}