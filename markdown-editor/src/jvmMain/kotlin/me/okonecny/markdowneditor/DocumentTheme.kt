package me.okonecny.markdowneditor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

@Immutable
class DocumentTheme(
    val styles: DocumentStyles = DocumentStyles()
) {
    companion object {
        val default: DocumentTheme = DocumentTheme(DocumentStyles())
        internal val current: DocumentTheme
            @Composable
            @ReadOnlyComposable
            get() = LocalDocumentTheme.current
    }

    fun copy(
        styles: DocumentStyles = this.styles.copy()
    ) = DocumentTheme(
        styles = styles
    )
}

internal val LocalDocumentTheme = staticCompositionLocalOf { DocumentTheme.default }
